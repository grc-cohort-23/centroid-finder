package io.github.akurfurst.centroidFinder;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.junit.jupiter.api.Test;

class VideoProcessorHelperTest {

    /** Serves a fixed list of frames, each with a timestamp, in order. */
    private static class FakeFrameGrabber extends FrameGrabber {
        private final Deque<Frame> frames = new ArrayDeque<>();
        private final Deque<Long> timestamps = new ArrayDeque<>();
        private long currentTimestamp;

        FakeFrameGrabber(List<Frame> frameList, long[] timestampValues) {
            for (int i = 0; i < frameList.size(); i++) {
                frames.add(frameList.get(i));
                timestamps.add(timestampValues[i]);
            }
        }

        @Override
        public void start() {}

        @Override
        public void stop() {}

        @Override
        public void trigger() {}

        @Override
        public void release() {}

        @Override
        public Frame grab() {
            if (frames.isEmpty()) {
                return null;
            }
            currentTimestamp = timestamps.poll();
            return frames.poll();
        }

        @Override
        public long getTimestamp() {
            return currentTimestamp;
        }
    }

    /** Returns a pre-set list of groups for each call, in order. */
    private static class FakeImageGroupFinder implements ImageGroupFinder {
        private final Deque<List<Group>> results;
        final List<BufferedImage> receivedImages = new ArrayList<>();

        FakeImageGroupFinder(List<List<Group>> results) {
            this.results = new ArrayDeque<>(results);
        }

        @Override
        public List<Group> findConnectedGroups(BufferedImage image) {
            receivedImages.add(image);
            return results.poll();
        }
    }

    private static Frame blankFrame(Java2DFrameConverter converter) throws Exception {
        BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_3BYTE_BGR);

        return converter.convert(image);
    }

    @Test
    void returnsLargestCentroidWithTimestampForEachFrame() throws Exception {
        Java2DFrameConverter converter = new Java2DFrameConverter();
        List<Frame> frames = List.of(blankFrame(converter), blankFrame(converter));
        FakeFrameGrabber video = new FakeFrameGrabber(frames, new long[] { 0L, 1_000_000L });
        converter.close();

        FakeImageGroupFinder finder = new FakeImageGroupFinder(List.of(
            List.of(new Group(10, new Coordinate(3, 4)), new Group(2, new Coordinate(0, 0))),
            List.of(new Group(5, new Coordinate(7, 8)))
        ));

        VideoProcessorHelper helper = new VideoProcessorHelper(finder);
        List<CentroidAtTime> result = helper.processVideo(video);

        assertEquals(2, result.size());
        assertEquals(new CentroidAtTime(new Coordinate(3, 4), 0L), result.get(0));
        assertEquals(new CentroidAtTime(new Coordinate(7, 8), 1_000_000L), result.get(1));
        assertEquals(2, finder.receivedImages.size());
    }

    @Test
    void recordsMinusOneCoordinatesWhenFrameHasNoGroups() throws Exception {
        Java2DFrameConverter converter = new Java2DFrameConverter();
        List<Frame> frames = List.of(blankFrame(converter));
        FakeFrameGrabber video = new FakeFrameGrabber(frames, new long[] { 2_000_000L });
        converter.close();

        FakeImageGroupFinder finder = new FakeImageGroupFinder(List.of(List.of()));

        VideoProcessorHelper helper = new VideoProcessorHelper(finder);
        List<CentroidAtTime> result = helper.processVideo(video);

        assertEquals(1, result.size());
        assertEquals(new CentroidAtTime(new Coordinate(-1, -1), 2_000_000L), result.get(0));
    }
}
