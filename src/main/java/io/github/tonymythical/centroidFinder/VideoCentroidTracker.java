package io.github.tonymythical.centroidFinder;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VideoCentroidTracker {
    private final ImageGroupFinder groupFinder;

    public VideoCentroidTracker(ImageGroupFinder groupFinder) {
        this.groupFinder = groupFinder;
    }

    public List<CentroidTimestampRecord> processVideo(File videoFile) {
        List<CentroidTimestampRecord> records = new ArrayList<>();
        double currentSecond = 0.0;

        try (VideoFrameReader reader = new JCodecVideoFrameReader(videoFile)) {
            BufferedImage frame;
            while ((frame = reader.nextFrame()) != null) {
                List<Group> groups = groupFinder.findConnectedGroups(frame);

                if (groups == null || groups.isEmpty()) {
                    records.add(new CentroidTimestampRecord(currentSecond, -1, -1));
                } else {
                    Group largestGroup = groups.get(0);
                    int x = largestGroup.centroid().x();
                    int y = largestGroup.centroid().y();
                    records.add(new CentroidTimestampRecord(currentSecond, x, y));
                }
                currentSecond += 1.0; 
            }
        } catch (Exception e) {
            System.err.println("Error processing video frames: " + e.getMessage());
        }

        return records;
    }
}
