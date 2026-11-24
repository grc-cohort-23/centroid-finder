package io.github.nadia5129.centroidfinder;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.StringWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

public class ParallelFrameProcessorTest {

    // Every test must finish within this time, so a bug that makes the processor hang fails the test
    private static final Duration TIME_LIMIT = Duration.ofSeconds(10);

    // A pretend video with `frameCount` frames. Each frame is a new small image with its
    // frame number stored in pixel (0,0). Frame n has timestamp n / 10 seconds.
    private static class NumberedFrameReader implements FrameReader {
        private final int frameCount;
        int framesRead = 0;

        NumberedFrameReader(int frameCount) {
            this.frameCount = frameCount;
        }

        @Override
        public BufferedImage nextFrame() {
            if (framesRead >= frameCount) {
                return null;
            }
            BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
            image.setRGB(0, 0, framesRead);
            framesRead++;
            return image;
        }

        @Override
        public double getTimestamp() {
            return (framesRead - 1) / 10.0;
        }

        @Override
        public void close() {
        }
    }

    // A pretend video that works like the real JavaCV reader: it draws every frame
    // into the SAME image object and returns that one object each time.
    private static class ReusingFrameReader implements FrameReader {
        final BufferedImage sharedImage = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        private final int frameCount;
        //opened once frame 1 has been drawn over frame 0
        final CountDownLatch secondFrameDrawn = new CountDownLatch(1);
        private int framesRead = 0;

        ReusingFrameReader(int frameCount) {
            this.frameCount = frameCount;
        }

        @Override
        public BufferedImage nextFrame() {
            if (framesRead >= frameCount) {
                return null;
            }
            sharedImage.setRGB(0, 0, framesRead);
            if (framesRead == 1) {
                secondFrameDrawn.countDown();
            }
            framesRead++;
            return sharedImage;
        }

        @Override
        public double getTimestamp() {
            return framesRead - 1;
        }

        @Override
        public void close() {
        }
    }

    // A pretend group finder: the centroid it returns is (frame number, frame number * 2),
    // so the CSV shows which frame each row came from.
    private static class FrameNumberGroupFinder implements ImageGroupFinder {
        @Override
        public List<Group> findConnectedGroups(BufferedImage image) {
            int n = frameNumberOf(image);
            return List.of(new Group(1, new Coordinate(n, n * 2)));
        }
    }

    // Reads back the frame number stored in pixel (0,0)
    private static int frameNumberOf(BufferedImage image) {
        return image.getRGB(0, 0) & 0xFFFFFF;
    }

    // Runs the processor and returns the CSV text it wrote
    private static String run(ParallelFrameProcessor processor, FrameReader reader) throws IOException {
        StringWriter output = new StringWriter();
        CSVOutputWriter writer = new CSVOutputWriter(output);
        processor.process(reader, writer);
        writer.close();
        return output.toString();
    }

    // The CSV we expect for frames 0 .. frameCount-1 from NumberedFrameReader + FrameNumberGroupFinder
    private static String expectedCsv(int frameCount) {
        StringBuilder csv = new StringBuilder();
        for (int n = 0; n < frameCount; n++) {
            csv.append(String.format(java.util.Locale.US, "%.3f,%d,%d\n", n / 10.0, n, n * 2));
        }
        return csv.toString();
    }

    // Frames finish in a mixed-up order (some are slow), but the rows must still be in frame order
    @Test
    void process_framesFinishOutOfOrder_writesRowsInFrameOrder() {
        // Arrange
        int frameCount = 200;
        ImageGroupFinder slowSometimes = image -> {
            //every 7th frame is slow, so later frames finish before it
            if (frameNumberOf(image) % 7 == 0) {
                sleep(3);
            }
            return new FrameNumberGroupFinder().findConnectedGroups(image);
        };
        ParallelFrameProcessor processor =
            new ParallelFrameProcessor(() -> new ImageCentroidTracker(slowSometimes), 4);

        // Act
        String csv = assertTimeoutPreemptively(TIME_LIMIT, () -> run(processor, new NumberedFrameReader(frameCount)));

        // Assert
        assertEquals(expectedCsv(frameCount), csv);
    }

