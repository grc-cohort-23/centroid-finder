import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for DistanceImageBinarizer.
 *
 * These tests use FAKE distance finders instead of the real Euclidean one.
 * That way, every test checks only the binarizer's own logic (looping,
 * alpha stripping, threshold comparison, x/y order), and a bug in the
 * distance math can't make these tests fail.
 */
public class DistanceImageBinarizerTest {

    // ------------------------------------------------------------------
    // Fakes
    // ------------------------------------------------------------------

    /**
     * A fake that always returns the same distance, no matter which colors
     * it gets. It also remembers the last two colors it was called with,
     * so a test can check WHAT the binarizer passed in.
     */
    private static class FixedDistanceFinder implements ColorDistanceFinder {
        private final double distanceToReturn;
        int lastColorA;
        int lastColorB;

        FixedDistanceFinder(double distanceToReturn) {
            this.distanceToReturn = distanceToReturn;
        }

        @Override
        public double distance(int colorA, int colorB) {
            lastColorA = colorA;
            lastColorB = colorB;
            return distanceToReturn;
        }
    }

    /**
     * A fake that returns 0 when the two colors match exactly and 100
     * otherwise. Lets a test mark ONE specific pixel as "close" and check
     * that exactly that spot in the array becomes 1.
     */
    private static class ExactMatchDistanceFinder implements ColorDistanceFinder {
        @Override
        public double distance(int colorA, int colorB) {
            return colorA == colorB ? 0 : 100;
        }
    }

