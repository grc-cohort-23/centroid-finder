import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CentroidCsvWriter implements AutoCloseable {
    private final PrintWriter writer;

    public CentroidCsvWriter(String outputPath) throws IOException {
        writer = new PrintWriter(new FileWriter(outputPath));
    }

    public void writeResult(VideoCentroidProcessor.Result result) {
        writer.printf("%s,%d,%d%n", 
            result.seconds(), 
            result.x(), 
            result.y()
        );
    }

    @Override
    public void close() {
        writer.close();
    }
}