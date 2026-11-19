import static org.junit.jupiter.api.Assertions.*;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;


public class DistanceImageBinarizerTest {

   // Tests if the image pixels are correctly converted into 0s and 1s based on their color distance.
    @Test
    void testToBinaryArray() {

        // Fake color distance finder
        ColorDistanceFinder finder = (a, b) -> Math.abs(a - b);

        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(finder, 0x000000, 10);

        // Create a 2x2 image
        BufferedImage image =
            new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);

        image.setRGB(0, 0, 0x000000);
        image.setRGB(1, 0, 0x000005);
        image.setRGB(0, 1, 0x000010);
        image.setRGB(1, 1, 0xFFFFFF);

        int[][] expected = {
            {1, 1},
            {0, 0}
        };

        int[][] actual = binarizer.toBinaryArray(image);

        assertArrayEquals(expected, actual);
    }



    // Tests if a binary array is correctly converted into an image with black and white pixels.
    @Test
    void testToBufferedImage() {

        ColorDistanceFinder finder = (a, b) -> Math.abs(a - b);

        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(finder, 0x000000, 10);

        int[][] binary = {
            {1, 0},
            {0, 1}
        };

        BufferedImage result = binarizer.toBufferedImage(binary);

        assertEquals(2, result.getWidth());
        assertEquals(2, result.getHeight());

        assertEquals(0xFFFFFF, result.getRGB(0, 0) & 0x00FFFFFF);
        assertEquals(0x000000, result.getRGB(1, 0) & 0x00FFFFFF);
        assertEquals(0x000000, result.getRGB(0, 1) & 0x00FFFFFF);
        assertEquals(0xFFFFFF, result.getRGB(1, 1) & 0x00FFFFFF);
    }


    // Tests if a pixel becomes 0 when its color distance is exactly equal to the threshold.
    @Test
    void testDistanceEqualsThreshold() {
        ColorDistanceFinder finder = (a, b) -> Math.abs(a - b);

        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(finder, 0x000000, 10);

        BufferedImage image =
            new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

        image.setRGB(0, 0, 0x00000A);

        int[][] result = binarizer.toBinaryArray(image);

        assertEquals(0, result[0][0]);
    }


    // Tests if the alpha channel is removed before comparing the pixel color with the target color.
    @Test
    void testAlphaChannelRemoval() {
        ColorDistanceFinder finder = (a, b) -> Math.abs(a - b);

        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(finder, 0xFF0000, 1);

        BufferedImage image =
            new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

        image.setRGB(0, 0, 0x80FF0000);

        int[][] result = binarizer.toBinaryArray(image);

        assertEquals(1, result[0][0]);
    }


    // Tests if a rectangular binary array is converted into an image with the correct size and pixel colors.
    @Test
    void testRectangularImage() {
        ColorDistanceFinder finder = (a, b) -> Math.abs(a - b);

        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(finder, 0x000000, 10);

        int[][] binary = {
            {1, 0, 1},
            {0, 1, 0}
        };

        BufferedImage result = binarizer.toBufferedImage(binary);

        assertEquals(3, result.getWidth());
        assertEquals(2, result.getHeight());

        assertEquals(0xFFFFFF, result.getRGB(2, 0) & 0x00FFFFFF);
        assertEquals(0x000000, result.getRGB(2, 1) & 0x00FFFFFF);
    }
}