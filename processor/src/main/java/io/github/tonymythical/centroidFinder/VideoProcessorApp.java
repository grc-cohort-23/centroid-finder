package io.github.tonymythical.centroidFinder;
import java.awt.Color;
import java.io.File;
import java.util.List;

public class VideoProcessorApp {

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: java -jar videoprocessor.jar <inputPath> <outputCsv> <targetColorHex> <threshold>");
            System.exit(1);
        }

        String inputPath = args[0];
        String outputCsv = args[1];
        String targetColorHex = args[2];
        int threshold = 0;

        try {
            threshold = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            System.err.println("Threshold must be an integer.");
            System.exit(1);
        }

        int targetColor = 0;
        try {
            targetColor = Integer.parseInt(targetColorHex.replace("#", ""), 16);
        } catch (NumberFormatException e) {
            System.err.println("Invalid hex target color. Please provide a color in RRGGBB format.");
            System.exit(1);
        }

        ColorDistanceFinder distanceFinder = new EuclideanColorDistance();
        DistanceImageBinarizer binarizer = new DistanceImageBinarizer(distanceFinder, targetColor, threshold);
        DfsBinaryGroupFinder groupFinderImpl = new DfsBinaryGroupFinder();
        BinarizingImageGroupFinder binarizingFinder = new BinarizingImageGroupFinder(binarizer, groupFinderImpl);

        VideoCentroidTracker tracker = new VideoCentroidTracker(binarizingFinder);
        CsvReportWriter writer = new CsvReportWriter();

        try {
            File videoFile = new File(inputPath);
            List<CentroidTimestampRecord> results = tracker.processVideo(videoFile);
            writer.writeRecords(outputCsv, results);
            System.out.println("Processing complete. CSV output saved to: " + outputCsv);
        } catch (Exception e) {
            System.err.println("Error processing video: " + e.getMessage());
            e.printStackTrace();
        }
    }
}