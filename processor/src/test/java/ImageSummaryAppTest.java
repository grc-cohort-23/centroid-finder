import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ImageSummaryAppTest {
    @TempDir
    Path directory;

    @Test
    void reportsUnsupportedImageWithoutCrashing() throws IOException {
        Path input = directory.resolve("invalid.txt");
        Files.writeString(input, "This is not an image.");

        ByteArrayOutputStream errors = new ByteArrayOutputStream();
        PrintStream originalError = System.err;

        try (PrintStream capturedError =
                new PrintStream(errors, true, StandardCharsets.UTF_8)) {
            System.setErr(capturedError);

            assertDoesNotThrow(() -> ImageSummaryApp.main(new String[] {
                input.toString(), "FFA200", "164"
            }));
        } finally {
            System.setErr(originalError);
        }

        assertTrue(
            errors.toString(StandardCharsets.UTF_8)
                .contains("Unsupported or invalid image: " + input)
        );
    }
}