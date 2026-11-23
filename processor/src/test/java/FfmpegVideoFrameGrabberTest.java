import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link FfmpegVideoFrameGrabber} that decode the real fixture video
 * {@code processor/sampleInput/ball.mp4}.
 */
public class FfmpegVideoFrameGrabberTest {

    /**
     * Returns the absolute path to the fixture video.
     *
     * The old tests used the relative path "sampleInput/ball.mp4", which only worked when
     * the JVM's working directory was processor/. Instead we start from where the compiled
     * test classes live (processor/target/test-classes) and walk up two folders to
     * processor/, so the path is correct no matter which directory the tests run from
     * (Maven, an IDE, or CI).
     *
     * @return the absolute path of processor/sampleInput/ball.mp4 as a string
     */
    static String fixtureVideoPath() {
        try {
            Path testClassesDir = Path.of(FfmpegVideoFrameGrabberTest.class
                    .getProtectionDomain().getCodeSource().getLocation().toURI());
            Path processorDir = testClassesDir.getParent().getParent();
            return processorDir.resolve("sampleInput").resolve("ball.mp4").toString();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Could not locate the test classes directory", e);
        }
    }

    // Reading the sample video produces frames with real image data and increasing timestamps
    @Test
    void readsFramesWithIncreasingTimestamps() throws Exception {
        try (FfmpegVideoFrameGrabber grabber = new FfmpegVideoFrameGrabber(fixtureVideoPath())) {
            assertTrue(grabber.hasNext());

            FrameData first = grabber.nextFrame();
            assertNotNull(first.frame());
            assertTrue(first.timestamp() >= 0.0);

            assertTrue(grabber.hasNext());
            FrameData second = grabber.nextFrame();
            assertTrue(second.timestamp() > first.timestamp());
        }
    }

    // Reading past the final frame makes hasNext() report false
    @Test
    void hasNext_returnsFalseAfterLastFrame() throws Exception {
        try (FfmpegVideoFrameGrabber grabber = new FfmpegVideoFrameGrabber(fixtureVideoPath())) {
            while (grabber.hasNext()) {
                grabber.nextFrame();
            }
            assertFalse(grabber.hasNext());
        }
    }
}
