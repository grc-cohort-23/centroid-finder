import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/*
* This class saves information about tracked objects 
into a CSV file. A CSV file is like a simple spreadsheet 
that stores information in rows and columns.
 */

// writes one TrackingResult per line: seconds,x,y 
public class CsvResultWriter implements ResultWriter {
    private final Writer out;
// Creates a file where the tracking information will be saved.
    public CsvResultWriter(String path) throws IOException {
        this(Files.newBufferedWriter(Path.of(path)));
    }

    // pass a StringWriter. 
    public CsvResultWriter(Writer out) {
        this.out = new BufferedWriter(Objects.requireNonNull(out, "out must not be null"));
    }

    // Takes tracking information and saves it as a new line in the CSV file.
    @Override
    public void write(TrackingResult result) throws IOException {
        out.write(result.toCsvRow());
        out.write("\n");
    }

    // Finishes writing and closes the file when the program is done.
    @Override
    public void close() throws IOException {
        out.close();  
    }
}