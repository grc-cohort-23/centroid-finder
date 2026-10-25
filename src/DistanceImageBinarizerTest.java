import static org.junit.jupiter.api.Assertions.*;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

public class DistanceImageBinarizerTest {

// [[   |   Created BY AI   |   Tests generated without human intervention, implementation of tested file independently human made     ]]

    @Test
    void testToBinaryArrayWithFakeDistanceFinder() {
        // Create a fake ColorDistanceFinder: returns 5.0 if color matches target, 20.0 otherwise
        ColorDistanceFinder fakeFinder = (colorA, colorB) -> (colorA == colorB) ? 0.0 : 20.0;

        // Target color is 0xFF0000, threshold is 10
        DistanceImageBinarizer binarizer = new DistanceImageBinarizer(fakeFinder, 0xFF0000, 10);

        // Create a small 2x1 test image
        BufferedImage img = new BufferedImage(2, 1, BufferedImage.TYPE_INT_RGB);
        img.setRGB(0, 0, 0xFF0000); // Distance = 0.0 (< 10) -> should be 1 (white)
        img.setRGB(1, 0, 0x00FF00); // Distance = 20.0 (>= 10) -> should be 0 (black)

        int[][] binary = binarizer.toBinaryArray(img);

        assertEquals(1, binary.length);
        assertEquals(2, binary[0].length);
        assertEquals(1, binary[0][0], "Matching color should be binarized to 1");
        assertEquals(0, binary[0][1], "Distant color should be binarized to 0");
    }

    @Test
    void testToBinaryArrayThrowsOnNullImage() {
        ColorDistanceFinder fakeFinder = (a, b) -> 0.0;
        DistanceImageBinarizer binarizer = new DistanceImageBinarizer(fakeFinder, 0xFF0000, 10);

        assertThrows(NullPointerException.class, () -> {
            binarizer.toBinaryArray(null);
        });
    }

    @Test
    void testToBufferedImage() {
        int[][] binary = {
            {1, 0},
            {0, 1}
        };

        ColorDistanceFinder fakeFinder = (a, b) -> 0.0;
        DistanceImageBinarizer binarizer = new DistanceImageBinarizer(fakeFinder, 0, 10);

        BufferedImage img = binarizer.toBufferedImage(binary);

        assertEquals(2, img.getWidth());
        assertEquals(2, img.getHeight());
        // Check white (0xFFFFFF) and black (0x000000) conversions, stripping alpha channel
        assertEquals(0xFFFFFF, img.getRGB(0, 0) & 0xFFFFFF);
        assertEquals(0x000000, img.getRGB(1, 0) & 0xFFFFFF);
        assertEquals(0x000000, img.getRGB(0, 1) & 0xFFFFFF);
        assertEquals(0xFFFFFF, img.getRGB(1, 1) & 0xFFFFFF);
    }

    @Test
    void testToBufferedImageThrowsOnInvalidArray() {
        ColorDistanceFinder fakeFinder = (a, b) -> 0.0;
        DistanceImageBinarizer binarizer = new DistanceImageBinarizer(fakeFinder, 0, 10);

        assertThrows(IllegalArgumentException.class, () -> {
            binarizer.toBufferedImage(null);
        });
    }
}