package io.github.tonymythical.centroidFinder;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.List;

import org.jcodec.api.FrameGrab;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

public class VideoPreviewApp {
    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: VideoPreviewApp <inputVideo> <outputImage> <targetColor> <threshold> [originalImage]");
            System.exit(1);
            return;
        }

        File videoFile = new File(args[0]);
        File outputFile = new File(args[1]);
        int targetColor = Integer.parseInt(args[2].replace("#", ""), 16);
        int threshold = Integer.parseInt(args[3]);

        try {
            Picture picture = FrameGrab.getFrameAtSec(videoFile, 0.0);

            if (picture == null) {
                throw new IOException("Could not extract video frame.");
            }

            BufferedImage frame = AWTUtil.toBufferedImage(picture);

            ColorDistanceFinder distanceFinder = new EuclideanColorDistance();

            DistanceImageBinarizer binarizer = new DistanceImageBinarizer(distanceFinder, targetColor, threshold);

            int[][] binaryArray = binarizer.toBinaryArray(frame);

            BufferedImage binaryFrame = binarizer.toBufferedImage(binaryArray);

            DfsBinaryGroupFinder groupFinder = new DfsBinaryGroupFinder();

            List<Group> groups = groupFinder.findConnectedGroups(binaryArray);


            writePng(binaryFrame, outputFile);
            if (args.length >= 5) {
                writePng(frame, new File(args[4]));
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

        } catch (Exception e) {
           System.err.println("Error generating preview: " + e.getMessage());
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
