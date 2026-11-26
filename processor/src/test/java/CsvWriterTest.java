import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CsvWriterTest {

  @TempDir
  Path tempDir;

  @Test
  public void testWritesRows() throws IOException {
    Path output = tempDir.resolve("output.csv");

    try (CsvWriter writer = new CsvWriter(output.toString())) {
      writer.writeRow(0.0, new Coordinate(8, 6));
      writer.writeRow(0.5, new Coordinate(-1, -1));
    }

    List<String> rows = Files.readAllLines(output);

    assertEquals(List.of("0.0,8,6", "0.5,-1,-1"), rows);
  }
}