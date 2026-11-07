import java.util.Locale;

public record TrackingResult(double seconds, Coordinate centroid) {

    /** Formats this result as one CSV line: seconds,x,y */

    public String toCsvRow() {

  // Locale.ROOT make sure decimals points is always period (.) not , -> break CSV
  //%.3f print seconds with 3 decimals (for example: 0.0333333333 will be 0.033)
        return String.format(Locale.ROOT, "%.3f,%d,%d", seconds, centroid.x(), centroid.y());
    }
}
