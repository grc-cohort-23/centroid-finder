import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

public class DistanceImageBinarizerTest {

    // Helper: makes an image filled with one color
    private BufferedImage makeImage(int width, int height, int color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, color);
            }
        }
        return image;
    }

    @Test
    public void testAllPixelsCloseBecomeWhite() {
        // Fake: every pixel is distance 0 from the target
        ColorDistanceFinder fake = (a, b) -> 0;
        DistanceImageBinarizer binarizer = new DistanceImageBinarizer(fake, 0x000000, 10);
        BufferedImage image = makeImage(3, 2, 0x123456);

        int[][] expected = {
            {1, 1, 1},
            {1, 1, 1}
        };
        int[][] actual = binarizer.toBinaryArray(image);

        assertArrayEquals(expected, actual);
    }
    @Test
    public void testAllPixelsFarBecomeBlack() {
        // Fake: every pixel is distance 100 from the target
        ColorDistanceFinder fake = (a, b) -> 100;
        DistanceImageBinarizer binarizer = new DistanceImageBinarizer(fake, 0x000000, 50);
        BufferedImage image = makeImage(3, 2, 0x123456);

        int[][] expected = {
            {0, 0, 0},
            {0, 0, 0}
        };
        int[][] actual = binarizer.toBinaryArray(image);

        assertArrayEquals(expected, actual);
    }

    @Test
    public void testToBufferedImageAllBlack() {
        DistanceImageBinarizer binarizer = new DistanceImageBinarizer((a, b) -> 0, 0, 0);

        BufferedImage actual = binarizer.toBufferedImage(new int[][] {{0}});

        assertEquals(1, actual.getWidth());
        assertEquals(1, actual.getHeight());
        assertEquals(BufferedImage.TYPE_INT_RGB, actual.getType());
        assertEquals(0xFF000000, actual.getRGB(0, 0));
    }

    @Test
    public void testToBufferedImageAllWhite() {
        DistanceImageBinarizer binarizer = new DistanceImageBinarizer((a, b) -> 0, 0, 0);

        BufferedImage actual = binarizer.toBufferedImage(new int[][] {{1}});

        assertEquals(1, actual.getWidth());
        assertEquals(1, actual.getHeight());
        assertEquals(BufferedImage.TYPE_INT_RGB, actual.getType());
        assertEquals(0xFFFFFFFF, actual.getRGB(0, 0));
    }

    @Test
    public void testToBufferedImageMixedNonSquarePattern() {
        DistanceImageBinarizer binarizer = new DistanceImageBinarizer((a, b) -> 0, 0, 0);
        int[][] binary = {
            {1, 0, 1, 1},
            {0, 1, 0, 0},
            {1, 1, 0, 1}
        };
        int[][] expected = {
            {0xFFFFFFFF, 0xFF000000, 0xFFFFFFFF, 0xFFFFFFFF},
            {0xFF000000, 0xFFFFFFFF, 0xFF000000, 0xFF000000},
            {0xFFFFFFFF, 0xFFFFFFFF, 0xFF000000, 0xFFFFFFFF}
        };

        BufferedImage actual = binarizer.toBufferedImage(binary);

        assertEquals(4, actual.getWidth());
        assertEquals(3, actual.getHeight());
        assertImagePixels(expected, actual);
    }

    @Test
    public void testToBufferedImageIrregularPatternPreservesInput() {
        DistanceImageBinarizer binarizer = new DistanceImageBinarizer((a, b) -> 0, 0, 0);
        int[][] binary = {
            {0, 0, 1, 0, 1},
            {1, 0, 1, 1, 0},
            {0, 1, 0, 1, 0},
            {1, 1, 1, 0, 0}
        };
        int[][] original = new int[binary.length][];
        for (int y = 0; y < binary.length; y++) {
            original[y] = binary[y].clone();
        }
        int[][] expected = {
            {0xFF000000, 0xFF000000, 0xFFFFFFFF, 0xFF000000, 0xFFFFFFFF},
            {0xFFFFFFFF, 0xFF000000, 0xFFFFFFFF, 0xFFFFFFFF, 0xFF000000},
            {0xFF000000, 0xFFFFFFFF, 0xFF000000, 0xFFFFFFFF, 0xFF000000},
            {0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFF000000, 0xFF000000}
        };

        BufferedImage actual = binarizer.toBufferedImage(binary);

        assertEquals(BufferedImage.TYPE_INT_RGB, actual.getType());
        assertEquals(5, actual.getWidth());
        assertEquals(4, actual.getHeight());
        assertImagePixels(expected, actual);
        assertArrayEquals(original, binary);
    }

    private void assertImagePixels(int[][] expected, BufferedImage actual) {
        for (int y = 0; y < expected.length; y++) {
            for (int x = 0; x < expected[y].length; x++) {
                assertEquals(expected[y][x], actual.getRGB(x, y), "pixel at (" + x + ", " + y + ")");
            }
        }
    }
}
