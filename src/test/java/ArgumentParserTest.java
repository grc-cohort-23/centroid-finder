import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

public class ArgumentParserTest {
    private final ArgumentParser parser = new ArgumentParser();

    private static String existingFile() throws IOException {
        Path file = Files.createTempFile("video", ".mp4");
        file.toFile().deleteOnExit();
        return file.toString();
    }

    @Test
    public void testValidArguments() throws IOException {
        String video = existingFile();

        VideoProcessorArgs args = parser.parse(new String[] {video, "out.csv", "FFA200", "164"});

        assertEquals(video, args.inputPath());
        assertEquals("out.csv", args.outputCsv());
        assertEquals(0xFFA200, args.targetColor());
        assertEquals(164, args.threshold());
    }

    @Test
    public void testColorWithHashSign() throws IOException {
        VideoProcessorArgs args = parser.parse(new String[] {existingFile(), "out.csv", "#00ff00", "10"});
        assertEquals(0x00FF00, args.targetColor());
    }

    @Test
    public void testLowercaseColor() throws IOException {
        VideoProcessorArgs args = parser.parse(new String[] {existingFile(), "out.csv", "ffa200", "10"});
        assertEquals(0xFFA200, args.targetColor());
    }

    @Test
    public void testZeroThresholdAllowed() throws IOException {
        VideoProcessorArgs args = parser.parse(new String[] {existingFile(), "out.csv", "FFA200", "0"});
        assertEquals(0, args.threshold());
    }

    @Test
    public void testNullArgs() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(null));
    }

    @Test
    public void testTooFewArgs() {
        assertThrows(IllegalArgumentException.class,
            () -> parser.parse(new String[] {"a.mp4", "out.csv", "FFA200"}));
    }

    @Test
    public