import java.io.IOException;
import java.util.List;

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
 */
public class VideoSummaryApp {
    private static final double SAMPLE_INTERVAL_SECONDS = 0.5;

    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Usage: java -jar videoprocessor.jar <input_video> <output_csv> <hex_target_color> <threshold>");
            return;
        }

        String inputVideoPath = args[0];
        String outputCsvPath = args[1];
        String hexTargetColor = args[2];

        int threshold;
        try {
            threshold = parseThreshold(args[3]);
        } catch (NumberFormatException e) {
            System.err.println("Threshold must be an integer.");
            return;
        }

        int targetColor;
        try {
            targetColor = parseTargetColor(hexTargetColor);
        } catch (NumberFormatException e) {
            System.err.println("Invalid hex target color. Please provide a color in RRGGBB format.");
            return;
        }

        // Reuse the Part 1 image pipeline, unchanged.
        ColorDistanceFinder distanceFinder = new EuclideanColorDistance();
        ImageBinarizer binarizer = new DistanceImageBinarizer(distanceFinder, targetColor, threshold);
        ImageGroupFinder imageGroupFinder = new BinarizingImageGroupFinder(binarizer, new DfsBinaryGroupFinder());

        List<VideoResult> results;
        try (FfmpegVideoFrameGrabber grabber = new FfmpegVideoFrameGrabber(inputVideoPath)) {
            VideoGroupFinder videoGroupFinder = new SamplingVideoGroupFinder(grabber, imageGroupFinder, SAMPLE_INTERVAL_SECONDS);
            results = videoGroupFinder.findCentroids();
        } catch (IOException e) {
            System.err.println("Error opening video: " + inputVideoPath);
            return;
        } catch (Exception e) {
            System.err.println("Error reading video: " + inputVideoPath);
            e.printStackTrace();
            return;
        }

        try {
            new VideoResultWriter(outputCsvPath).writeResults(results);
            System.out.println("Video summary saved as " + outputCsvPath);
        } catch (Exception e) {
            System.err.println("Error writing output CSV: " + outputCsvPath);
            e.printStackTrace();
        }
    }

    /**
     * Parses a hex color string in RRGGBB format into a 24-bit integer (0xRRGGBB).
     *
     * @param hexColor the hex color string to parse
     * @return the parsed color as a 24-bit integer
     * @throws NumberFormatException if hexColor is not a valid hex string
     */
    static int parseTargetColor(String hexColor) {
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
