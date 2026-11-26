public class VideoSummaryApp {

  public static void main(String[] args) {
    if (args.length != 4) {
      System.err.println(
          "Usage: java -jar videoprocessor.jar "
              + "<inputPath> <outputCsv> <targetColor> <threshold>");
      System.exit(1);
    }

    String inputPath = args[0];
    String outputCsv = args[1];

    if (!args[2].matches("[0-9a-fA-F]{6}")) {
      System.err.println(
          "Target color must contain six hex digits, such as FF0000.");
      System.exit(1);
    }

    int targetColor = Integer.parseInt(args[2], 16);

    int threshold;
    try {
      threshold = Integer.parseInt(args[3]);
    } catch (NumberFormatException e) {
      System.err.println("Threshold must be an integer.");
      System.exit(1);
      return; // Never reached, but tells the compiler threshold is set below.
    }

    if (threshold < 0) {
      System.err.println("Threshold must be nonnegative.");
      System.exit(1);
    }

    ColorDistanceFinder distanceFinder = new EuclideanColorDistance();

    ImageBinarizer binarizer = new DistanceImageBinarizer(
        distanceFinder, targetColor, threshold);

    ImageGroupFinder groupFinder = new BinarizingImageGroupFinder(
        binarizer, new DfsBinaryGroupFinder());

    VideoProcessor processor = new VideoProcessor(
        groupFinder, new LargestGroupSelector());

    try (
        FrameSource source = new JavaCvFrameSource(inputPath);
        CsvWriter writer = new CsvWriter(outputCsv)) {
      processor.process(source, writer);
    } catch (Exception e) {
      System.err.println("Video processing failed: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }

    System.out.println("Centroid tracking saved to " + outputCsv);
  }
}