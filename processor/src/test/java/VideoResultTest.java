import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class VideoResultTest {

    // A normal centroid is formatted as timestamp,x,y with 2 decimal places
    @Test
    void toCsvRow_formatsTimestampAndCentroid() {
        VideoResult result = new VideoResult(1.5, new Coordinate(100, 200));

        assertEquals("1.50,100,200", result.toCsvRow());
    }

    // No group found is represented with coordinate (-1,-1)
    @Test
    void toCsvRow_noMatch_usesNegativeOneCoordinates() {
        VideoResult result = new VideoResult(2.0, new Coordinate(-1, -1));

        assertEquals("2.00,-1,-1", result.toCsvRow());
    }

    // Timestamp 0 is still formatted with 2 decimal places
    @Test
    void toCsvRow_zeroTimestamp_formatsCorrectly() {
        VideoResult result = new VideoResult(0.0, new Coordinate(5, 5));

        assertEquals("0.00,5,5", result.toCsvRow());
    }
}
