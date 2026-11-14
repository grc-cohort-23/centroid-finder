
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class VideoProcessorOptionsTest {

    @Test
    void testValidArguments() {
        String[] args = {
            "input.mp4", "output.csv", "FF0000", "50"
        };

        VideoProcessorOptions options =
            new VideoProcessorOptions(args);

        assertEquals("input.mp4", options.getInputPath());
        assertEquals("output.csv", options.getOutputPath());
        assertEquals(0xFF0000, options.getTargetColor());
        assertEquals(50, options.getThreshold());
    }

    @Test
    void testMissingArguments() {
        String[] args = {"input.mp4"};

        assertThrows(
            IllegalArgumentException.class,
            () -> new VideoProcessorOptions(args)
        );
    }

    @Test
    void testInvalidColor() {
        String[] args = {
            "input.mp4", "output.csv", "INVALID", "50"
        };

        assertThrows(
            IllegalArgumentException.class,
            () -> new VideoProcessorOptions(args)
        );
    }

    @Test
    void testInvalidThreshold() {
        String[] args = {
            "input.mp4", "output.csv", "FF0000", "abc"
        };

        assertThrows(
            IllegalArgumentException.class,
            () -> new VideoProcessorOptions(args)
        );
    }
}
