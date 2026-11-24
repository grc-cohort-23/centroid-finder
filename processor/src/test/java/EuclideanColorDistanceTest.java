import static org.junit.Assert.*;
import java.util.*;
import org.junit.Test;


public class EuclideanColorDistanceTest {
    @Test 
    public void test_EuclideanColorDistance_same_color() {
    
        EuclideanColorDistance d = new EuclideanColorDistance();

        int colorA = 0xFFFFFF;
        int colorB = 0xFFFFFF;

        double actual = d.distance(colorA, colorB);

        double expected = 0.0;

        assertEquals(expected, actual, 0.001);
    }


    @Test
    public void test_symmetry() {
        EuclideanColorDistance d = new EuclideanColorDistance();

        int colorA = 0x12AB34;
        int colorB = 0xF045C8;

        assertEquals(
            d.distance(colorA, colorB),
            d.distance(colorB, colorA),
            0.0001
        );
    }

    @Test
    public void test_maximumColorDistance() {
        EuclideanColorDistance d = new EuclideanColorDistance();

        //this is the largest value difference that should be produced
        double expected = Math.sqrt(
                            Math.pow(255 - 0, 2) +
                            Math.pow(255 - 0, 2) +
                            Math.pow(255 - 0, 2)  
                        );

        assertEquals(expected, d.distance(0x000000, 0xFFFFFF), 0.01);
    }

    @Test
    public void test_redChannelOnly() {
        EuclideanColorDistance d = new EuclideanColorDistance();

        assertEquals(255.0, d.distance(0x000000, 0xFF0000), 0.0001);
    }

    @Test
    public void test_greenChannelOnly() {
        EuclideanColorDistance d = new EuclideanColorDistance();

        assertEquals(255.0, d.distance(0x000000, 0x00FF00), 0.0001);
    }

    @Test
    public void test_blueChannelOnly() {
        EuclideanColorDistance d = new EuclideanColorDistance();

        assertEquals(255.0, d.distance(0x000000, 0x0000FF), 0.0001);
    }

}


