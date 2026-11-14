public class VideoSummaryApp {

  public static void main(String[] args) {
    if (args.length != 4) {
      System.err.println(
          "Usage: java -jar videoprocessor.jar "
              + "<inputPath> <outputCsv> <targetColor> <threshold>");
      return;
    }

    String inputPath = args[0];
    String outputCsv = args[1];

    if (!args[2].matches("[0-9a-fA-F]{6}")) {
      System.err.println(
          "Target color must contain six hex digits, such as FF0000.");
      return;
    }

    int targetColor = Integer.parseInt(args[2], 16);

    int threshold;
    try {
      threshold = Integer.parseInt(args[3]);
    } catch (NumberFormatException e) {
      System.err.println("Threshold must be an integer.");
      return;
    }

    if (threshold < 0) {
      System.err.println("Threshold must be nonnegative.");
      return;
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
      return;
    }

    System.out.println("Centroid tracking saved to " + outputCsv);
  }
}