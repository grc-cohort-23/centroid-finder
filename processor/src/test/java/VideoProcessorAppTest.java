
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class VideoProcessorAppTest {

    @Test
    void testMissingArguments() {
        String[] args = {"video.mp4"};

        int exitCode = VideoProcessorApp.run(args);

        assertEquals(1, exitCode);
    }

    @Test
    void testInvalidHexColor() {
        String[] args = {
            "video.mp4", "output.csv", "NOTHEX", "50"
        };

        int exitCode = VideoProcessorApp.run(args);

        assertEquals(1, exitCode);
    }

    @Test
    void testInvalidThreshold() {
        String[] args = {
            "video.mp4", "output.csv", "FF0000", "banana"
        };

        int exitCode = VideoProcessorApp.run(args);

        assertEquals(1, exitCode);
    }
}
