import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CentroidCsvWriterTest {
    @TempDir
    Path directory;

    @Test
    void writesSingleResult() throws IOException {
        Path tempFile = directory.resolve("single.csv");

        try (CentroidCsvWriter writer = new CentroidCsvWriter(tempFile.toString())) {
            writer.writeResult(
                new VideoCentroidProcessor.Result(1.0, 150, 75)
            );
        }

        String actual = Files.readString(tempFile);
        assertEquals("1.0,150,75" + System.lineSeparator(), actual);
    }

    @Test
    void writesMultipleResults() throws IOException {
        Path tempFile = directory.resolve("multiple.csv");

        try (CentroidCsvWriter writer = new CentroidCsvWriter(tempFile.toString())) {
            writer.writeResult(new VideoCentroidProcessor.Result(0.0, 100, 200));
            writer.writeResult(new VideoCentroidProcessor.Result(1.0, 110, 210));
            writer.writeResult(new VideoCentroidProcessor.Result(2.0, 120, 220));
        }

        String expected =
            "0.0,100,200" + System.lineSeparator() +
            "1.0,110,210" + System.lineSeparator() +
            "2.0,120,220" + System.lineSeparator();

        assertEquals(expected, Files.readString(tempFile));
    }

    @Test
    void writesMissingCentroid() throws IOException {
        Path tempFile = directory.resolve("missing.csv");

        try (CentroidCsvWriter writer = new CentroidCsvWriter(tempFile.toString())) {
            writer.writeResult(
                new VideoCentroidProcessor.Result(3.0, -1, -1)
            );
        }

        assertEquals(
            "3.0,-1,-1" + System.lineSeparator(),
            Files.readString(tempFile)
        );
    }
}