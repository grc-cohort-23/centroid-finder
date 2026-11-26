import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The Video Summary Application.
 *
 * This application takes four command-line arguments:
 * 1. The path to an input video file (for example, "video.mp4").
 * 2. The path to write the output CSV to (for example, "output.csv").
 * 3. A target hex color in the format RRGGBB (for example, "FF0000" for red).
 * 4. An integer threshold for binarization.
 *
 * The application samples one frame every SAMPLE_INTERVAL_SECONDS and, for each sampled
 * frame, reuses the Part 1 image pipeline (DistanceImageBinarizer + EuclideanColorDistance,
 * then BinarizingImageGroupFinder + DfsBinaryGroupFinder) to find the largest centroid
 * matching the target color. Each sampled frame becomes one row of the output CSV:
 * "timestamp,x,y", where timestamp is seconds since the start of the video. If no
 * matching centroid is found in a sampled frame, that row's coordinates are (-1,-1).
 *
 * Usage:
 *   java -jar videoprocessor.jar <input_video> <output_csv> <hex_target_color> <threshold>
 *
 * Exit codes (the server treats any non-zero code as a failed job, and stores whatever was
 * written to stderr as the job's error text):
 * <ul>
 *   <li>{@value #EXIT_OK} - success, the CSV was written</li>
 *   <li>{@value #EXIT_USAGE} - wrong number of arguments</li>
 *   <li>{@value #EXIT_BAD_THRESHOLD} - the threshold is not an integer</li>
 *   <li>{@value #EXIT_BAD_COLOR} - the target color is not a 6 digit RRGGBB hex string</li>
 *   <li>{@value #EXIT_VIDEO_OPEN_FAILED} - the video could not be opened (missing file, not a video)</li>
 *   <li>{@value #EXIT_VIDEO_READ_FAILED} - an error happened while decoding frames</li>
 *   <li>{@value #EXIT_CSV_WRITE_FAILED} - the output CSV could not be written</li>
 * </ul>
 */
public class VideoSummaryApp {
    private static final double SAMPLE_INTERVAL_SECONDS = 0.5;

    /** Exit code: the video was processed and the CSV was written. */
    static final int EXIT_OK = 0;
    /** Exit code: the wrong number of command-line arguments was given. */
    static final int EXIT_USAGE = 1;
    /** Exit code: the threshold argument is not an integer. */
    static final int EXIT_BAD_THRESHOLD = 2;
    /** Exit code: the target color argument is not a valid RRGGBB hex string. */
    static final int EXIT_BAD_COLOR = 3;
    /** Exit code: the input video could not be opened. */
    static final int EXIT_VIDEO_OPEN_FAILED = 4;
    /** Exit code: the video opened, but decoding its frames failed part way through. */
    static final int EXIT_VIDEO_READ_FAILED = 5;
    /** Exit code: the output CSV file could not be created or written. */
    static final int EXIT_CSV_WRITE_FAILED = 6;

    private static final String USAGE =
            "Usage: java -jar videoprocessor.jar <input_video> <output_csv> <hex_target_color> <threshold>";

    /** Exactly six hex digits, matching the documented RRGGBB format. */
    private static final Pattern HEX_COLOR = Pattern.compile("[0-9A-Fa-f]{6}");

    /**
     * Entry point. Runs the app and exits the JVM with a non-zero code if anything failed.
     *
     * All the real work happens in {@link #run(String[])}, which returns the exit code instead
     * of calling System.exit itself. That split keeps the logic testable: System.exit would
     * stop the whole JUnit run, so tests call run() (or main() on the success path) and check
     * the result instead. On success main() simply returns, which ends the JVM with code 0.
     *
     * @param args the command-line arguments described in the class documentation
     */
    public static void main(String[] args) {
        int exitCode = run(args);
        if (exitCode != EXIT_OK) {
            System.exit(exitCode);
        }
    }

    /**
     * Runs the whole video summary pipeline and reports how it went as an exit code.
     *
     * Every failure prints a one line explanation to stderr and returns the matching
     * EXIT_* code from the class documentation. Nothing in here calls System.exit.
     *
     * @param args the command-line arguments described in the class documentation
     * @return {@link #EXIT_OK} on success, otherwise one of the non-zero EXIT_* codes
     */
    static int run(String[] args) {
        if (args.length != 4) {
            // Wrong usage is a failure, so it goes to stderr where the server will see it.
            System.err.println(USAGE);
            return EXIT_USAGE;
        }

        String inputVideoPath = args[0];
        String outputCsvPath = args[1];
        String hexTargetColor = args[2];

        int threshold;
        try {
            threshold = parseThreshold(args[3]);
        } catch (NumberFormatException e) {
            System.err.println("Threshold must be an integer.");
            return EXIT_BAD_THRESHOLD;
        }

        int targetColor;
        try {
            targetColor = parseTargetColor(hexTargetColor);
        } catch (NumberFormatException e) {
            System.err.println("Invalid hex target color. Please provide a color in RRGGBB format.");
            return EXIT_BAD_COLOR;
        }

        // Reuse the Part 1 image pipeline, unchanged.
        ColorDistanceFinder distanceFinder = new EuclideanColorDistance();
        ImageBinarizer binarizer = new DistanceImageBinarizer(distanceFinder, targetColor, threshold);
        ImageGroupFinder imageGroupFinder = new BinarizingImageGroupFinder(binarizer, new DfsBinaryGroupFinder());

        // Opening is done separately from reading so the two failures get different exit codes.
        FfmpegVideoFrameGrabber grabber;
        try {
            grabber = new FfmpegVideoFrameGrabber(inputVideoPath);
        } catch (IOException e) {
            System.err.println("Error opening video: " + inputVideoPath + " (" + e.getMessage() + ")");
            return EXIT_VIDEO_OPEN_FAILED;
        }

        List<VideoResult> results;
        try (grabber) {
            VideoGroupFinder videoGroupFinder = new SamplingVideoGroupFinder(grabber, imageGroupFinder, SAMPLE_INTERVAL_SECONDS);
            results = videoGroupFinder.findCentroids();
        } catch (UncheckedIOException e) {
            // Thrown by FfmpegVideoFrameGrabber.nextFrame() when a later frame can't be decoded.
            System.err.println("Error reading video: " + inputVideoPath + " (" + e.getCause().getMessage() + ")");
            return EXIT_VIDEO_READ_FAILED;
        } catch (IOException e) {
            // Thrown by close() (a FrameGrabber.Exception) if FFmpeg fails to shut down cleanly.
            System.err.println("Error closing video: " + inputVideoPath + " (" + e.getMessage() + ")");
            return EXIT_VIDEO_READ_FAILED;
        }

        try {
            new VideoResultWriter(outputCsvPath).writeResults(results);
        } catch (FileNotFoundException e) {
            // PrintWriter reports "can't create or open the file" as FileNotFoundException,
            // for example when the output folder doesn't exist or isn't writable.
            System.err.println("Error writing output CSV: " + outputCsvPath + " (" + e.getMessage() + ")");
            return EXIT_CSV_WRITE_FAILED;
        }

        System.out.println("Video summary saved as " + outputCsvPath);
        return EXIT_OK;
    }

    /**
     * Parses a hex color string in RRGGBB format into a 24-bit integer (0xRRGGBB).
     *
     * The string must be exactly six hex digits. Integer.parseInt alone would also accept
     * things like "-1" or "FFF", which are not valid colors, so the format is checked first.
     *
     * @param hexColor the hex color string to parse
     * @return the parsed color as a 24-bit integer
     * @throws NumberFormatException if hexColor is not exactly six hex digits
     */
    static int parseTargetColor(String hexColor) {
        if (hexColor == null || !HEX_COLOR.matcher(hexColor).matches()) {
            throw new NumberFormatException("Not an RRGGBB hex color: " + hexColor);
        }
        return Integer.parseInt(hexColor, 16);
    }

    /**
     * Parses a binarization threshold from a command-line argument string.
     *
     * @param thresholdArg the threshold string to parse
     * @return the parsed threshold
     * @throws NumberFormatException if thresholdArg is not a valid integer
     */
    static int parseThreshold(String thresholdArg) {
        return Integer.parseInt(thresholdArg);
    }
}
