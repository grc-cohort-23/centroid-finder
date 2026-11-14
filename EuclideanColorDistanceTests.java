 import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EuclideanColorDistanceTests {

    private final EuclideanColorDistance finder =
            new EuclideanColorDistance();

    // dark blue- 0x123456
    // red - 0xFF0000
    
        
    @Test //  test two identical colors have a distance of 0
    public void testSameColor() {
        assertEquals(0.0,
                finder.distance(0x123456, 0x123456), 0.001);
    }

    @Test // test distance between red and black
    public void testRedDifference() {
        assertEquals(255.0,
                finder.distance(0xFF0000, 0x000000), 0.001);
    }

    @Test // test multiple colors
    public void testMultipleColorChannels() {
        assertEquals(13.0,
                finder.distance(0x000000, 0x03040C), 0.001);
    }
}
