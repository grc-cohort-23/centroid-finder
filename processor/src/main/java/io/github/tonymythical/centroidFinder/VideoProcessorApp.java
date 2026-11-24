package io.github.tonymythical.centroidFinder;

import java.io.File;
import java.util.List;

/**
 * Main entry point for the command-line video processor application.
 */
public class VideoProcessorApp {

    public static void main(String[] args) {
        CliArgumentParser config;
        try {
            config = CliArgumentParser.parseProcessorArgs(args);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(1);
            return;
        }

        ColorDistanceFinder distanceFinder = new EuclideanColorDistance();
        DistanceImageBinarizer binarizer = new DistanceImageBinarizer(distanceFinder, config.targetColor(), config.threshold());
        DfsBinaryGroupFinder groupFinderImpl = new DfsBinaryGroupFinder();
        BinarizingImageGroupFinder binarizingFinder = new BinarizingImageGroupFinder(binarizer, groupFinderImpl);

        VideoCentroidTracker tracker = new VideoCentroidTracker(binarizingFinder);
        CsvReportWriter writer = new CsvReportWriter();

        try {
            File videoFile = new File(config.inputPath());
            List<CentroidTimestampRecord> results = tracker.processVideo(videoFile);
            writer.writeRecords(config.outputPath(), results);
            System.out.println("Processing complete. CSV output saved to: " + config.outputPath());
        } catch (IOException e) {
            System.err.println("I/O error while processing video or writing CSV: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (JCodecException e) {
            System.err.println("Could not decode video: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
