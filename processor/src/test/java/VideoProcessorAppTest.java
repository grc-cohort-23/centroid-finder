
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class VideoProcessorAppTest {

    @TempDir
    Path tempDir;

    @Test
    void testMissingArguments() {
        String[] args = {"video.mp4"};

        String output = captureErrors(args);

        assertTrue(output.contains("Usage:"));
    }

    @Test
    void testInvalidHexColor() {
        String[] args = {
            "video.mp4", "output.csv", "NOTHEX", "50"
        };

        String output = captureErrors(args);

        assertTrue(output.contains("Invalid hex target color"));
    }

    // Helper method to capture System.err output.
    private String captureErrors(String[] args) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;

        try (PrintStream capture = new PrintStream(output)) {
            System.setErr(capture);

            VideoProcessorApp.main(args);

        } finally {
            System.setErr(originalErr);
        }

        return output.toString();
    }
}
