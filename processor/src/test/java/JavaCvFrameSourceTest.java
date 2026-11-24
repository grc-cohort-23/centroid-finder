
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class JavaCvFrameSourceTest {

    @TempDir
    Path tempDir;

    @Test
    public void testZeroStepThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> new JavaCvFrameSource("sample.mp4", 0));
    }

    @Test
    public void testNegativeStepThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> new JavaCvFrameSource("sample.mp4", -1));
    }

    @Test
    public void testMissingVideoThrows() {
        Path missing = tempDir.resolve("missing.mp4");

        assertThrows(IOException.class, () -> {
            try (JavaCvFrameSource source =
                    new JavaCvFrameSource(missing.toString(), 1.0)) {
            }
        });
    }

    @Test
    public void testInvalidVideoThrows() throws IOException {
        Path invalid = tempDir.resolve("invalid.mp4");
        Files.writeString(invalid, "This is not a video.");

        assertThrows(IOException.class, () -> {
            try (JavaCvFrameSource source =
                    new JavaCvFrameSource(invalid.toString(), 1.0)) {
            }
        });
    }
}
