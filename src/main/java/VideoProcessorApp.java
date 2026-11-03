public class VideoProcessorApp {
      private static final double STEP_SECONDS = 1.0;
    public static void main(String[] args) {
        VideoProcessorArgs parsed;
        try {
            parsed = new ArgumentParser().parse(args);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(1);
            return;
        }

        ImageGroupFinder finder = new BinarizingImageGroupFinder(
            new DistanceImageBinarizer(new EuclideanColorDistance(),
                                       parsed.targetColor(), parsed.threshold()),
            new DfsBinaryGroupFinder());
        VideoProcessor processor = new VideoProcessor(new CentroidTracker(finder));

        try (FrameSource source = new JavaCvFrameSource(parsed.inputPath(), STEP_SECONDS);
             ResultWriter writer = new ResultWriter(parsed.outputCsv())) {
            int count = processor.process(source, writer);
            System.out.println("Processed " + count + " frames -> " + parsed.outputCsv());
        } catch (Exception e) {
            System.err.println("Failed to process video: " + e.getMessage());
            System.exit(1);
        }
    }
}