    /** Builds an image of the given size where every pixel is one color. */
    private static BufferedImage solidImage(int width, int height, int rgb) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                img.setRGB(x, y, rgb);
            }
        }
        return img;
    }

    // ------------------------------------------------------------------
    // toBinaryArray: the threshold rule
    // ------------------------------------------------------------------

    @Test
    void toBinaryArray_distanceBelowThreshold_isWhite() {
        // Fake says every pixel is distance 5; threshold is 10 -> 5 < 10 -> white (1)
        DistanceImageBinarizer binarizer =
                new DistanceImageBinarizer(new FixedDistanceFinder(5), 0x000000, 10);

        int[][] result = binarizer.toBinaryArray(solidImage(2, 2, 0x123456));

        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 2; c++) {
                assertEquals(1, result[r][c]);
            }
        }
    }

    @Test
    void toBinaryArray_distanceAboveThreshold_isBlack() {
        // 50 is not < 10 -> black (0)
        DistanceImageBinarizer binarizer =
                new DistanceImageBinarizer(new FixedDistanceFinder(50), 0x000000, 10);

        int[][] result = binarizer.toBinaryArray(solidImage(2, 2, 0x123456));

        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 2; c++) {
                assertEquals(0, result[r][c]);
            }
        }
    }

    @Test
    void toBinaryArray_distanceEqualToThreshold_isBlack() {
        // Edge case: the rule is "LESS THAN", so exactly equal must be black.
        // Catches someone accidentally writing <= instead of <.
        DistanceImageBinarizer binarizer =
                new DistanceImageBinarizer(new FixedDistanceFinder(10), 0x000000, 10);

        int[][] result = binarizer.toBinaryArray(solidImage(1, 1, 0x123456));

        assertEquals(0, result[0][0]);
    }

    // ------------------------------------------------------------------
    // toBinaryArray: what gets passed to the distance finder
    // ------------------------------------------------------------------

    @Test
    void toBinaryArray_stripsAlphaBeforeMeasuringDistance() {
        // getRGB returns 0xAARRGGBB (here 0xFF123456). The distance finder
        // must receive 0x00123456 -- alpha bits cleared.
        FixedDistanceFinder fake = new FixedDistanceFinder(0);
        DistanceImageBinarizer binarizer = new DistanceImageBinarizer(fake, 0xABCDEF, 10);

        binarizer.toBinaryArray(solidImage(1, 1, 0x123456));

        assertEquals(0x123456, fake.lastColorA, "pixel color should have alpha stripped");
        assertEquals(0xABCDEF, fake.lastColorB, "second color should be the target color");
    }

    // ------------------------------------------------------------------
    // toBinaryArray: shape and x/y order
    // ------------------------------------------------------------------

    @Test
    void toBinaryArray_nonSquareImage_hasHeightRowsAndWidthColumns() {
        // 4 wide, 3 tall -> array should be [3][4]
        DistanceImageBinarizer binarizer =
                new DistanceImageBinarizer(new FixedDistanceFinder(0), 0x000000, 10);

        int[][] result = binarizer.toBinaryArray(solidImage(4, 3, 0x000000));

        assertEquals(3, result.length, "number of rows should equal image height");
        assertEquals(4, result[0].length, "number of columns should equal image width");
    }

    @Test
    void toBinaryArray_pixelLandsInCorrectRowAndColumn() {
        // 3 wide, 2 tall image, all black except ONE pixel at x=2, y=1 that
        // matches the target. Only result[1][2] (row=y, col=x) should be 1.
        // If x and y were swapped anywhere, this fails (or crashes).
        int target = 0xFF0000;
        BufferedImage img = solidImage(3, 2, 0x000000);
        img.setRGB(2, 1, target);

        DistanceImageBinarizer binarizer =
                new DistanceImageBinarizer(new ExactMatchDistanceFinder(), target, 10);

        int[][] result = binarizer.toBinaryArray(img);

        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 3; c++) {
                int expected = (r == 1 && c == 2) ? 1 : 0;
                assertEquals(expected, result[r][c], "wrong value at row " + r + ", col " + c);
            }
        }
    }

    // ------------------------------------------------------------------
    // toBufferedImage
    // ------------------------------------------------------------------

    @Test
    void toBufferedImage_hasCorrectDimensions() {
        // Array with 2 rows and 5 columns -> image 5 wide, 2 tall
        DistanceImageBinarizer binarizer =
                new DistanceImageBinarizer(new FixedDistanceFinder(0), 0x000000, 10);

        BufferedImage img = binarizer.toBufferedImage(new int[2][5]);

        assertEquals(5, img.getWidth());
        assertEquals(2, img.getHeight());
    }

    @Test
    void toBufferedImage_onesAreWhite_zerosAreBlack() {
        DistanceImageBinarizer binarizer =
                new DistanceImageBinarizer(new FixedDistanceFinder(0), 0x000000, 10);

        int[][] binary = {
            {1, 0},
            {0, 1}
        };

        BufferedImage img = binarizer.toBufferedImage(binary);

        // getRGB adds alpha back on, so mask it off before comparing
        assertEquals(0xFFFFFF, img.getRGB(0, 0) & 0xFFFFFF);
        assertEquals(0x000000, img.getRGB(1, 0) & 0xFFFFFF);
        assertEquals(0x000000, img.getRGB(0, 1) & 0xFFFFFF);
        assertEquals(0xFFFFFF, img.getRGB(1, 1) & 0xFFFFFF);
    }

    @Test
    void toBufferedImage_nonSquare_pixelLandsInCorrectSpot() {
        // 2 rows, 3 columns, single 1 at row 1, col 2 -> white pixel at x=2, y=1
        DistanceImageBinarizer binarizer =
                new DistanceImageBinarizer(new FixedDistanceFinder(0), 0x000000, 10);

        int[][] binary = {
            {0, 0, 0},
            {0, 0, 1}
        };

        BufferedImage img = binarizer.toBufferedImage(binary);

        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 3; x++) {
                int expected = (x == 2 && y == 1) ? 0xFFFFFF : 0x000000;
                assertEquals(expected, img.getRGB(x, y) & 0xFFFFFF, "wrong color at x=" + x + ", y=" + y);
            }
        }
    }

    // ------------------------------------------------------------------
    // Both methods together
    // ------------------------------------------------------------------

    @Test
    void roundTrip_arrayToImageAndBack_givesSameArray() {
        // Turn an array into an image, then binarize that image with
        // target = white. White pixels match (distance 0 -> 1), black
        // pixels don't (distance 100 -> 0), so we get the original back.
        DistanceImageBinarizer binarizer =
                new DistanceImageBinarizer(new ExactMatchDistanceFinder(), 0xFFFFFF, 10);

        int[][] original = {
            {1, 0, 1},
            {0, 1, 0}
        };

        int[][] back = binarizer.toBinaryArray(binarizer.toBufferedImage(original));

        for (int r = 0; r < original.length; r++) {
            for (int c = 0; c < original[r].length; c++) {
                assertEquals(original[r][c], back[r][c], "mismatch at row " + r + ", col " + c);
            }
        }
    }
}