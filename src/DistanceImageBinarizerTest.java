
import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

public class DistanceImageBinarizerTest {
    @Test
    void colorWithinThresholdBecomesWhite() {
        int targetColor = 0x000000;
        int threshold = 10;

        ColorDistanceFinder finder = new EuclideanColorDistance();
        DistanceImageBinarizer binarizer = new DistanceImageBinarizer(finder, targetColor, threshold);

        // Makes a Buffered image that is only 1 pixel to make testing easier
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        
        // Sets the color to black
        image.setRGB(0, 0, 0x000000);

        //binarizer returns: 0 if > threshhold 1 if less than
        int[][] result = binarizer.toBinaryArray(image);

        assertEquals(1, result[0][0]);
    }

    @Test
    void colorOutsideThresholdBecomesBlack() {
        int targetColor = 0xFFFFFF;
        int threshold = 400;

        ColorDistanceFinder finder = new EuclideanColorDistance();
        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(finder, targetColor, threshold);

        BufferedImage image =
            new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, 0x000000);

        int[][] result = binarizer.toBinaryArray(image);

        assertEquals(0, result[0][0]);
    }

    @Test
    void colorBelowThresholdBecomesWhite() {
        ColorDistanceFinder finder = (color1, color2) -> 9;

        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(finder, 0x123456, 10);

        BufferedImage image =
            new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

        image.setRGB(0, 0, 0x654321);

        int[][] result = binarizer.toBinaryArray(image);

        assertEquals(1, result[0][0]);
    }

    @Test
    void binaryZeroProducesBlackPixel() {
        EuclideanColorDistance d = new EuclideanColorDistance();


        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(d, 0x000000, 1);

        int[][] binary = {
            {0}
        };

        BufferedImage result = binarizer.toBufferedImage(binary);

        assertEquals(0x000000, result.getRGB(0, 0) & 0x00FFFFFF);
    }

    @Test
    void binaryOneProducesWhitePixel() {
        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(
                (color1, color2) -> 0, 0x000000, 1);

        int[][] binary = {
            {1}
        };

        BufferedImage result = binarizer.toBufferedImage(binary);

        assertEquals(0xFFFFFF, result.getRGB(0, 0) & 0x00FFFFFF);
    }

    @Test
    void nonSquareMatrixPreservesDimensionsAndPositions() {
        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(
                (color1, color2) -> 0, 0x000000, 1);

        int[][] binary = {
            {1, 0, 1},
            {0, 1, 0}
        };

        BufferedImage result = binarizer.toBufferedImage(binary);

        assertEquals(3, result.getWidth());
        assertEquals(2, result.getHeight());

        assertEquals(0xFFFFFF, result.getRGB(0, 0) & 0x00FFFFFF);
        assertEquals(0x000000, result.getRGB(1, 0) & 0x00FFFFFF);
        assertEquals(0xFFFFFF, result.getRGB(2, 0) & 0x00FFFFFF);

        assertEquals(0x000000, result.getRGB(0, 1) & 0x00FFFFFF);
        assertEquals(0xFFFFFF, result.getRGB(1, 1) & 0x00FFFFFF);
        assertEquals(0x000000, result.getRGB(2, 1) & 0x00FFFFFF);
    }
}

