package io.github.nadia5129.centroidfinder;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class VideoSummaryAppTest {

    // A pretend video: gives back the images it was handed, one per call.
    // Each frame is 1 second after the one before it, starting at 0.
    private static class FakeFrameReader implements FrameReader {
        private final List<BufferedImage> frames;
        private int nextIndex = 0;

        FakeFrameReader(List<BufferedImage> frames) {
            this.frames = frames;
        }

        @Override
        public BufferedImage nextFrame() {
            if (nextIndex >= frames.size()) {
                return null;
            }
            return frames.get(nextIndex++);
        }

        @Override
        public double getTimestamp() {
            return nextIndex - 1;
        }

        @Override
        public void close() {
        }
    }

    // A pretend video that fails when asked for a frame, like a corrupted file
    private static class BrokenFrameReader extends FakeFrameReader {
        BrokenFrameReader() {
            super(List.of());
        }

        @Override
        public BufferedImage nextFrame() {
            throw new RuntimeException("video is broken");
        }
    }

    // A pretend group finder: gives back prepared groups, one list per call, in order
    private static class FakeGroupFinder implements ImageGroupFinder {
        private final List<List<Group>> results;
        final List<BufferedImage> imagesSeen = new ArrayList<>();

        FakeGroupFinder(List<List<Group>> results) {
            this.results = results;
        }

        @Override
        public List<Group> findConnectedGroups(BufferedImage image) {
            imagesSeen.add(image);
            return results.get(imagesSeen.size() - 1);
        }
    }

    // Small blank images. The fake group finder ignores what is in them.
    private BufferedImage blankImage() {
        return new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
    }

    // Runs the loop with fakes and returns the CSV text it wrote
    private String runLoop(FrameReader reader, ImageGroupFinder groupFinder) throws IOException {
        StringWriter output = new StringWriter();
        CSVOutputWriter writer = new CSVOutputWriter(output);
        VideoSummaryApp.processFrames(reader, new ImageCentroidTracker(groupFinder), writer);
        writer.close();
        return output.toString();
    }

    // One frame with one group -> one row with its timestamp and centroid
    @Test
    void processFrames_oneFrameOneGroup_writesOneRow() throws IOException {
        // Arrange
        FrameReader reader = new FakeFrameReader(List.of(blankImage()));
        ImageGroupFinder groupFinder = new FakeGroupFinder(List.of(
            List.of(new Group(10, new Coordinate(5, 7)))));

        // Act
        String csv = runLoop(reader, groupFinder);

        // Assert
        assertEquals("0.000,5,7\n", csv);
    }

    // Each frame gets its own row, in video order, with its own timestamp
    @Test
    void processFrames_manyFrames_writesOneRowPerFrameInOrder() throws IOException {
        // Arrange
        FrameReader reader = new FakeFrameReader(List.of(blankImage(), blankImage(), blankImage()));
        ImageGroupFinder groupFinder = new FakeGroupFinder(List.of(
            List.of(new Group(10, new Coordinate(1, 2))),
            List.of(new Group(10, new Coordinate(3, 4))),
            List.of(new Group(10, new Coordinate(5, 6)))));

        // Act
        String csv = runLoop(reader, groupFinder);

        // Assert
        assertEquals("0.000,1,2\n1.000,3,4\n2.000,5,6\n", csv);
    }

    // No groups in a frame -> -1,-1 for that frame, and the video keeps going
    @Test
    void processFrames_frameWithNoGroups_writesNegativeOnesAndContinues() throws IOException {
        // Arrange
        FrameReader reader = new FakeFrameReader(List.of(blankImage(), blankImage(), blankImage()));
        ImageGroupFinder groupFinder = new FakeGroupFinder(List.of(
            List.of(new Group(10, new Coordinate(1, 2))),
            List.of(),
            List.of(new Group(10, new Coordinate(5, 6)))));

        // Act
        String csv = runLoop(reader, groupFinder);

        // Assert
        assertEquals("0.000,1,2\n1.000,-1,-1\n2.000,5,6\n", csv);
    }

    // Many groups in a frame -> only the first (largest) one is written
    @Test
    void processFrames_frameWithManyGroups_writesOnlyLargest() throws IOException {
        // Arrange
        FrameReader reader = new FakeFrameReader(List.of(blankImage()));
        ImageGroupFinder groupFinder = new FakeGroupFinder(List.of(List.of(
            new Group(50, new Coordinate(8, 9)),
            new Group(3, new Coordinate(1, 1)))));

        // Act
        String csv = runLoop(reader, groupFinder);

        // Assert
        assertEquals("0.000,8,9\n", csv);
    }

    // A video with no frames -> an empty CSV, and no crash
    @Test
    void processFrames_noFrames_writesNothing() throws IOException {
        // Arrange
        FrameReader reader = new FakeFrameReader(List.of());
        ImageGroupFinder groupFinder = new FakeGroupFinder(List.of());

        // Act
        String csv = runLoop(reader, groupFinder);

        // Assert
        assertEquals("", csv);
    }

    // The loop must give the group finder the actual frames from the video, in order
    @Test
    void processFrames_manyFrames_passesEachFrameToGroupFinder() throws IOException {
        // Arrange
        BufferedImage first = blankImage();
        BufferedImage second = blankImage();
        FrameReader reader = new FakeFrameReader(List.of(first, second));
        FakeGroupFinder groupFinder = new FakeGroupFinder(List.of(List.of(), List.of()));

        // Act
        runLoop(reader, groupFinder);

        // Assert
        assertEquals(2, groupFinder.imagesSeen.size());
        assertSame(first, groupFinder.imagesSeen.get(0));
        assertSame(second, groupFinder.imagesSeen.get(1));
    }

    // A problem reading the video should not be hidden by the loop
    @Test
    void processFrames_readerFails_passesProblemAlong() {
        // Arrange
        FrameReader reader = new BrokenFrameReader();
        ImageGroupFinder groupFinder = new FakeGroupFinder(List.of());

        // Act + Assert
        assertThrows(RuntimeException.class, () -> runLoop(reader, groupFinder));
    }
}
