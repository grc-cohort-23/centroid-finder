package io.github.akurfurst.centroidFinder;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

/**
 * This class uses an ImageGroupFinder to find groups of pixels at each frame in a video.
 * The centroid of the largest group is stored in a list and returned in the order they are found (ascending time).
 * 
 */
public class VideoProcessorHelper {
    private final ImageGroupFinder groupFinder;

    /**
     * Constructs a new VideoProcessorHelper using the specified ImageGroupFinder
     * 
     * @param groupFinder the ImageGroupFinder used to find centroids
     */
    public VideoProcessorHelper(ImageGroupFinder groupFinder) {
        this.groupFinder = groupFinder;
    }

    /**
     * 
     * Finds the centroid of the largest group at each frame in a video.
     * 
     * Returned in time ASCENDING order
     * 
     * @param video
     * @return a list of centroids in a video
     */
    public List<CentroidAtTime> processVideo(FrameGrabber video) throws Exception{
        List<CentroidAtTime> result = new ArrayList<>();
        Java2DFrameConverter converter = new Java2DFrameConverter();
        Frame frame;
        video.start();

        int total = video.getLengthInFrames();
        int count = 0;

        while ((frame = video.grab()) != null) {
            System.err.printf("\rFrame %d / %d (%.1f%%)", count, total, 100.0 * count / total);
            BufferedImage current = converter.convert(frame);
            if(current == null) continue;
            count++;
            List<Group> currentCentroids = groupFinder.findConnectedGroups(current);
            if (currentCentroids.isEmpty()) {
                result.add(new CentroidAtTime(new Coordinate(-1, -1), video.getTimestamp()));
                continue;
            }
            result.add(new CentroidAtTime(currentCentroids.get(0).centroid(), video.getTimestamp()));
        }
        System.err.println();
        video.close();
        converter.close();
        return result;
    }
}
