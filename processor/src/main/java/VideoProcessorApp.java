import java.io.IOException;
import java.nio.file.Path;

import org.jcodec.api.JCodecException;

public class VideoProcessorApp {
    public static void main(String[] args) {
        System.exit(run(args));
    }

    public static int run(String[] args) {
        if (args.length != 4) {
            System.err.println(
                    "Usage: java -jar videoprocessor.jar <inputPath> <outputCsv> <targetColor> <threshold>");
            return 1;
        }

        String inputPath = args[0];
        String outputCsv = args[1];
        String hexTargetColor = args[2];
        String thresholdText = args[3];

        int threshold;

        try {
            threshold = Integer.parseInt(thresholdText);
        } catch (NumberFormatException e) {
            System.err.println("Threshold must be an integer.");
            return 1;
        }

        int targetColor;

        try {
            targetColor = Integer.parseInt(hexTargetColor, 16);
        } catch (NumberFormatException e) {
            System.err.println("Target color must be a valid hexadecimal number.");
            return 1;
        }

        ColorDistanceFinder distanceFinder = new EuclideanColorDistance();
        ImageBinarizer binarizer =
                new DistanceImageBinarizer(distanceFinder, targetColor, threshold);
        ImageGroupFinder groupFinder =
                new BinarizingImageGroupFinder(binarizer, new DfsBinaryGroupFinder());

        VideoCentroidProcessor processor = new VideoCentroidProcessor(groupFinder);

        try (VideoFrameReader reader = new VideoFrameReader(Path.of(inputPath));
                CentroidCsvWriter csvWriter = new CentroidCsvWriter(outputCsv)) {
            VideoFrameReader.VideoFrame frame;

            while ((frame = reader.readNext()) != null) {
                VideoCentroidProcessor.Result result = processor.process(frame);
                csvWriter.writeResult(result);
            }
        } catch (IOException | JCodecException e) {
            System.err.println("Error processing video: " + e.getMessage());
            return 1;
        }

        return 0;
    }
}