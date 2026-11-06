
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class VideoCsvWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void testWriteGroupsNormal() throws IOException {
        Path output = tempDir.resolve("groups.csv");

        List<String> rows = List.of(
            "0.0,10,20",
            "0.5,15,25"
        );

        VideoCsvWriter writer = new VideoCsvWriter(output.toString());
        writer.writeGroups(rows);

        assertEquals(rows, Files.readAllLines(output));
    }

    @Test
    void testWriteGroupsNoCentroid() throws IOException {
        Path output = tempDir.resolve("empty-detection.csv");

        VideoCsvWriter writer = new VideoCsvWriter(output.toString());
        writer.writeGroups(List.of("0.0,-1,-1"));

        assertEquals(
            List.of("0.0,-1,-1"),
            Files.readAllLines(output)
        );
    }

    @Test
    void testWriteGroupsEmptyList() throws IOException {
        Path output = tempDir.resolve("empty.csv");

        VideoCsvWriter writer = new VideoCsvWriter(output.toString());
        writer.writeGroups(List.of());

        assertTrue(Files.exists(output));
        assertTrue(Files.readAllLines(output).isEmpty());
    }

    @Test
    void testWriteGroupsInvalidPath() {
        Path output = tempDir.resolve("missing-folder/groups.csv");

        VideoCsvWriter writer = new VideoCsvWriter(output.toString());

        assertThrows(IOException.class, () -> {
            writer.writeGroups(List.of("0.0,10,20"));
        });
    }
}
