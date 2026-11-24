import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;
 import java.nio.file.Files;
import java.nio.file.Path;
import java.io.Writer;
import org.junit.jupiter.api.io.TempDir;

public class CsvResultWriterTest {
    @TempDir
Path tempDir;

    @Test
    public void testWritesOneLinePerResult() throws IOException {
        StringWriter sink = new StringWriter();

        try (CsvResultWriter writer = new CsvResultWriter(sink)) {
            writer.write(new TrackingResult(0.0, new Coordinate(5, 6)));
            writer.write(new TrackingResult(1.0, new Coordinate(-1, -1)));
        }

        assertEquals("0.000,5,6\n1.000,-1,-1\n", sink.toString());
    }

    @Test
    public void testNothingWrittenGivesEmptyOutput() throws IOException {
        StringWriter sink = new StringWriter();
        new CsvResultWriter(sink).close();
        assertEquals("", sink.toString());
    }

    @Test
    public void testNullWriterThrows() {
        assertThrows(NullPointerException.class, () -> new CsvResultWriter((java.io.Writer) null));
    }

    @Test
public void testWritesToActualFile() throws IOException {
    Path output = tempDir.resolve("results.csv");

    try (CsvResultWriter writer =
            new CsvResultWriter(output.toString())) {

        writer.write(
            new TrackingResult(0.0, new Coordinate(3, 4))
        );

        writer.write(
            new TrackingResult(1.0, new Coordinate(5, 6))
        );
    }

    assertEquals(
        "0.000,3,4\n1.000,5,6\n",
        Files.readString(output)
    );
}

@Test
public void testInvalidOutputPathThrows() {
    Path output = tempDir.resolve("missing/results.csv");

    assertThrows(IOException.class,
        () -> new CsvResultWriter(output.toString()));
}

@Test
public void testClosePropagatesWriteFailure() throws IOException {
    Writer failingWriter = new Writer() {
        @Override
        public void write(char[] data, int offset, int length)
                throws IOException {
            throw new IOException("Write failed");
        }

        @Override
        public void flush() {}

        @Override
        public void close() {}
    };

    CsvResultWriter writer = new CsvResultWriter(failingWriter);

    writer.write(
        new TrackingResult(0.0, new Coordinate(1, 2))
    );

    assertThrows(IOException.class, writer::close);
}
}