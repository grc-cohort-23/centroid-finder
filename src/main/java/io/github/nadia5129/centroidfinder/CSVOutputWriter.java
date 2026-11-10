package io.github.nadia5129.centroidfinder;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Locale;

/**
 * Writes the video results to a CSV file, one row per frame, in the format:
 * seconds,x,y
 *
 * Seconds always have 3 decimal places (example: 1.500,320,180).
 * There is no header row, matching sampleOutput/groups.csv.
 *
 * Implements AutoCloseable so it can be used in a try-with-resources block,
 * which closes the file automatically.
 */
public class CSVOutputWriter implements AutoCloseable {

    private final PrintWriter writer;

    //Opens (or creates) the CSV file at the given path
    public CSVOutputWriter(String outputPath) throws IOException {
        this(new FileWriter(outputPath));
    }

    //Writes to any Writer. Tests pass in a StringWriter so no real file is needed.
    public CSVOutputWriter(Writer output) {
        this.writer = new PrintWriter(output);
    }

    //Writes one row: seconds (3 decimals), x, y
    public void writeRow(double seconds, Coordinate centroid) {
        //Locale.US makes sure the decimal point is a "." and not a "," on computers set to other languages
        writer.printf(Locale.US, "%.3f,%d,%d\n", seconds, centroid.x(), centroid.y());
    }

    //Saves everything to the file and closes it
    @Override
    public void close() {
        writer.close();
    }
}
