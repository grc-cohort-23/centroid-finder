package io.github.nadia5129.centroidfinder;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * The Video Summary Application. It is the video version of ImageSummaryApp.
 *
 * It takes four command line arguments:
 * inputPath outputCsv targetColor threshold
 *
 * It builds the pieces the program needs, then goes through the video one frame
 * at a time and writes the largest centroid of each frame to a CSV as seconds,x,y.
 * If a frame has no centroid, the row has -1,-1 for x and y.
 *
 * Usage:
 *   java -jar videoprocessor.jar <inputPath> <outputCsv> <targetColor> <threshold>
 */
public class VideoSummaryApp {

    //Groups whose centroid is in the top TOP_MARGIN rows are ignored.
    //In the salamander video the dark strip's centroid is always above y=20,
    //and the salamander's is always well below, so 30 leaves room on both sides.
    static final int TOP_MARGIN = 30;

    public static void main(String[] args) {
        //checks all 4 arguments, and stops the program with a clear message if one is bad
        ArgumentParser arguments;
        try {
            arguments = new ArgumentParser(args);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            //1 tells whoever ran the program that it failed
            System.exit(1);
            return;
        }

        //reuses the same classes ImageSummaryApp uses to find groups in an image
        ImageBinarizer binarizer = new DistanceImageBinarizer(
            new EuclideanColorDistance(), arguments.getTargetColor(), arguments.getThreshold());
        ImageGroupFinder groupFinder = new BinarizingImageGroupFinder(binarizer, new DfsBinaryGroupFinder());
        //ignore the dark strip along the top edge of the salamander videos
        ImageGroupFinder filteredGroupFinder = new TopEdgeFilteringGroupFinder(groupFinder, TOP_MARGIN);
        ImageCentroidTracker tracker = new ImageCentroidTracker(filteredGroupFinder);

        //The try statement automatically closes the video and the CSV, even if something fails
        try (FrameReader reader = new JavaCVFrameReader(arguments.getInputPath());
             CSVOutputWriter writer = new CSVOutputWriter(arguments.getOutputCsv())) {
            processFrames(reader, tracker, writer);
        } catch (IOException e) {
            System.err.println("Error processing video: " + e.getMessage());
            System.exit(1);
        }
    }

    //The main loop: find the centroid of every frame and write one row for each.
    //It takes in a FrameReader, not a JavaCVFrameReader, so tests can give it a fake video.
    static void processFrames(FrameReader reader, ImageCentroidTracker tracker, CSVOutputWriter writer)
            throws IOException {
        //nextFrame() returns null when the video is over
        BufferedImage frame;
        while ((frame = reader.nextFrame()) != null) {
            double seconds = reader.getTimestamp();
            Coordinate centroid = tracker.findCentroid(frame);
            writer.writeRow(seconds, centroid);
        }
    }
}
