import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Writes a list of VideoResult rows out to a CSV file.
 */
public class VideoResultWriter {
    private final String outputPath;

    /**
     * Constructs a VideoResultWriter that writes to the given output file path.
     *
     * @param outputPath the path of the CSV file to write results to
     */
    public VideoResultWriter(String outputPath) {
        this.outputPath = outputPath;
    }

    /**
     * Writes each VideoResult's CSV row to the output file, one per line, in the
     * order given.
     *
     * @param results the list of VideoResult rows to write
     * @throws FileNotFoundException if the output path cannot be opened for writing
     */
    public void writeResults(List<VideoResult> results) throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter(outputPath)) {
            for (VideoResult result : results) {
                writer.println(result.toCsvRow());
            }
        }
    }
}
