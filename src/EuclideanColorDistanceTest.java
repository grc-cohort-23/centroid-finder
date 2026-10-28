import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class EuclideanColorDistanceTest {
    
    // Object created for non-static references.
    EuclideanColorDistance ecd = new EuclideanColorDistance();

    @Test 
    public void testConvertHexToR() {
        int color = 0xc84912;
        
        int actual = ecd.hexToR(color);
        int expected = 200;
        
        assertEquals(expected, actual);
    }

    @Test
    public void testConvertHexToG() {
        int color = 0xc84912;
        
        int actual = ecd.hexToG(color);
        int expected = 73;
        
        assertEquals(expected, actual);
    }

    @Test 
    public void testConvertHexToB() {
        int color = 0xc84912;
        
        int actual = ecd.hexToB(color);
        int expected = 18;
        
        assertEquals(expected, actual);
    }

    @Test
    public void testAllWhite() {
        int color = 0xffffff;

        int[] expected = new int[]{255, 255, 255};
        int[] actual = new int[]{ecd.hexToR(color), ecd.hexToG(color), ecd.hexToB(color)};

        assertArrayEquals(expected, actual);
    }

    @Test
    public void testAllBlack() {
        int color = 0x000000;

        int[] expected = new int[]{0, 0, 0};
        int[] actual = new int[]{ecd.hexToR(color), ecd.hexToG(color), ecd.hexToB(color)};

        assertArrayEquals(expected, actual);
    }

    @Test
    public void testDistanceBetweenOrangeAndTeal() {
        int orange = 0xffa500;
        int teal = 0x008080;

        double expected = Math.sqrt(Math.pow(255, 2) + Math.pow((165 - 128), 2) + Math.pow((0 - 128), 2));
        double actual = ecd.distance(orange, teal);

        assertEquals(expected, actual);
    }

    @Test
    public void testDistanceRandomColors() {
        int colorA = 0x391751;
        int colorB = 0x5a0fbc;

        double expected = Math.sqrt(Math.pow(57-90, 2) + Math.pow((23 - 15), 2) + Math.pow((81 - 188), 2));
        double actual = ecd.distance(colorA, colorB);

        assertEquals(expected, actual);
    }

    @Test
    public void testDistanceValueIsSameForwardAndBackward() {
        int orange = 0xffa500;
        int teal = 0x008080;

        double orangeFirst = ecd.distance(orange, teal); 
        double tealFirst = ecd.distance(teal, orange);

        assertEquals(orangeFirst, tealFirst);
    }

    @Test
    public void testDistanceSameColor() {
        int orange = 0xffa500;

        double expected = 0;
        double actual = ecd.distance(orange, orange);

        assertEquals(expected, actual);
    }
}
