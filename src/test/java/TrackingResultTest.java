import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class TrackingResultTest {

    @Test
    public void testCsvRowNormal() {
        TrackingResult result = new TrackingResult(1.5, new Coordinate(412, 233));
        assertEquals("1.500,412,233", result.toCsvRow());
    }

    @Test
    public void testCsvRowRoundsDown() {
        // 1/30 = 0.03333... (one frame at 30 fps)
        TrackingResult result = new TrackingResult(1.0 / 30, new Coordinate(10, 20));
        assertEquals("0.033,10,20", result.toCsvRow());
    }

    @Test
    public void testCsvRowRoundsUp() {
        // 2/30 = 0.06666... (two frames at 30 fps)
        TrackingResult result = new TrackingResult(2.0 / 30, new Coordinate(10, 20));
        assertEquals("0.067,10,20", result.toCsvRow());
    }

    @Test
    public void testCsvRowStartOfVideo() {
        TrackingResult result = new TrackingResult(0.0, new Coordinate(0, 0));
        assertEquals("0.000,0,0", result.toCsvRow());
    }

    @Test
    public void testCsvRowNoGroupFound() {
        TrackingResult result = new TrackingResult(5.0, new Coordinate(-1, -1));
        assertEquals("5.000,-1,-1", result.toCsvRow());
    }
}