    // With only 1 worker thread the result must be the same as with many
    @Test
    void process_oneThread_writesAllRowsInOrder() {
        // Arrange
        ParallelFrameProcessor processor =
            new ParallelFrameProcessor(() -> new ImageCentroidTracker(new FrameNumberGroupFinder()), 1);

        // Act
        String csv = assertTimeoutPreemptively(TIME_LIMIT, () -> run(processor, new NumberedFrameReader(25)));

        // Assert
        assertEquals(expectedCsv(25), csv);
    }

    // The reader reuses one image for every frame. The processor must copy each frame,
    // or the worker for frame 0 would see frame 1's pixels.
    @Test
    void process_readerReusesOneImage_eachWorkerSeesItsOwnFrame() {
        // Arrange
        ReusingFrameReader reader = new ReusingFrameReader(3);
        List<BufferedImage> imagesSeen = Collections.synchronizedList(new ArrayList<>());
        ImageGroupFinder waitsForSecondFrame = image -> {
            //wait until the reader has drawn frame 1 into its shared image, then look at our image
            await(reader.secondFrameDrawn);
            imagesSeen.add(image);
            return new FrameNumberGroupFinder().findConnectedGroups(image);
        };
        //1 thread -> up to 2 frames in flight, so frame 1 is read while frame 0 is still waiting
        ParallelFrameProcessor processor =
            new ParallelFrameProcessor(() -> new ImageCentroidTracker(waitsForSecondFrame), 1);

        // Act
        String csv = assertTimeoutPreemptively(TIME_LIMIT, () -> run(processor, reader));

        // Assert
        assertEquals("0.000,0,0\n1.000,1,2\n2.000,2,4\n", csv);
        for (BufferedImage image : imagesSeen) {
            assertNotSame(reader.sharedImage, image);
        }
    }

    // A worker that throws must stop the processor with an IOException that names the frame,
    // without reading the rest of the video and without hanging
    @Test
    void process_workerThrows_throwsIOExceptionAndStopsReading() {
        // Arrange
        int frameCount = 1000;
        NumberedFrameReader reader = new NumberedFrameReader(frameCount);
        ImageGroupFinder failsOnFrame5 = image -> {
            if (frameNumberOf(image) == 5) {
                throw new IllegalStateException("bad frame");
            }
            return List.of();
        };
        ParallelFrameProcessor processor =
            new ParallelFrameProcessor(() -> new ImageCentroidTracker(failsOnFrame5), 2);

        // Act
        IOException error = assertTimeoutPreemptively(TIME_LIMIT,
            () -> assertThrows(IOException.class, () -> run(processor, reader)));

        // Assert
        assertTrue(error.getMessage().contains("frame 5"), error.getMessage());
        assertTrue(error.getMessage().contains("bad frame"), error.getMessage());
        assertInstanceOf(IllegalStateException.class, error.getCause());
        assertTrue(reader.framesRead < frameCount, "it should stop reading after the error");
    }

    // A video with no frames -> an empty CSV, and no crash
    @Test
    void process_noFrames_writesNothing() {
        // Arrange
        ParallelFrameProcessor processor =
            new ParallelFrameProcessor(() -> new ImageCentroidTracker(new FrameNumberGroupFinder()), 4);

        // Act
        String csv = assertTimeoutPreemptively(TIME_LIMIT, () -> run(processor, new NumberedFrameReader(0)));

        // Assert
        assertEquals("", csv);
    }

