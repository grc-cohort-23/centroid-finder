package io.github.tonymythical.centroidFinder;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

public class VideoCentroidTrackerTest {

    private static class StubFrameReader implements VideoFrameReader {
        private final List<BufferedImage> frames;
        private int index = 0;

        StubFrameReader(List<BufferedImage> frames) {
            this.frames = frames;
        }

        @Override
        public BufferedImage nextFrame() throws IOException {
            if (index < frames.size()) {
                return frames.get(index++);
            }
            return null;
        }

        @Override
        public void close() throws IOException {
        }
    }

    @Test
    void testProcessFramesWithCentroidsAndEmptyFrames() throws IOException {
        BufferedImage frame1 = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        BufferedImage frame2 = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        BufferedImage frame3 = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);

        List<BufferedImage> frames = List.of(frame1, frame2, frame3);
        StubFrameReader reader = new StubFrameReader(frames);

        ImageGroupFinder stubFinder = (image) -> {
            if (image == frame1) {
                return List.of(new Group(5, new Coordinate(3, 4)));
            } else if (image == frame2) {
                return Collections.emptyList();
            } else {
                return List.of(new Group(12, new Coordinate(7, 8)), new Group(2, new Coordinate(1, 1)));
            }
        };

        VideoCentroidTracker tracker = new VideoCentroidTracker(stubFinder);
        List<CentroidTimestampRecord> records = tracker.processFrames(reader);

        assertEquals(3, records.size());

        // Frame 1 at 0.0s -> centroid (3, 4)
        assertEquals(0.0, records.get(0).timestampSeconds(), 0.001);
        assertEquals(3, records.get(0).x());
        assertEquals(4, records.get(0).y());

        // Frame 2 at 1.0s -> no group found -> (-1, -1)
        assertEquals(1.0, records.get(1).timestampSeconds(), 0.001);
        assertEquals(-1, records.get(1).x());
        assertEquals(-1, records.get(1).y());

        // Frame 3 at 2.0s -> largest group centroid (7, 8)
        assertEquals(2.0, records.get(2).timestampSeconds(), 0.001);
        assertEquals(7, records.get(2).x());
        assertEquals(8, records.get(2).y());
    }

    @Test
    void testProcessFramesEmptyVideo() throws IOException {
        StubFrameReader reader = new StubFrameReader(Collections.emptyList());
        ImageGroupFinder stubFinder = (img) -> Collections.emptyList();

        VideoCentroidTracker tracker = new VideoCentroidTracker(stubFinder);
        List<CentroidTimestampRecord> records = tracker.processFrames(reader);

        assertTrue(records.isEmpty());
    }
}
