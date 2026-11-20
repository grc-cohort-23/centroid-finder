import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.imageio.ImageIO;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;

/**
 * Shows what the tracker sees, for tuning the target color and threshold.
 *
 * Writes to the output directory:
 * - debug.mp4: every frame, with the original and the mask side by side
 * - frame_NNNNNN.png: full-size versions of every Nth frame
 *
 * It uses the same binarizer, group finder and selector as VideoSummaryApp, so
 * the crosshair lands on the coordinates that app writes to its CSV.
 */
public class VideoDebugApp {
  private static final int DEFAULT_SAMPLE_EVERY = 30;
  private static final int MAX_VIDEO_WIDTH = 1920;
  private static final double FALLBACK_FRAME_RATE = 30.0;

  public static void main(String[] args) {
    System.setProperty("java.awt.headless", "true");

    if (args.length < 4 || args.length > 5) {
      System.err.println(
          "Usage: java -cp videoprocessor.jar VideoDebugApp "
              + "<inputPath> <outputDir> <targetColor> <threshold> [sampleEveryNFrames]");
      return;
    }

    if (!args[2].matches("[0-9a-fA-F]{6}")) {
      System.err.println(
          "Target color must contain six hex digits, such as FF0000.");
      return;
    }

    int targetColor = Integer.parseInt(args[2], 16);
    int threshold;
    int sampleEvery = DEFAULT_SAMPLE_EVERY;

    try {
      threshold = Integer.parseInt(args[3]);
      if (args.length == 5) {
        sampleEvery = Integer.parseInt(args[4]);
      }
    } catch (NumberFormatException e) {
      System.err.println("Threshold and sample interval must be integers.");
      return;
    }

    if (threshold < 0 || sampleEvery < 1) {
      System.err.println(
          "Threshold must be nonnegative and the sample interval must be at least 1.");
      return;
    }

    try {
      run(args[0], Path.of(args[1]), targetColor, threshold, sampleEvery);
    } catch (Exception e) {
      System.err.println("Debug run failed: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static void run(
      String inputPath,
      Path outputDir,
      int targetColor,
      int threshold,
      int sampleEvery) throws Exception {
    Files.createDirectories(outputDir);

    ImageBinarizer binarizer = new DistanceImageBinarizer(
        new EuclideanColorDistance(), targetColor, threshold);
    BinaryGroupFinder groupFinder = new DfsBinaryGroupFinder();
    LargestGroupSelector selector = new LargestGroupSelector();
    DebugFrameRenderer renderer = new DebugFrameRenderer();

    int frameCount = 0;
    int noDetectionCount = 0;
    double biggestJump = 0;
    double biggestJumpSeconds = 0;
    Coordinate previous = null;

    FFmpegFrameRecorder recorder = null;

    try (
        JavaCvFrameSource source = new JavaCvFrameSource(inputPath);
        Java2DFrameConverter videoConverter = new Java2DFrameConverter()) {
      VideoFrame frame = source.nextFrame();

      while (frame != null) {
        // Binarized here rather than through BinarizingImageGroupFinder
        // because the mask is needed for drawing as well as for the groups.
        int[][] binary = binarizer.toBinaryArray(frame.image());
        List<Group> groups = groupFinder.findConnectedGroups(binary);
        Coordinate centroid = selector.select(groups);

        BufferedImage panels = renderer.render(
            frame.image(),
            binarizer.toBufferedImage(binary),
            groups,
            centroid,
            frame.seconds());

        if (frameCount % sampleEvery == 0) {
          File png = outputDir
              .resolve(String.format("frame_%06d.png", frameCount))
              .toFile();
          ImageIO.write(panels, "png", png);
        }

        BufferedImage videoImage =
            DebugFrameRenderer.fitForVideo(panels, MAX_VIDEO_WIDTH);

        if (recorder == null) {
          recorder = startRecorder(
              outputDir.resolve("debug.mp4"), videoImage, source.frameRate());
        }
        recorder.record(videoConverter.convert(videoImage));

        if (groups.isEmpty()) {
          noDetectionCount++;
          previous = null;
        } else {
          if (previous != null) {
            double jump = Math.hypot(
                centroid.x() - previous.x(), centroid.y() - previous.y());
            if (jump > biggestJump) {
              biggestJump = jump;
              biggestJumpSeconds = frame.seconds();
            }
          }
          previous = centroid;
        }

        frameCount++;
        frame = source.nextFrame();
      }
    } finally {
      if (recorder != null) {
        recorder.close();
      }
    }

    System.out.println("Frames processed: " + frameCount);
    System.out.printf(
        "No detection: %d (%.1f%%)%n",
        noDetectionCount,
        frameCount == 0 ? 0.0 : 100.0 * noDetectionCount / frameCount);
    System.out.printf(
        "Biggest jump between frames: %.0f px at t=%.3fs%n",
        biggestJump, biggestJumpSeconds);
    System.out.println("Debug output saved to " + outputDir);
  }

  private static FFmpegFrameRecorder startRecorder(
      Path outputPath, BufferedImage firstImage, double frameRate)
      throws Exception {
    FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(
        outputPath.toString(), firstImage.getWidth(), firstImage.getHeight());

    recorder.setFormat("mp4");
    recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
    recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
    recorder.setFrameRate(frameRate > 0 ? frameRate : FALLBACK_FRAME_RATE);
    recorder.start();

    return recorder;
  }
}
