package io.github.nadia5129.centroidfinder;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import org.junit.jupiter.api.Test;

/**
 * These tests use the real sample video in sampleInput/.
 * The balloon video is 1920x1080, 25 frames per second, and about 11 seconds long (275 frames).
 */
public class JavaCVFrameReaderTest {

    private static final String VIDEO = "sampleInput/bright_balloon_sample_video.mp4";

    // The first frame should be a real image with the video's size
    @Test
    void nextFrame_firstFrame_returnsImageWithVideoSize() throws IOException {
        try (JavaCVFrameReader reader = new JavaCVFrameReader(VIDEO)) {
            // Act
            BufferedImage frame = reader.nextFrame();

            // Assert
            assertNotNull(frame);
            assertEquals(1920, frame.getWidth());
            assertEquals(1080, frame.getHeight());
        }
    }

    // The first frame is at the very start of the video
    @Test
    void getTimestamp_firstFrame_isZero() throws IOException {
        try (JavaCVFrameReader reader = new JavaCVFrameReader(VIDEO)) {
            // Act
            reader.nextFrame();

            // Assert
            assertEquals(0.0, reader.getTimestamp(), 0.000001);
        }
    }

    // At 25 frames per second, the second frame is 1/25 = 0.04 seconds in
    @Test
    void getTimestamp_secondFrame_isOneFrameLater() throws IOException {
        try (JavaCVFrameReader reader = new JavaCVFrameReader(VIDEO)) {
            // Act
            reader.nextFrame();
            reader.nextFrame();

            // Assert
            assertEquals(0.04, reader.getTimestamp(), 0.001);
        }
    }

    // Reading the whole video: 275 frames, timestamps always going up, last one just under 11 seconds
    @Test
    void nextFrame_wholeVideo_readsAllFramesInOrder() throws IOException {
        try (JavaCVFrameReader reader = new JavaCVFrameReader(VIDEO)) {
            // Arrange
            int frameCount = 0;
            double previousTimestamp = -1;

            // Act
            while (reader.nextFrame() != null) {
                double timestamp = reader.getTimestamp();
                // each frame should come after the one before it
                assertTrue(timestamp > previousTimestamp);
                previousTimestamp = timestamp;
                frameCount++;
            }

            // Assert
            assertEquals(275, frameCount);
            assertEquals(10.96, previousTimestamp, 0.001);
        }
    }

    // After the last frame, it keeps returning null instead of crashing
    @Test
    void nextFrame_afterVideoEnds_returnsNull() throws IOException {
        try (JavaCVFrameReader reader = new JavaCVFrameReader(VIDEO)) {
            // Arrange: read until the end
            while (reader.nextFrame() != null) {
            }

            // Act + Assert
            assertNull(reader.nextFrame());
        }
    }

    // A file that doesn't exist -> IOException
    @Test
    void constructor_missingFile_throwsIOException() {
        assertThrows(IOException.class, () -> new JavaCVFrameReader("sampleInput/no_such_video.mp4"));
    }

    // A file that isn't a video -> IOException
    @Test
    void constructor_notAVideo_throwsIOException() {
        assertThrows(IOException.class, () -> new JavaCVFrameReader("sampleOutput/groups.csv"));
    }

    // JavaCVFrameReader can be used anywhere a FrameReader is expected
    @Test
    void javaCVFrameReader_isAFrameReader() throws IOException {
        try (FrameReader reader = new JavaCVFrameReader(VIDEO)) {
            assertNotNull(reader.nextFrame());
        }
    }
}
