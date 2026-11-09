import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CsvWriter implements AutoCloseable {
  private final BufferedWriter writer;

  public CsvWriter(String outputPath) throws IOException {
    writer = Files.newBufferedWriter(Path.of(outputPath));
  }

  public void writeRow(double seconds, Coordinate coordinate)
      throws IOException {
    writer.write(
        seconds + "," + coordinate.x() + "," + coordinate.y());
    writer.newLine();
  }

  @Override
  public void close() throws IOException {
    writer.close();
  }
}