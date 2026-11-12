import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class DistanceImageBinarizerTest {

    // Returns the same distance for every pixel, so only the threshold comparison is under test
    private static class FixedDistanceFinder implements ColorDistanceFinder {
        private final double fixedDistance;

        FixedDistanceFinder(double fixedDistance) {
            this.fixedDistance = fixedDistance;
        }

        @Override
        public double distance(int color1, int color2) {
            return fixedDistance;
        }
    }

    // Distance is |blue1 - blue2| only, so expected values can be computed by eye
    private static class BlueOnlyDistanceFinder implements ColorDistanceFinder {
        @Override
        public double distance(int color1, int color2) {
            return Math.abs((color1 & 0xFF) - (color2 & 0xFF));
        }
    }

    // Records every (color1, color2) pair it receives so tests can check the arguments
    private static class RecordingDistanceFinder implements ColorDistanceFinder {
        private final List<int[]> calls = new ArrayList<int[]>();

        @Override
        public double distance(int color1, int color2) {
            calls.add(new int[] {color1, color2});
            return 0.0;
        }
    }

    // Distance 5 is below threshold 10, so the pixel is white (1)
    @Test
    void distanceBelowThreshold_isWhite() {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        img.setRGB(0, 0, 0x123456);
        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(new FixedDistanceFinder(5.0), 0xFFFFFF, 10);

        int[][] result = binarizer.toBinaryArray(img);

        assertEquals(1, result[0][0]);
    }

    // Distance 10 equals threshold 10, and the Javadoc says "less than", so the pixel is black (0)
    @Test
    void distanceEqualToThreshold_isBlack() {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        img.setRGB(0, 0, 0x123456);
        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(new FixedDistanceFinder(10.0), 0xFFFFFF, 10);

        int[][] result = binarizer.toBinaryArray(img);

        assertEquals(0, result[0][0]);
    }

    // 3 wide by 2 tall -> 2 rows (height) of 3 columns (width)
    // A swapped width/height or [x][y] indexing mix-up fails on length alone
    @Test
    void nonSquareImage_hasHeightRowsAndWidthColumns() {
        BufferedImage img = new BufferedImage(3, 2, BufferedImage.TYPE_INT_RGB);
        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(new FixedDistanceFinder(0.0), 0x000000, 10);

        int[][] result = binarizer.toBinaryArray(img);

        assertEquals(2, result.length);
        assertEquals(3, result[0].length);
    }

    // Target blue is 10, threshold is 5, so a pixel is white only if |blue - 10| < 5
    // Row 0 blues: 10, 12, 15 -> dists 0, 2, 5   -> white, white, black (5 equals threshold)
    // Row 1 blues: 0, 5, 9    -> dists 10, 5, 1  -> black, black (equals threshold), white
    @Test
    void mixedPixels_matchesHandComputedArray() {
        BufferedImage img = new BufferedImage(3, 2, BufferedImage.TYPE_INT_RGB);
        img.setRGB(0, 0, 0x00000A);
        img.setRGB(1, 0, 0x00000C);
        img.setRGB(2, 0, 0x00000F);
        img.setRGB(0, 1, 0x000000);
        img.setRGB(1, 1, 0x000005);
        img.setRGB(2, 1, 0x000009);
        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(new BlueOnlyDistanceFinder(), 0x00000A, 5);

        int[][] result = binarizer.toBinaryArray(img);

        int[][] expected = {
            {1, 1, 0},
            {0, 0, 1}
        };
        assertArrayEquals(expected, result);
    }

    // getRGB returns 0xFF123456 for this pixel, but targetColor is documented as 0xRRGGBB
    // So the finder should see the pixel with alpha stripped (0x123456), then the target
    @Test
    void finderReceivesPixelThenTarget_withAlphaStripped() {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        img.setRGB(0, 0, 0x123456);
        RecordingDistanceFinder spy = new RecordingDistanceFinder();
        DistanceImageBinarizer binarizer = new DistanceImageBinarizer(spy, 0xABCDEF, 10);

        binarizer.toBinaryArray(img);

        assertEquals(1, spy.calls.size());
        assertEquals(0x123456, spy.calls.get(0)[0]);
        assertEquals(0xABCDEF, spy.calls.get(0)[1]);
    }

    // Array is 2 rows by 3 columns, so the image should be 3 wide by 2 tall
    // input[y][x] maps to getRGB(x, y); mask off alpha since TYPE_INT_RGB reports 0xFF there
    // Row 0: white, black, white. Row 1: black, white, black
    @Test
    void nonSquareArray_hasCorrectSizeAndPixels() {
        int[][] input = {
            {1, 0, 1},
            {0, 1, 0}
        };
        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(new FixedDistanceFinder(0.0), 0x000000, 1);

        BufferedImage result = binarizer.toBufferedImage(input);

        assertEquals(3, result.getWidth());
        assertEquals(2, result.getHeight());
        assertEquals(0xFFFFFF, result.getRGB(0, 0) & 0xFFFFFF);
        assertEquals(0x000000, result.getRGB(1, 0) & 0xFFFFFF);
        assertEquals(0xFFFFFF, result.getRGB(2, 0) & 0xFFFFFF);
        assertEquals(0x000000, result.getRGB(0, 1) & 0xFFFFFF);
        assertEquals(0xFFFFFF, result.getRGB(1, 1) & 0xFFFFFF);
        assertEquals(0x000000, result.getRGB(2, 1) & 0xFFFFFF);
    }

    // Target blue is 255, threshold is 10: blue pixels are dist 0 (white), black pixels are dist 255 (black)
    // Input row 0: blue, black, blue. Input row 1: black, blue, black
    // Output should be white, black, white over black, white, black
    // Neither distance is near the threshold, so this only checks the two methods agree on layout
    @Test
    void roundTrip_nonSquareImage_whiteWhereColorMatchesTarget() {
        BufferedImage input = new BufferedImage(3, 2, BufferedImage.TYPE_INT_RGB);
        input.setRGB(0, 0, 0x0000FF);
        input.setRGB(1, 0, 0x000000);
        input.setRGB(2, 0, 0x0000FF);
        input.setRGB(0, 1, 0x000000);
        input.setRGB(1, 1, 0x0000FF);
        input.setRGB(2, 1, 0x000000);
        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(new BlueOnlyDistanceFinder(), 0x0000FF, 10);

        BufferedImage result = binarizer.toBufferedImage(binarizer.toBinaryArray(input));

        assertEquals(3, result.getWidth());
        assertEquals(2, result.getHeight());
        assertEquals(0xFFFFFF, result.getRGB(0, 0) & 0xFFFFFF);
        assertEquals(0x000000, result.getRGB(1, 0) & 0xFFFFFF);
        assertEquals(0xFFFFFF, result.getRGB(2, 0) & 0xFFFFFF);
        assertEquals(0x000000, result.getRGB(0, 1) & 0xFFFFFF);
        assertEquals(0xFFFFFF, result.getRGB(1, 1) & 0xFFFFFF);
        assertEquals(0x000000, result.getRGB(2, 1) & 0xFFFFFF);
    }
}