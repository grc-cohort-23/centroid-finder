import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

// writes one TrackingResult per line: seconds,x,y 
public class CsvResultWriter implements ResultWriter {
    private final Writer out;

    public CsvResultWriter(String path) throws IOException {
        this(Files.newBufferedWriter(Path.of(path)));
    }

    // pass a StringWriter. 
    public CsvResultWriter(Writer out) {
        this.out = new BufferedWriter(Objects.requireNonNull(out, "out must not be null"));
    }

    @Override
    public void write(TrackingResult result) throws IOException {
        out.write(result.toCsvRow());
        out.write("\n");
    }

    @Override
    public void close() throws IOException {
        out.close();  
    }
}