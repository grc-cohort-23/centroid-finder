import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class EuclideanColorDistanceTest {
    private final ColorDistanceFinder finder = new EuclideanColorDistance();

    //black 0x000000
    // white 0xFFFFFF
    // red 0xFF0000
    // blue 0x0000FF

    // same color test
    @Test
    public void testSameColorIsZero() {
        assertEquals(0.0, finder.distance(0xFFFFFF, 0xFFFFFF), 0.0001);    
}

    
    // Red to black expected
    @Test 
    public void testRedToBlack(){
        assertEquals(255.0,finder.distance(0xFF0000, 0x00000));
    }

    @Test 
    // AI used
    public void testBlackToWhite(){
        assertEquals(441.6729559300637,finder.distance(0x000000, 0xFFFFFF));
    }

    @Test 
    // AI used
    public void testGreenToBlack(){
        assertEquals(255.0,finder.distance(0x00FF00, 0x000000));
    }

    @Test
    // AI used
    public void testBlueToBlack() {
        assertEquals(255.0, finder.distance(0x0000FF, 0x000000), 0.0001);
    }

    @Test
    // AI used
    public void testRedToBlue() {
        assertEquals(360.62445840513925, finder.distance(0xFF0000, 0x0000FF), 0.0001);
    }

    @Test
    // AI used
    public void testMixedColors() {
        assertEquals(99.61425600786265, finder.distance(0x123456, 0x654321), 0.0001);
    }

    @Test
    // AI used
    // swapping the two colors should give the same distance
    public void testSymmetric() {
        assertEquals(finder.distance(0x123456, 0x654321), finder.distance(0x654321, 0x123456), 0.0001);
    }


}