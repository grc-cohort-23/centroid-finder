import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DistanceImageBinarizerTest {

    @Test
    public void distanceBelowThresholdBecomesWhite() {
        ColorDistanceFinder fake = (a, b) -> 9.0;

        DistanceImageBinarizer binarizer =
                new DistanceImageBinarizer(fake, 0xFF0000, 10);

        BufferedImage image =
                new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

        assertEquals(1, binarizer.toBinaryArray(image)[0][0]);
    }

    @Test
    public void distanceEqualToThresholdBecomesBlack() {
        ColorDistanceFinder fake = (a, b) -> 10.0;

        DistanceImageBinarizer binarizer =
                new DistanceImageBinarizer(fake, 0xFF0000, 10);

        BufferedImage image =
                new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

        assertEquals(0, binarizer.toBinaryArray(image)[0][0]);
    }

    @Test
    public void distanceAboveThresholdBecomesBlack() {
        ColorDistanceFinder fake = (a, b) -> 11.0;

        DistanceImageBinarizer binarizer =
                new DistanceImageBinarizer(fake, 0xFF0000, 10);

        BufferedImage image =
                new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

        assertEquals(0, binarizer.toBinaryArray(image)[0][0]);
    }

    @Test
    public void rectangularImageKeepsPixelPositions() {
        ColorDistanceFinder fake =
                (pixel, target) -> pixel == target ? 0.0 : 100.0;

        DistanceImageBinarizer binarizer =
                new DistanceImageBinarizer(fake, 0xFF0000, 10);

        BufferedImage image =
                new BufferedImage(3, 2, BufferedImage.TYPE_INT_RGB);

        image.setRGB(0, 0, 0xFF0000);
        image.setRGB(2, 0, 0xFF0000);
        image.setRGB(1, 1, 0xFF0000);

        int[][] actual = binarizer.toBinaryArray(image);

        assertEquals(2, actual.length);
        assertArrayEquals(new int[] {1, 0, 1}, actual[0]);
        assertArrayEquals(new int[] {0, 1, 0}, actual[1]);
    }

    @Test
    public void alphaIsRemovedBeforeComparingColors() {
        ColorDistanceFinder fake = (pixel, target) -> {
            assertEquals(0x123456, pixel);
            assertEquals(0xABCDEF, target);
            return 0.0;
        };

        DistanceImageBinarizer binarizer =
                new DistanceImageBinarizer(fake, 0xABCDEF, 10);

        BufferedImage image =
                new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

        image.setRGB(0, 0, 0x80123456);

        assertEquals(1, binarizer.toBinaryArray(image)[0][0]);
    }

    @Test
    public void binaryArrayBecomesBlackAndWhiteImage() {
        DistanceImageBinarizer binarizer =
                new DistanceImageBinarizer(
                        new EuclideanColorDistance(), 0, 10
                );

        int[][] binary = {
                {1, 0, 1},
                {0, 1, 0}
        };

        BufferedImage actual = binarizer.toBufferedImage(binary);

        assertEquals(3, actual.getWidth());
        assertEquals(2, actual.getHeight());

        for (int row = 0; row < binary.length; row++) {
            for (int col = 0; col < binary[row].length; col++) {
                int expected = binary[row][col] == 1
                        ? 0xFFFFFF : 0x000000;

                assertEquals(
                        expected,
                        actual.getRGB(col, row) & 0xFFFFFF
                );
            }
        }
    }

    @Test
    public void realColorDistanceFindsMatchingPixel() {
        DistanceImageBinarizer binarizer =
                new DistanceImageBinarizer(
                        new EuclideanColorDistance(), 0xFF0000, 10
                );

        BufferedImage image =
                new BufferedImage(2, 1, BufferedImage.TYPE_INT_RGB);

        image.setRGB(0, 0, 0xFF0000);
        image.setRGB(1, 0, 0x0000FF);

        int[][] actual = binarizer.toBinaryArray(image);

        assertArrayEquals(new int[] {1, 0}, actual[0]);
    }
}