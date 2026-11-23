import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of the VideoGroupFinder interface that samples frames out of a video
 * at a fixed time interval and finds the largest centroid in each sampled frame.
 *
 * This class reads frames from a VideoFrameGrabber and only processes a frame once its
 * timestamp reaches the next sampling target, since video frames do not always land on
 * exact interval boundaries. Each processed frame is passed to an ImageGroupFinder, which
 * returns its connected groups sorted largest first; the largest group's centroid becomes
 * that frame's result. If no group is found, the result's centroid is (-1, -1).
 */
public class SamplingVideoGroupFinder implements VideoGroupFinder {
    private final VideoFrameGrabber grabber;
    private final ImageGroupFinder imageGroupFinder;
    private final double intervalSeconds;

    /**
     * Constructs a SamplingVideoGroupFinder using the given frame source, image group
     * finder, and sampling interval.
     *
     * @param grabber the VideoFrameGrabber to pull frames from
     * @param imageGroupFinder the ImageGroupFinder used to find groups in a sampled frame
     * @param intervalSeconds the minimum number of seconds between sampled frames
     */
    public SamplingVideoGroupFinder(VideoFrameGrabber grabber, ImageGroupFinder imageGroupFinder, double intervalSeconds) {
        this.grabber = grabber;
        this.imageGroupFinder = imageGroupFinder;
        this.intervalSeconds = intervalSeconds;
    }

    /**
     * Finds the largest centroid in each sampled frame of the video.
     *
     * Walks the video's frames in order, processing a frame once its timestamp is greater
     * than or equal to the next sampling target, then advances the target by
     * intervalSeconds. The target advances relative to itself rather than to the frame's
     * actual timestamp, so sampling stays close to even intervals even when frame
     * timestamps have gaps.
     *
     * @return a list of VideoResult, one per sampled frame, in the order the frames occur
     */
    @Override
    public List<VideoResult> findCentroids() {
        List<VideoResult> results = new ArrayList<>();
        double target = 0.0;

        while (grabber.hasNext()) {
            FrameData current = grabber.nextFrame();
            if (current.timestamp() < target) {
                continue;
            }

            List<Group> groups = imageGroupFinder.findConnectedGroups(current.frame());

            Coordinate centroid = groups.isEmpty() ? new Coordinate(-1, -1) : groups.get(0).centroid();
            results.add(new VideoResult(current.timestamp(), centroid));

            target += intervalSeconds;
        }

        return results;
    }
}
