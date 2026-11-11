import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VideoFrameReaderTest {
    @Test
    void readsVideoFramesWithFractionalTimestamps() throws Exception {
        int frameCount = 0;
        boolean foundFractionalTimestamp = false;

        try (VideoFrameReader reader =
                     new VideoFrameReader(Path.of("sampleInput", "sample.mp4"))) {
            VideoFrameReader.VideoFrame frame;
            while ((frame = reader.readNext()) != null) {
                assertNotNull(frame.image());
                frameCount++;

                if (frame.seconds() % 1 != 0) {
                    foundFractionalTimestamp = true;
                }
            }
        }

        assertTrue(frameCount > 5, "Expected more than five frames from the five-second clip");
        assertTrue(foundFractionalTimestamp, "Expected a timestamp with fractional seconds");
    }
}