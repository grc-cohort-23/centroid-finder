import java.util.Collections;
import java.util.List;

public class VideoCentroidProcessor {
    public record Result(double seconds, int x, int y) {}

    private final ImageGroupFinder groupFinder;

    public VideoCentroidProcessor(ImageGroupFinder groupFinder) {
        this.groupFinder = groupFinder;
    }

    public Result process(VideoFrameReader.VideoFrame frame) {
        List<Group> groups = groupFinder.findConnectedGroups(frame.image());

        if (groups.isEmpty()) {
            return new Result(frame.seconds(), -1, -1);
        }

        Coordinate center = Collections.max(groups).centroid();
        return new Result(frame.seconds(), center.x(), center.y());
    }
}