import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EuclideanColorDistanceTest {

    private final EuclideanColorDistance finder = new EuclideanColorDistance();

    @Test
    public void sameColorReturnsZero() {
        assertEquals(
                0.0,
                finder.distance(0x123456, 0x123456),
                0.000001);
    }

    @Test
    public void blackToRedReturns255() {
        assertEquals(
                255.0,
                finder.distance(0x000000, 0xFF0000),
                0.000001);
    }

    @Test
    public void blackToGreenReturns255() {
        assertEquals(255.0,
                finder.distance(0x000000, 0x00FF00), 0.000001);
    }

    @Test
    public void blackToBlueReturns255() {
        assertEquals(255.0,
                finder.distance(0x000000, 0x0000FF), 0.000001);
    }

    @Test
    public void mixedColorsReturnThirteen() {
        // RGB differences are 3, 4, and 12.
        assertEquals(13.0,
                finder.distance(0x0A141E, 0x0D182A), 0.000001);
    }

    @Test
    public void distanceCanHaveDecimals() {
        assertEquals(1.7320508075688772,
                finder.distance(0x000000, 0x010101), 0.000001);
    }

    @Test
    public void blackToWhiteReturnsMaximumDistance() {
        assertEquals(441.6729559300637,
                finder.distance(0x000000, 0xFFFFFF), 0.000001);
    }

    @Test
    public void reversedColorsReturnSameDistance() {
        assertEquals(13.0,
                finder.distance(0x0D182A, 0x0A141E), 0.000001);
    }
}