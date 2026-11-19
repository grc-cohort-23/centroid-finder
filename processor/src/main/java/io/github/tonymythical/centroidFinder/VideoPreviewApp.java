package io.github.tonymythical.centroidFinder;
import java.io.File;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.List;

import org.jcodec.api.FrameGrab;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

public class VideoPreviewApp {
    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: VideoPreviewApp <inputVideo> <outputImage> <targetColor> <threshold>");
            return;
        }

        File videoFile = new File(args[0]);
        File outputFile = new File(args[1]);
        int targetColor = Integer.parseInt(args[2].replace("#", ""), 16);
        int threshold = Integer.parseInt(args[3]);

        

        try {
            Picture picture = FrameGrab.getFrameAtSec(videoFile, 0.0);

            if (picture == null) {
                System.err.println("Could not extract video frame.");
                return;
            }

            BufferedImage frame = AWTUtil.toBufferedImage(picture);

            ColorDistanceFinder distanceFinder = new EuclideanColorDistance();

            DistanceImageBinarizer binarizer = new DistanceImageBinarizer(distanceFinder, targetColor, threshold);

            int[][] binaryArray = binarizer.toBinaryArray(frame);

            BufferedImage binaryFrame = binarizer.toBufferedImage(binaryArray);

            DfsBinaryGroupFinder groupFinder = new DfsBinaryGroupFinder();

            List<Group> groups = groupFinder.findConnectedGroups(binaryArray);


            if (groups.isEmpty()) {
                System.out.println("Centroid: -1, -1");
            } else {
                Group largestGroup = groups.get(0);

                int x = largestGroup.centroid().x();
                int y = largestGroup.centroid().y();

                System.out.println("Centroid: " + x + "," + y);
            }


            ImageIO.write(binaryFrame, "png", outputFile);

            System.out.println("Preview frame saved to: " + outputFile);

        } catch (Exception e) {
           System.err.println("Error generating preview: " + e.getMessage());
            e.printStackTrace();
        }

    }
}