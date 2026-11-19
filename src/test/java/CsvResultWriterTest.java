import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;

public class CsvResultWriterTest {

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
}