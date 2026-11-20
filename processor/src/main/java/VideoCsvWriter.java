import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Writes video centroid tracking results to a CSV file.
 *
 * Each row contains the timestamp in seconds followed by
 * the x and y coordinates of the largest detected centroid.
 *
 * The output destination is configured during construction.
 */
public class VideoCsvWriter {

    private final String outputPath;

    /**
     * Constructs a VideoCsvWriter with the specified output path.
     *
     * @param outputPath the destination path for the CSV file
     */
    public VideoCsvWriter(String outputPath) {
        this.outputPath = outputPath;
    }


    /**
     * Writes the provided centroid tracking results to a CSV file.
     *
     * Each string represents one frame in the format "seconds,x,y".
     * Existing file contents are overwritten.
     *
     * @param groups a list of CSV-formatted centroid results
     * @throws IOException if an error occurs while writing the file
     */
    public void writeGroups(List<String> groups) throws IOException {

        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(outputPath))) {

            for (String entry : groups) {
                writer.write(entry);
                writer.newLine();
            }
            System.out.println("Success! Groups summary saved as " + outputPath);
        }catch(IOException e){
            throw e;
        }
    }
}