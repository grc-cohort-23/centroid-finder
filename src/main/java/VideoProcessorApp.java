import java.io.FileWriter;

public class VideoProcessorApp {

    public static void main(String[] args) {

        try {
            VideoProcessorOptions options = new VideoProcessorOptions(args);

            ColorDistanceFinder distance = new EuclideanColorDistance();
            ImageBinarizer binarizer = new DistanceImageBinarizer(
                    distance, options.getTargetColor(), options.getThreshold());
            ImageGroupFinder groupFinder =
                    new BinarizingImageGroupFinder(binarizer, new DfsBinaryGroupFinder());

            try (VideoFrameExtractor extractor =
                    new VideoFrameExtractor(options.getInputPath());
                 CsvWriter writer =
                    new CsvWriter(new FileWriter(options.getOutputPath()))) {

                new VideoProcessor(extractor, groupFinder, writer).process();
                System.out.println("Done. Wrote " + options.getOutputPath());
            }

        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("Error processing video: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

