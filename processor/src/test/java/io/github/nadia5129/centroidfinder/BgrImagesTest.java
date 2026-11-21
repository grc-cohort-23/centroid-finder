package io.github.nadia5129.centroidfinder;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.util.Random;
import org.junit.jupiter.api.Test;

public class BgrImagesTest {

    // rgbAt reads blue, green, red bytes and gives back 0xRRGGBB
    @Test
    void rgbAt_knownBytes_returnsRgbInt() {
        // Arrange: pixel 0 is blue=0x56 green=0x34 red=0x12, pixel 1 is all 0xFF
        byte[] bytes = { 0x56, 0x34, 0x12, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };

        // Act + Assert
        assertEquals(0x123456, BgrImages.rgbAt(bytes, 0));
        assertEquals(0xFFFFFF, BgrImages.rgbAt(bytes, 1));
    }

    // Bytes above 127 are negative in Java; & 0xFF must turn them back into 128-255
    @Test
    void rgbAt_bytesAbove127_areNotNegative() {
        // Arrange
        byte[] bytes = { (byte) 0x80, (byte) 0x90, (byte) 0xF0 };

        // Act + Assert
        assertEquals(0xF09080, BgrImages.rgbAt(bytes, 0));
    }

    // For every pixel of a JavaCV-style image, rgbAt gives the same color as getRGB
    @Test
    void rgbAt_random3ByteBgrImage_matchesGetRgb() {
        // Arrange
        BufferedImage image = randomImage(31, 17, BufferedImage.TYPE_3BYTE_BGR, new Random(7));
        byte[] bytes = BgrImages.bytes(image);

        // Act + Assert
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = y * image.getWidth() + x;
                assertEquals(image.getRGB(x, y) & 0xFFFFFF, BgrImages.rgbAt(bytes, pixel), "pixel " + x + "," + y);
            }
        }
    }

    // The fast byte path in DistanceImageBinarizer (TYPE_3BYTE_BGR) and the getRGB path
    // (TYPE_INT_RGB) must give exactly the same binary array for the same pixels
    @Test
    void toBinaryArray_bgrImageAndIntRgbImageWithSamePixels_giveSameArray() {
        // Arrange
        Random random = new Random(2026);
        int[] targets = { 0x420500, 0xFF4500, 0x000000, 0xFFFFFF, 0x808080 };
        int[] thresholds = { 0, 1, 30, 60, 100, 200, 442 };

        for (int round = 0; round < 20; round++) {
            BufferedImage bgr = randomImage(1 + random.nextInt(40), 1 + random.nextInt(40),
                BufferedImage.TYPE_3BYTE_BGR, random);
            BufferedImage intRgb = sameImageAs(bgr, BufferedImage.TYPE_INT_RGB);

            for (int target : targets) {
                for (int threshold : thresholds) {
                    DistanceImageBinarizer binarizer =
                        new DistanceImageBinarizer(new EuclideanColorDistance(), target, threshold);

                    // Act
                    int[][] fast = binarizer.toBinaryArray(bgr);
                    int[][] slow = binarizer.toBinaryArray(intRgb);

                    // Assert
                    assertArrayEquals(slow, fast, "round " + round + " target " + target + " threshold " + threshold);
                }
            }
        }
    }

    // A pixel exactly on the threshold (distance 5 = sqrt(3*3 + 4*4)) is black, on both paths
    @Test
    void toBinaryArray_pixelExactlyOnThreshold_isBlackOnBothPaths() {
        // Arrange: target black, pixel red=3 green=4 blue=0 is exactly distance 5 away
        BufferedImage bgr = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
        bgr.setRGB(0, 0, 0x030400);
        BufferedImage intRgb = sameImageAs(bgr, BufferedImage.TYPE_INT_RGB);
        DistanceImageBinarizer binarizer = new DistanceImageBinarizer(new EuclideanColorDistance(), 0x000000, 5);

        // Act + Assert
        assertEquals(0, binarizer.toBinaryArray(bgr)[0][0]);
        assertEquals(0, binarizer.toBinaryArray(intRgb)[0][0]);
    }

    private static BufferedImage randomImage(int width, int height, int type, Random random) {
        BufferedImage image = new BufferedImage(width, height, type);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, random.nextInt(0x1000000));
            }
        }
        return image;
    }

    private static BufferedImage sameImageAs(BufferedImage source, int type) {
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), type);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                copy.setRGB(x, y, source.getRGB(x, y));
            }
        }
        return copy;
    }
}
