package io.github.tonymythical.centroidFinder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jcodec.api.JCodecException;

/**
 * Tracks centroids across sequential video frames using an ImageGroupFinder.
 */
public class VideoCentroidTracker {
    private final ImageGroupFinder groupFinder;

    /**
     * Constructs a VideoCentroidTracker with the specified group finder.
     *
     * @param groupFinder the finder used to detect connected pixel groups in each frame
     */
    public VideoCentroidTracker(ImageGroupFinder groupFinder) {
        this.groupFinder = groupFinder;
    }

    /**
     * Processes a video file frame by frame, extracting the largest centroid for each second.
     *
     * @param videoFile the video file to process
     * @return a list of timestamped centroid records
     * @throws IOException if the file cannot be read or decoded
     * @throws JCodecException if video codec decoding encounters an error
     */
    public List<CentroidTimestampRecord> processVideo(File videoFile) throws IOException, JCodecException {
        try (VideoFrameReader reader = new JCodecVideoFrameReader(videoFile)) {
            return processFrames(reader);
        }
    }

    /**
     * Processes frames sequentially from a VideoFrameReader.
     *
     * @param reader the frame reader source
     * @return a list of timestamped centroid records
     * @throws IOException if a frame cannot be read
     */
    public List<CentroidTimestampRecord> processFrames(VideoFrameReader reader) throws IOException {
        List<CentroidTimestampRecord> records = new ArrayList<>();
        double currentSecond = 0.0;

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

        return records;
    }
}
