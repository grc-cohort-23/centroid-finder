import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.junit.jupiter.api.Test;

public class SamplingVideoGroupFinderTest {

    // Fake grabber: hands out a preset sequence of frames, one per call to nextFrame()
    private static class StubGrabber implements VideoFrameGrabber {
        private final Deque<FrameData> frames;

        StubGrabber(List<FrameData> frames) {
            this.frames = new ArrayDeque<>(frames);
        }

        @Override
        public boolean hasNext() {
            return !frames.isEmpty();
        }

        @Override
        public FrameData nextFrame() {
            return frames.removeFirst();
        }
    }

    // Fake image group finder: returns a preset list of groups for every frame it's given
    private static class StubImageGroupFinder implements ImageGroupFinder {
        private final List<Group> toReturn;
        int calls = 0;

        StubImageGroupFinder(List<Group> toReturn) {
            this.toReturn = toReturn;
        }

        @Override
        public List<Group> findConnectedGroups(BufferedImage image) {
            calls++;
            return toReturn;
        }
    }

    private final BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);

    // An empty video produces no results
    @Test
    void emptyVideo_returnsEmptyList() {
        StubGrabber grabber = new StubGrabber(List.of());
        StubImageGroupFinder imageGroupFinder = new StubImageGroupFinder(List.of());
        SamplingVideoGroupFinder finder = new SamplingVideoGroupFinder(grabber, imageGroupFinder, 0.5);

        List<VideoResult> results = finder.findCentroids();

        assertTrue(results.isEmpty());
    }

    // A frame with a matching group reports that group's centroid
    @Test
    void frameWithGroup_reportsLargestCentroid() {
        List<Group> groups = List.of(new Group(4, new Coordinate(10, 20)));
        StubGrabber grabber = new StubGrabber(List.of(new FrameData(image, 0.0)));
        StubImageGroupFinder imageGroupFinder = new StubImageGroupFinder(groups);
        SamplingVideoGroupFinder finder = new SamplingVideoGroupFinder(grabber, imageGroupFinder, 0.5);

        List<VideoResult> results = finder.findCentroids();

        assertEquals(List.of(new VideoResult(0.0, new Coordinate(10, 20))), results);
    }

    // A frame with no matching groups reports (-1,-1), not an exception
    @Test
    void frameWithNoGroups_reportsNegativeOneCoordinate() {
        StubGrabber grabber = new StubGrabber(List.of(new FrameData(image, 0.0)));
        StubImageGroupFinder imageGroupFinder = new StubImageGroupFinder(List.of());
        SamplingVideoGroupFinder finder = new SamplingVideoGroupFinder(grabber, imageGroupFinder, 0.5);

        List<VideoResult> results = finder.findCentroids();

        assertEquals(List.of(new VideoResult(0.0, new Coordinate(-1, -1))), results);
    }

    // Frames landing exactly on each interval are all sampled
    @Test
    void framesOnEveryInterval_areAllSampled() {
        List<Group> groups = List.of(new Group(1, new Coordinate(0, 0)));
        StubGrabber grabber = new StubGrabber(List.of(
            new FrameData(image, 0.0),
            new FrameData(image, 0.5),
            new FrameData(image, 1.0)
        ));
        StubImageGroupFinder imageGroupFinder = new StubImageGroupFinder(groups);
        SamplingVideoGroupFinder finder = new SamplingVideoGroupFinder(grabber, imageGroupFinder, 0.5);

        List<VideoResult> results = finder.findCentroids();

        assertEquals(3, results.size());
        assertEquals(3, imageGroupFinder.calls);
    }

    // A frame that arrives before the next sampling target is skipped, not processed
    @Test
    void frameBeforeTarget_isSkipped() {
        List<Group> groups = List.of(new Group(1, new Coordinate(0, 0)));
        StubGrabber grabber = new StubGrabber(List.of(
            new FrameData(image, 0.0),
            new FrameData(image, 0.2),
            new FrameData(image, 0.6)
        ));
        StubImageGroupFinder imageGroupFinder = new StubImageGroupFinder(groups);
        SamplingVideoGroupFinder finder = new SamplingVideoGroupFinder(grabber, imageGroupFinder, 0.5);

        List<VideoResult> results = finder.findCentroids();

        assertEquals(List.of(0.0, 0.6), results.stream().map(VideoResult::timestamp).toList());
    }

    // A gap larger than the interval still produces a result for every frame that arrives
    // once its timestamp is past the target, since the target only advances by
    // intervalSeconds each time, not by jumping ahead to the frame's own timestamp
    @Test
    void gapBetweenTimestamps_processesEveryFrameUntilCaughtUp() {
        List<Group> groups = List.of(new Group(1, new Coordinate(0, 0)));
        StubGrabber grabber = new StubGrabber(List.of(
            new FrameData(image, 0.0),
            new FrameData(image, 3.0),
            new FrameData(image, 3.2)
        ));
        StubImageGroupFinder imageGroupFinder = new StubImageGroupFinder(groups);
        SamplingVideoGroupFinder finder = new SamplingVideoGroupFinder(grabber, imageGroupFinder, 0.5);

        List<VideoResult> results = finder.findCentroids();

        assertEquals(List.of(0.0, 3.0, 3.2), results.stream().map(VideoResult::timestamp).toList());
    }
}