    // Each worker thread must get its own tracker: no more trackers than threads,
    // and each tracker is only ever used by one thread
    @Test
    void process_manyFrames_eachThreadUsesItsOwnTracker() {
        // Arrange
        int threadCount = 3;
        AtomicInteger trackersMade = new AtomicInteger();
        List<Set<Thread>> threadsPerTracker = Collections.synchronizedList(new ArrayList<>());
        ParallelFrameProcessor processor = new ParallelFrameProcessor(() -> {
            trackersMade.incrementAndGet();
            Set<Thread> threadsSeen = Collections.synchronizedSet(new HashSet<>());
            threadsPerTracker.add(threadsSeen);
            return new ImageCentroidTracker(image -> {
                threadsSeen.add(Thread.currentThread());
                sleep(1);
                return List.of();
            });
        }, threadCount);

        // Act
        assertTimeoutPreemptively(TIME_LIMIT, () -> run(processor, new NumberedFrameReader(60)));

        // Assert
        assertTrue(trackersMade.get() >= 1 && trackersMade.get() <= threadCount,
            "trackers made: " + trackersMade.get());
        for (Set<Thread> threadsSeen : threadsPerTracker) {
            assertEquals(1, threadsSeen.size());
        }
    }

    // Only a few frames may be in memory at once: the reader must wait when the workers are behind
    @Test
    void process_slowWorkers_keepsAtMostTwoFramesPerThreadInFlight() {
        // Arrange
        int threadCount = 2;
        AtomicInteger framesDone = new AtomicInteger();
        AtomicInteger mostInFlight = new AtomicInteger();
        //counts how many frames were read but not finished yet, each time a frame is read
        NumberedFrameReader reader = new NumberedFrameReader(100) {
            @Override
            public BufferedImage nextFrame() {
                BufferedImage frame = super.nextFrame();
                mostInFlight.accumulateAndGet(framesRead - framesDone.get(), Math::max);
                return frame;
            }
        };
        ParallelFrameProcessor processor = new ParallelFrameProcessor(() -> new ImageCentroidTracker(image -> {
            sleep(2);
            framesDone.incrementAndGet();
            return List.of();
        }), threadCount);

        // Act
        assertTimeoutPreemptively(TIME_LIMIT, () -> run(processor, reader));

        // Assert
        assertTrue(mostInFlight.get() <= threadCount * ParallelFrameProcessor.FRAMES_PER_THREAD,
            "most frames in flight: " + mostInFlight.get());
    }

    // Less than 1 thread makes no sense
    @Test
    void constructor_zeroThreads_throwsIllegalArgumentException() {
        // Act + Assert
        assertThrows(IllegalArgumentException.class,
            () -> new ParallelFrameProcessor(() -> new ImageCentroidTracker(new FrameNumberGroupFinder()), 0));
    }

    // The copy has the same pixels and image type, but it is a different image with its own pixels
    @Test
    void copyImage_anyImage_returnsSeparateCopyWithSamePixelsAndType() {
        // Arrange
        BufferedImage original = new BufferedImage(3, 2, BufferedImage.TYPE_3BYTE_BGR);
        original.setRGB(0, 0, 0x123456);
        original.setRGB(2, 1, 0xABCDEF);

        // Act
        BufferedImage copy = ParallelFrameProcessor.copyImage(original);
        original.setRGB(0, 0, 0x000000);

        // Assert
        assertNotSame(original, copy);
        assertEquals(BufferedImage.TYPE_3BYTE_BGR, copy.getType());
        assertEquals(3, copy.getWidth());
        assertEquals(2, copy.getHeight());
        assertEquals(0x123456, copy.getRGB(0, 0) & 0xFFFFFF);
        assertEquals(0xABCDEF, copy.getRGB(2, 1) & 0xFFFFFF);
    }

    // A normal JavaCV-style frame can use the fast byte-array copy
    @Test
    void isPlainBgrImage_new3ByteBgrImage_returnsTrue() {
        // Arrange
        BufferedImage image = new BufferedImage(4, 3, BufferedImage.TYPE_3BYTE_BGR);

        // Act + Assert
        assertTrue(BgrImages.isPlainBgrImage(image));
    }

    // Other image types must use the safe copy
    @Test
    void isPlainBgrImage_intRgbImage_returnsFalse() {
        // Arrange
        BufferedImage image = new BufferedImage(4, 3, BufferedImage.TYPE_INT_RGB);

        // Act + Assert
        assertFalse(BgrImages.isPlainBgrImage(image));
    }

