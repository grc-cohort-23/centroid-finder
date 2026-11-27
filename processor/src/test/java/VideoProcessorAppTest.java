import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class VideoProcessorAppTest {
    @TempDir
    Path directory;

    @Test
    void failsWhenArgumentsAreMissing() {
        assertEquals(1, VideoProcessorApp.run(new String[] {}));
    }

    @Test
    void failsWhenThresholdIsNotAnInteger() {
        assertEquals(1, VideoProcessorApp.run(new String[] {
                "sampleInput/sample.mp4",
                directory.resolve("result.csv").toString(),
                "ffa200",
                "abc"
        }));
    }

    @Test
    void failsWhenColorIsNotHexadecimal() {
        assertEquals(1, VideoProcessorApp.run(new String[] {
                "sampleInput/sample.mp4",
                directory.resolve("result.csv").toString(),
                "gggggg",
                "164"
        }));
    }

    @Test
    void failsWhenVideoDoesNotExist() {
        assertEquals(1, VideoProcessorApp.run(new String[] {
                directory.resolve("missing.mp4").toString(),
                directory.resolve("result.csv").toString(),
                "ffa200",
                "164"
        }));
    }

    @Test
    void succeedsAndWritesResultsForValidVideo() throws Exception {
        Path output = directory.resolve("result.csv");

        int exitCode = VideoProcessorApp.run(new String[] {
                "sampleInput/sample.mp4",
                output.toString(),
                "ffa200",
                "164"
        });

        assertEquals(0, exitCode);
        assertTrue(Files.size(output) > 0);
    }
}