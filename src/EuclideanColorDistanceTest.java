import static org.junit.jupiter.api.Assertions.*;
 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
 
public class EuclideanColorDistanceTest {
 
    private static final double DELTA = 1e-9;
 
    private EuclideanColorDistance finder;
 
    @BeforeEach
    void setUp() {
        finder = new EuclideanColorDistance();
    }
 
    // ---- Channel extraction ----
 
    @Test
    void extractsEachChannel() {
        int color = 0xAB1234;
        assertEquals(0xAB, finder.getR(color));
        assertEquals(0x12, finder.getG(color));
        assertEquals(0x34, finder.getB(color));
    }
 
    @Test
    void extractsChannelsFromBlack() {
        assertEquals(0, finder.getR(0x000000));
        assertEquals(0, finder.getG(0x000000));
        assertEquals(0, finder.getB(0x000000));
    }
 
    @Test
    void extractsChannelsFromWhite() {
        assertEquals(255, finder.getR(0xFFFFFF));
        assertEquals(255, finder.getG(0xFFFFFF));
        assertEquals(255, finder.getB(0xFFFFFF));
    }
 
    @Test
    void extractsSingleChannelColors() {
        assertEquals(255, finder.getR(0xFF0000));
        assertEquals(0, finder.getG(0xFF0000));
        assertEquals(0, finder.getB(0xFF0000));
 
        assertEquals(0, finder.getR(0x00FF00));
        assertEquals(255, finder.getG(0x00FF00));
        assertEquals(0, finder.getB(0x00FF00));
 
        assertEquals(0, finder.getR(0x0000FF));
        assertEquals(0, finder.getG(0x0000FF));
        assertEquals(255, finder.getB(0x0000FF));
    }
 
    // ---- Distance ----
 
    @Test
    void identicalColorsHaveZeroDistance() {
        assertEquals(0.0, finder.distance(0x123456, 0x123456), DELTA);
        assertEquals(0.0, finder.distance(0x000000, 0x000000), DELTA);
        assertEquals(0.0, finder.distance(0xFFFFFF, 0xFFFFFF), DELTA);
    }
 
    @Test
    void blackToWhiteIsMaximumDistance() {
        // sqrt(3 * 255^2)
        assertEquals(255 * Math.sqrt(3), finder.distance(0x000000, 0xFFFFFF), DELTA);
    }
 
    @Test
    void singleChannelDifference() {
        assertEquals(255.0, finder.distance(0xFF0000, 0x000000), DELTA);
        assertEquals(255.0, finder.distance(0x00FF00, 0x000000), DELTA);
        assertEquals(255.0, finder.distance(0x0000FF, 0x000000), DELTA);
        assertEquals(5.0, finder.distance(0x102030, 0x102035), DELTA);
    }
 
    @Test
    void twoChannelDifference() {
        // red vs blue: sqrt(255^2 + 255^2)
        assertEquals(255 * Math.sqrt(2), finder.distance(0xFF0000, 0x0000FF), DELTA);
    }
 
    @Test
    void exactPythagoreanTriple() {
        // differences (3, 4, 0) -> 5
        assertEquals(5.0, finder.distance(0x000000, 0x030400), DELTA);
    }
 
    @Test
    void resultIsNotTruncatedToInteger() {
        // differences (1, 1, 1) -> sqrt(3) = 1.732..., not 1
        assertEquals(Math.sqrt(3), finder.distance(0x000000, 0x010101), DELTA);
        // differences (10, 20, 30) -> sqrt(1400) = 37.416...
        assertEquals(Math.sqrt(1400), finder.distance(0x000000, 0x0A141E), DELTA);
    }
 
    @Test
    void distanceIsSymmetric() {
        int a = 0x1A2B3C;
        int b = 0xC3B2A1;
        assertEquals(finder.distance(a, b), finder.distance(b, a), DELTA);
    }
 
    @Test
    void channelsWithMixedDirectionDifferences() {
        // A = (200, 50, 100), B = (100, 150, 90) -> diffs (100, -100, 10)
        int a = 0xC83264;
        int b = 0x64965A;
        assertEquals(Math.sqrt(100 * 100 + 100 * 100 + 10 * 10), finder.distance(a, b), DELTA);
    }
}