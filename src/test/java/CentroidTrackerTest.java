import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class CentroidTrackerTest {

    private static TimedFrame frameAt(double seconds) {
        return new TimedFrame(seconds, new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
    }

    @Test
    public void testPicksFirstGroupAsLargest() {
        List<Group> groups = List.of(
            new Group(10, new Coordinate(5, 7)),
            new Group(3, new Coordinate(1, 1))
        );
        ImageGroupFinder finder = image -> groups;
        CentroidTracker tracker = new CentroidTracker(finder);

        TrackingResult result = tracker.track(frameAt(2.0));

        assertEquals(new Coordinate(5, 7), result.centroid());
    }

    @Test
    public void testNoGroupsGivesMinusOne() {
        ImageGroupFinder finder = image -> new ArrayList<>();
        CentroidTracker tracker = new CentroidTracker(finder);

        TrackingResult result = tracker.track(frameAt(2.0));

        assertEquals(new Coordinate(-1, -1), result.centroid());
    }

    @Test
    public void testKeepsFrameTime() {
        ImageGroupFinder finder = image -> List.of(new Group(1, new Coordinate(0, 0)));
        CentroidTracker tracker = new CentroidTracker(finder);

        TrackingResult result = tracker.track(frameAt(3.25));

        assertEquals(3.25, result.seconds(), 0.0001);
    }

    @Test
    public void testKeepsFrameTimeWhenNoGroups() {
        ImageGroupFinder finder = image -> new ArrayList<>();
        CentroidTracker tracker = new CentroidTracker(finder);

        TrackingResult result = tracker.track(frameAt(7.5));

        assertEquals(7.5, result.seconds(), 0.0001);
    }

    @Test
    public void testPassesFrameImageToFinder() {
        List<BufferedImage> imagesSeen = new ArrayList<>();
        ImageGroupFinder finder = image -> {
            imagesSeen.add(image);
            return new ArrayList<>();
        };
        CentroidTracker tracker = new CentroidTracker(finder);
        TimedFrame frame = frameAt(0.0);

        tracker.track(frame);

        assertEquals(1, imagesSeen.size());
        assertSame(frame.image(), imagesSeen.get(0));
    }

    @Test
    public void testNullFinderThrows() {
        assertThrows(NullPointerException.class, () -> new CentroidTracker(null));
    }

    @Test
    public void testWithRealClassesFindsRedSquare() {
        int red = 0xFF0000;
        ImageGroupFinder finder = new BinarizingImageGroupFinder(
            new DistanceImageBinarizer(new EuclideanColorDistance(), red, 10),
            new DfsBinaryGroupFinder()
        );
        CentroidTracker tracker = new CentroidTracker(finder);

        BufferedImage image = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);
        image.setRGB(3, 1, red);
        image.setRGB(4, 1, red);
        image.setRGB(3, 2, red);
        image.setRGB(4, 2, red);
        image.setRGB(0, 4, red);

        TrackingResult result = tracker.track(new TimedFrame(1.0, image));

        assertEquals(new Coordinate(3, 1), result.centroid());
    }
}