import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

class DistanceImageBinarizerTest {
    private static final int TARGET_COLOR = 0xFFA200;

    @Test
    void convertsZeroValuesToBlackPixels() {
        DistanceImageBinarizer binarizer =
                new DistanceImageBinarizer(new EuclideanColorDistance(), TARGET_COLOR, 10);

        BufferedImage image = binarizer.toBufferedImage(new int[][] {
                { 0, 0 },
                { 0, 0 }
        });

        assertEquals(2, image.getWidth());
        assertEquals(2, image.getHeight());
        assertEquals(0xFF000000, image.getRGB(0, 0));
        assertEquals(0xFF000000, image.getRGB(1, 0));
        assertEquals(0xFF000000, image.getRGB(0, 1));
        assertEquals(0xFF000000, image.getRGB(1, 1));
    }

    @Test
    void convertsOneValuesToWhitePixels() {
        DistanceImageBinarizer binarizer =
                new DistanceImageBinarizer(new EuclideanColorDistance(), TARGET_COLOR, 10);

        BufferedImage image = binarizer.toBufferedImage(new int[][] {
                { 1, 1, 1 }
        });

        assertEquals(3, image.getWidth());
        assertEquals(1, image.getHeight());
        assertEquals(0xFFFFFFFF, image.getRGB(0, 0));
        assertEquals(0xFFFFFFFF, image.getRGB(1, 0));
        assertEquals(0xFFFFFFFF, image.getRGB(2, 0));
    }

    @Test
    void preservesRowsAndColumnsWhenConvertingMixedValues() {
        DistanceImageBinarizer binarizer =
                new DistanceImageBinarizer(new EuclideanColorDistance(), TARGET_COLOR, 10);

        BufferedImage image = binarizer.toBufferedImage(new int[][] {
                { 1, 0, 1 },
                { 0, 1, 0 }
        });

        assertEquals(3, image.getWidth());
        assertEquals(2, image.getHeight());
        assertEquals(0xFFFFFFFF, image.getRGB(0, 0));
        assertEquals(0xFF000000, image.getRGB(1, 0));
        assertEquals(0xFFFFFFFF, image.getRGB(2, 0));
        assertEquals(0xFF000000, image.getRGB(0, 1));
        assertEquals(0xFFFFFFFF, image.getRGB(1, 1));
        assertEquals(0xFF000000, image.getRGB(2, 1));
    }

    @Test
    void convertsPixelsToBinaryValuesUsingTheirColorDistance() {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, TARGET_COLOR);
        image.setRGB(1, 0, 0xFFAA00);
        image.setRGB(0, 1, 0x000000);
        image.setRGB(1, 1, 0xFFA209);

        DistanceImageBinarizer binarizer =
                new DistanceImageBinarizer(new EuclideanColorDistance(), TARGET_COLOR, 10);

        assertArrayEquals(new int[][] {
                { 1, 1 },
                { 0, 1 }
        }, binarizer.toBinaryArray(image));
    }

    @Test
    void treatsPixelsAtTheThresholdAsBlack() {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, 0xFFA205);

        DistanceImageBinarizer binarizer =
                new DistanceImageBinarizer(new EuclideanColorDistance(), TARGET_COLOR, 5);

        assertArrayEquals(new int[][] { { 0 } }, binarizer.toBinaryArray(image));
    }

    @Test
    void ignoresAlphaWhenComparingPixelColors() {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, 0x10FFA200);

        DistanceImageBinarizer binarizer =
                new DistanceImageBinarizer(new EuclideanColorDistance(), TARGET_COLOR, 1);

        assertArrayEquals(new int[][] { { 1 } }, binarizer.toBinaryArray(image));
    }
}
