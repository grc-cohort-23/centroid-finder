import java.util.Locale;

/**
 * Represents the largest centroid found in a single sampled video frame.
 * Represents one row to be written to the output CSV.
 *
 * If no group was found in that frame, centroid should be Coordinate(-1, -1).
 */
public record VideoResult(double timestamp, Coordinate centroid) {

    /**
     * Returns a string representing this result in comma-separated values (CSV) format.
     * The format is "timestamp,x,y", where timestamp is seconds since the start of the
     * video (formatted with 2 decimal places) and x, y are the centroid coordinates.
     *
     * @return a CSV row string representing the timestamp and centroid coordinates
     */
    public String toCsvRow() {
        // Locale.US keeps the decimal point as "." regardless of the machine's locale.
        return String.format(Locale.US, "%.2f,%d,%d", this.timestamp(), this.centroid().x(), this.centroid().y());
    }
}
