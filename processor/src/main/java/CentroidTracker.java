import java.util.List;
import java.util.Objects;

/**
 * Look at 1 video frame and find the biggest blob
 * Uses an ImageGroupFinder (from Part I) to do the actual image work.
 */
public class CentroidTracker {
    // used when we find out that a frame have no blob
    public static final Coordinate NOT_FOUND = new Coordinate(-1, -1);

    // used what we have implement in past wave
    private final ImageGroupFinder groupFinder;

    /**
     * @param groupFinder finds the groups in an image, sorted largest first
     */
    public CentroidTracker(ImageGroupFinder groupFinder) {
        // stop if no finder given
        this.groupFinder = Objects.requireNonNull(groupFinder, "groupFinder must not be null");
    }

    /**
     * Finds the centroid of the largest group in the frame.
     *
     * @param frame one video frame and its timestamp
     * @return the frame's time and the largest group's centroid, or (-1, -1) if there are no groups
     */
    public TrackingResult track(TimedFrame frame) {
        // ask for every blob in this picture
        List<Group> groups = groupFinder.findConnectedGroups(frame.image());

        // the list is sorted in descending order, so the first group is the largest
        // if empty -> no blob -> use (-1, -1)
        // if the list is empty -> NOT_FOUND
        // if the list is not empty -> use the 1st blob center
        Coordinate centroid = groups.isEmpty() ? NOT_FOUND : groups.get(0).centroid();

        return new TrackingResult(frame.seconds(), centroid);
    }
}