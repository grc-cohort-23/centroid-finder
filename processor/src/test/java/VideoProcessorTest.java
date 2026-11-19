import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import org.junit.jupiter.api.Test;

public class VideoProcessorTest {

    // ---------- fakes ----------

    /** Fake frame source: hands out the frames it was given, then empty. */
    private static class FakeFrameSource implements FrameSource {
        private final Queue<TimedFrame> frames;
        boolean closed = false;

        FakeFrameSource(List<TimedFrame> frames) {
            this.frames = new LinkedList<>(frames);
        }

        @Override
        public Optional<TimedFrame> nextFrame() {
            return Optional.ofNullable(frames.poll());
        }

        @Override
        public void close() {
            closed = true;
        }
    }

    /** Fake result writer: remembers every result it was given. */
    private static class FakeResultWriter implements ResultWriter {
        final List<TrackingResult> written = new ArrayList<>();

        @Override
        public void write(TrackingResult result) {
            written.add(result);
        }

        @Override
        public void close() {
        }
    }

    private static TimedFrame frameAt(double seconds) {
        return new TimedFrame(seconds, new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
    }

    private static CentroidTracker trackerAlwaysFinding(Coordinate where) {
        return new CentroidTracker(image -> List.of(new Group(1, where)));
    }

    // ---------- tests ----------

    @Test
    public void testProcessesEveryFrame() throws IOException {
        FakeFrameSource source = new FakeFrameSource(List.of(frameAt(0.0), frameAt(0.5), frameAt(1.0)));
        FakeResultWriter writer = new FakeResultWriter();
        VideoProcessor processor = new VideoProcessor(trackerAlwaysFinding(new Coordinate(4, 2)));

        int count = processor.process(source, writer);

        assertEquals(3, count);
        assertEquals(3, writer.written.size());
    }

    @Test
    public void testResultsKeepFrameOrderAndTimes() throws IOException {
        FakeFrameSource source = new FakeFrameSource(List.of(frameAt(0.0), frameAt(0.5), frameAt(1.0)));
        FakeResultWriter writer = new FakeResultWriter();
        VideoProcessor processor = new VideoProcessor(trackerAlwaysFinding(new Coordinate(4, 2)));

        processor.process(source, writer);

        assertEquals(0.0, writer.written.get(0).seconds(), 0.0001);
        assertEquals(0.5, writer.written.get(1).seconds(), 0.0001);
        assertEquals(1.0, writer.written.get(2).seconds(), 0.0001);
    }

    @Test
    public void testWritesTrackerResult() throws IOException {
        FakeFrameSource source = new FakeFrameSource(List.of(frameAt(0.0)));
        FakeResultWriter writer = new FakeResultWriter();
        VideoProcessor processor = new VideoProcessor(trackerAlwaysFinding(new Coordinate(7, 9)));

        processor.process(source, writer);

        assertEquals(new Coordinate(7, 9), writer.written.get(0).centroid());
    }

    @Test
    public void testFrameWithNoGroupWritesMinusOne() throws IOException {
        FakeFrameSource source = new FakeFrameSource(List.of(frameAt(0.0)));
        FakeResultWriter writer = new FakeResultWriter();
        VideoProcessor processor = new VideoProcessor(new CentroidTracker(image -> new ArrayList<>()));

        processor.process(source, writer);

        assertEquals(new Coordinate(-1, -1), writer.written.get(0).centroid());
    }

    @Test
    public void testEmptyVideoWritesNothing() throws IOException {
        FakeFrameSource source = new FakeFrameSource(List.of());
        FakeResultWriter writer = new FakeResultWriter();
        VideoProcessor processor = new VideoProcessor(trackerAlwaysFinding(new Coordinate(0, 0)));

        int count = processor.process(source, writer);

        assertEquals(0, count);
        assertTrue(writer.written.isEmpty());
    }

    @Test
    public void testDoesNotCloseSourceItself() throws IOException {
        FakeFrameSource source = new FakeFrameSource(List.of(frameAt(0.0)));
        VideoProcessor processor = new VideoProcessor(trackerAlwaysFinding(new Coordinate(0, 0)));

        processor.process(source, new FakeResultWriter());

        assertFalse(source.closed);
    }

    @Test
    public void testNullTrackerThrows() {
        assertThrows(NullPointerException.class, () -> new VideoProcessor(null));
    }
}