    // A sub-image shares its parent's byte[] and starts part-way into it, so copying
    // the whole array would copy the wrong pixels. It must use the safe copy.
    @Test
    void isPlainBgrImage_subImage_returnsFalse() {
        // Arrange
        BufferedImage parent = new BufferedImage(10, 10, BufferedImage.TYPE_3BYTE_BGR);
        BufferedImage part = parent.getSubimage(2, 3, 4, 5);

        // Act + Assert
        assertFalse(BgrImages.isPlainBgrImage(part));
    }

    // The fast copy gives exactly the same color for every pixel, in new memory
    @Test
    void copyImage_random3ByteBgrImage_copiesEveryPixelExactly() {
        // Arrange
        BufferedImage original = new BufferedImage(37, 23, BufferedImage.TYPE_3BYTE_BGR);
        java.util.Random random = new java.util.Random(42);
        for (int y = 0; y < original.getHeight(); y++) {
            for (int x = 0; x < original.getWidth(); x++) {
                original.setRGB(x, y, random.nextInt(0x1000000));
            }
        }

        // Act
        BufferedImage copy = ParallelFrameProcessor.copyImage(original);

        // Assert
        assertNotSame(original.getRaster().getDataBuffer(), copy.getRaster().getDataBuffer());
        for (int y = 0; y < original.getHeight(); y++) {
            for (int x = 0; x < original.getWidth(); x++) {
                assertEquals(original.getRGB(x, y), copy.getRGB(x, y), "pixel " + x + "," + y);
            }
        }
    }

    // A sub-image (safe copy path) still gets the right pixels
    @Test
    void copyImage_subImage_copiesOnlyThatPart() {
        // Arrange
        BufferedImage parent = new BufferedImage(10, 10, BufferedImage.TYPE_3BYTE_BGR);
        parent.setRGB(2, 3, 0x112233);
        parent.setRGB(5, 7, 0x445566);
        BufferedImage part = parent.getSubimage(2, 3, 4, 5);

        // Act
        BufferedImage copy = ParallelFrameProcessor.copyImage(part);

        // Assert
        assertEquals(4, copy.getWidth());
        assertEquals(5, copy.getHeight());
        assertEquals(0x112233, copy.getRGB(0, 0) & 0xFFFFFF);
        assertEquals(0x445566, copy.getRGB(3, 4) & 0xFFFFFF);
    }

    // No environment variable on a 15-core computer -> 14 workers (one core is for the reader)
    @Test
    void chooseThreadCount_noEnvValue_usesCoresMinusOne() {
        // Act + Assert
        assertEquals(14, ParallelFrameProcessor.chooseThreadCount(null, 15));
    }

    // On a 1-core computer there must still be 1 worker
    @Test
    void chooseThreadCount_oneCore_returnsOne() {
        // Act + Assert
        assertEquals(1, ParallelFrameProcessor.chooseThreadCount(null, 1));
    }

    // A valid environment variable wins
    @Test
    void chooseThreadCount_validEnvValue_usesEnvValue() {
        // Act + Assert
        assertEquals(3, ParallelFrameProcessor.chooseThreadCount("3", 15));
    }

    // Bad environment values are ignored
    @Test
    void chooseThreadCount_badEnvValue_usesCoresMinusOne() {
        // Act + Assert
        assertEquals(14, ParallelFrameProcessor.chooseThreadCount("abc", 15));
        assertEquals(14, ParallelFrameProcessor.chooseThreadCount("0", 15));
        assertEquals(14, ParallelFrameProcessor.chooseThreadCount("-2", 15));
    }

    // On this real computer there is always at least 1 worker
    @Test
    void chooseThreadCount_thisComputer_isAtLeastOne() {
        // Act + Assert
        assertTrue(ParallelFrameProcessor.chooseThreadCount() >= 1);
    }

    // Small helpers so the fakes above stay short
    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
