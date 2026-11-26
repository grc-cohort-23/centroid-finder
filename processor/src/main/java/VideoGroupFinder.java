import java.util.List;

/**
 * An interface for finding the largest centroid in each sampled frame of a video.
 */
public interface VideoGroupFinder {

    /**
     * Finds the largest centroid in each sampled frame of a video.
     *
     * @return a list of VideoResult, one per sampled frame, in the order the frames occur
     */
    public List<VideoResult> findCentroids();
}
