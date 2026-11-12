import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class FfmpegVideoFrameGrabberTest {

    // Reading the sample video produces frames with real image data and increasing timestamps
    @Test
    void readsFramesWithIncreasingTimestamps() throws Exception {
        try (FfmpegVideoFrameGrabber grabber = new FfmpegVideoFrameGrabber("sampleInput/ball.mp4")) {
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
        try (FfmpegVideoFrameGrabber grabber = new FfmpegVideoFrameGrabber("sampleInput/ball.mp4")) {
            while (grabber.hasNext()) {
                grabber.nextFrame();
            }
            assertFalse(grabber.hasNext());
        }
    }
}
