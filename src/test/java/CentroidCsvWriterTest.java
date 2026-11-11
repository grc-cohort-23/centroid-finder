import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

public class CentroidCsvWriterTest {
    @Test
    void writesSingleResult() throws IOException {
        Path tempFile = Files.createTempFile("centroid-test", ".csv");

        try (CentroidCsvWriter writer = new CentroidCsvWriter(tempFile.toString())) {
            VideoCentroidProcessor.Result result = new VideoCentroidProcessor.Result
                (1.0, 150, 75);
            writer.writeResult(result);
        }

        String actual = Files.readString(tempFile);
        assertEquals("1.0,150,75" + System.lineSeparator(), actual);
        Files.deleteIfExists(tempFile);
    }

    @Test
    void writesMultipleResults() throws IOException {
        Path tempFile = Files.createTempFile("centroid-multiple", ".csv");

        try (CentroidCsvWriter writer = new CentroidCsvWriter(tempFile.toString())) {
            writer.writeResult(new VideoCentroidProcessor.Result
                (0.0, 100, 200)
            );
            writer.writeResult(new VideoCentroidProcessor.Result
                (1.0, 110, 210)
            );
            writer.writeResult(new VideoCentroidProcessor.Result
                (2.0, 120, 220)
            );
        }

        String expected = "0.0,100,200" + System.lineSeparator() + 
            "1.0,110,210" + System.lineSeparator() +
            "2.0,120,220" + System.lineSeparator();

        String actual = Files.readString(tempFile);
        assertEquals(expected, actual);
        Files.deleteIfExists(tempFile);
    }

    @Test
    void writesMissingCentroid() throws IOException {
        Path tempFile = Files.createTempFile("centroid-missing", ".csv");

        try (CentroidCsvWriter writer = new CentroidCsvWriter(tempFile.toString())) {

            VideoCentroidProcessor.Result result =
                new VideoCentroidProcessor.Result(3.0, -1, -1);

            writer.writeResult(result);
        }

        String actual = Files.readString(tempFile);

        assertEquals(
            "3.0,-1,-1" + System.lineSeparator(),
            actual
        );

        Files.deleteIfExists(tempFile);
    }
}