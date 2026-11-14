
import java.io.IOException;
import java.io.Writer;

public class CsvWriter implements AutoCloseable {

    private final Writer writer;

    public CsvWriter(Writer writer) throws IOException {
        this.writer = writer;
        writer.write("time,x,y\n");
    }

    public void writeRow(double seconds, Coordinate centroid)
            throws IOException {

        writer.write(seconds + "," +
                     centroid.x() + "," +
                     centroid.y() + "\n");
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
