package video;

import java.util.List;

import image.Coordinate;
import image.Group;
import image.ImageGroupFinder;

public class LargestCentroidLocator {
    private final ImageGroupFinder groupFinder;

    public LargestCentroidLocator(ImageGroupFinder groupFinder) {
        this.groupFinder = groupFinder;
    }

    public TimedGroup locate(TimedFrame frame) {
        List<Group> groups = groupFinder.findConnectedGroups(frame.image());
        if (groups == null || groups.isEmpty()) {
            // no centroid found
            return new TimedGroup(frame.seconds(), new Group(0, new Coordinate(-1, -1)));
        }
        // groups are sorted largest-first
        return new TimedGroup(frame.seconds(), groups.get(0));
    }
}
