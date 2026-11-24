package io.github.tonymythical.centroidFinder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

/**
 * Generates an initial preview and binarized frame from a video for client tuning.
 */
public class VideoPreviewApp {
    public static void main(String[] args) {
        CliArgumentParser config;
        try {
            config = CliArgumentParser.parsePreviewArgs(args);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(1);
            return;
        }

        File videoFile = new File(config.inputPath());
        File outputFile = new File(config.outputPath());

        try {
            Picture picture = FrameGrab.getFrameAtSec(videoFile, 0.0);

            if (picture == null) {
                throw new IOException("Could not extract video frame.");
            }

            BufferedImage frame = AWTUtil.toBufferedImage(picture);
            ColorDistanceFinder distanceFinder = new EuclideanColorDistance();
            DistanceImageBinarizer binarizer = new DistanceImageBinarizer(distanceFinder, config.targetColor(), config.threshold());
            int[][] binaryArray = binarizer.toBinaryArray(frame);
            BufferedImage binaryFrame = binarizer.toBufferedImage(binaryArray);

            DfsBinaryGroupFinder groupFinder = new DfsBinaryGroupFinder();
            List<Group> groups = groupFinder.findConnectedGroups(binaryArray);

            writePng(binaryFrame, outputFile);
            if (config.originalImagePath() != null) {
                writePng(frame, new File(config.originalImagePath()));
            }
            System.out.printf("Frame size: %d,%d%n", frame.getWidth(), frame.getHeight());

            if (groups.isEmpty()) {
                System.out.println("Centroid: -1, -1");
            } else {
                Group largestGroup = groups.get(0);
                int x = largestGroup.centroid().x();
                int y = largestGroup.centroid().y();
                System.out.println("Centroid: " + x + "," + y);
            }

            System.out.println("Preview frame saved to: " + outputFile);

        } 
        catch (IOException e) {
            System.err.println("I/O error generating preview: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (JCodecException e) {
            System.err.println("Could not decode video frame: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void writePng(BufferedImage image, File output) throws IOException {
        if (!ImageIO.write(image, "png", output)) {
            throw new IOException("No PNG image writer available.");
        }
    }
}
