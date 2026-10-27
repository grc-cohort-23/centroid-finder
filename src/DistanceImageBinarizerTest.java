import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.awt.image.BufferedImage;

public class DistanceImageBinarizerTest {
    // A pretend distance tool. The distance between two colors is just the
    // difference between their numbers, so it's easy to predict.
    private static class FakeDistanceFinder implements ColorDistanceFinder {
        List<Integer> colorsSeen = new ArrayList<>();

        @Override
        public double distance(int colorA, int colorB) {
            colorsSeen.add(colorA);
            colorsSeen.add(colorB);
            return Math.abs(colorA - colorB);
        }
    }

    // Makes an image where every pixel is the same color.
    private static BufferedImage imageFilledWith(int width, int height, int color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, color);
            }
        }
        return image;
    }

    // If pixel is exactly the target color distance is 0. Less
    // than the threshold pixel should be white or 1.
    @Test
    void toBinaryArray_pixelMatchesTarget_returnsWhite() {
        // Arrange
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, 0x000000);
        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(new FakeDistanceFinder(), 0x000000, 10);

        // Act
        int[][] actual = binarizer.toBinaryArray(image);

        // Assert
        assertArrayEquals(new int[][] { {1} }, actual);
    }

    // The pixel is far from the target (distance 100, threshold 10),
    // so the pixel should be black (0).
    @Test
    void toBinaryArray_pixelFarFromTarget_returnsBlack() {
        // Arrange
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, 0x000064); // 100
        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(new FakeDistanceFinder(), 0x000000, 10);

        // Act
        int[][] actual = binarizer.toBinaryArray(image);

        // Assert
        assertArrayEquals(new int[][] { {0} }, actual);
    }

    // The README says "less than" the threshold, so a distance that EQUALS
    // the threshold is not close enough. Distance 10, threshold 10 -> black.
    @Test
    void toBinaryArray_distanceEqualsThreshold_returnsBlack() {
        // Arrange
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, 0x00000A); // 10
        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(new FakeDistanceFinder(), 0x000000, 10);

        // Act
        int[][] actual = binarizer.toBinaryArray(image);

        // Assert
        assertArrayEquals(new int[][] { {0} }, actual);
    }
        
    // One step under the line: distance 9, threshold 10 -> white.
    @Test
    void toBinaryArray_distanceJustUnderThreshold_returnsWhite() {
        // Arrange
        BufferedImage image = imageFilledWith(1, 1, 0x000009); // 9
        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(new FakeDistanceFinder(), 0x000000, 10);

        // Act
        int[][] actual = binarizer.toBinaryArray(image);

        // Assert
        assertArrayEquals(new int[][] { {1} }, actual);
    }

    // With a threshold of 0, nothing can be "less than 0", so even a perfect
    // match (distance 0) should be black.
    @Test
    void toBinaryArray_thresholdZero_exactMatchIsBlack() {
        // Arrange
        BufferedImage image = imageFilledWith(1, 1, 0x000000);
        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(new FakeDistanceFinder(), 0x000000, 0);

        // Act
        int[][] actual = binarizer.toBinaryArray(image);

        // Assert
        assertArrayEquals(new int[][] { {0} }, actual);
    }

    // The picture has see-through digits at the front (0x80). Our code should
    // wipe those off before asking the distance tool. If they leak through,
    // the distance would be huge and the pixel would wrongly be black.
    @Test
    void toBinaryArray_alphaBitsAreStrippedBeforeMeasuringDistance() {
        // Arrange
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, 0x80000010);
        FakeDistanceFinder fake = new FakeDistanceFinder();
        DistanceImageBinarizer binarizer = new DistanceImageBinarizer(fake, 0x000020, 20);

        // Act
        int[][] actual = binarizer.toBinaryArray(image);

        // Assert
        assertTrue(fake.colorsSeen.contains(0x000010));
        assertArrayEquals(new int[][] { {1} }, actual);
    }

    // The distance tool should be asked about the target color we gave it.
    @Test
    void toBinaryArray_targetColorIsPassedToDistanceFinder() {
        // Arrange
        BufferedImage image = imageFilledWith(1, 1, 0x000010);
        FakeDistanceFinder fake = new FakeDistanceFinder();
        DistanceImageBinarizer binarizer = new DistanceImageBinarizer(fake, 0x000020, 20);

        // Act
        binarizer.toBinaryArray(image);

        // Assert
        assertTrue(fake.colorsSeen.contains(0x000020));
    }

    // An image 3 wide and 2 tall should give an array with 2 rows and
    // 3 columns ([2][3]), not the other way around.
    @Test
    void toBinaryArray_nonSquareImage_hasRowsByColumnsShape() {
        // Arrange
        BufferedImage image = imageFilledWith(3, 2, 0x000064);
        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(new FakeDistanceFinder(), 0x000000, 10);

        // Act
        int[][] actual = binarizer.toBinaryArray(image);

        // Assert
        assertEquals(2, actual.length);
        assertEquals(3, actual[0].length);
    }

    // getRGB uses (x, y) but the array uses [row][column], so each white
    // pixel must land at [y][x].
    @Test
    void toBinaryArray_mixedPixels_landInCorrectRowAndColumn() {
        // Arrange
        BufferedImage image = imageFilledWith(3, 2, 0x000064); // far from target
        image.setRGB(0, 0, 0x000000); // x=0, y=0 matches
        image.setRGB(1, 1, 0x000000); // x=1, y=1 matches
        image.setRGB(2, 1, 0x000000); // x=2, y=1 matches
        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(new FakeDistanceFinder(), 0x000000, 10);

        // Act
        int[][] actual = binarizer.toBinaryArray(image);

        // Assert
        int[][] expected = {
            {1, 0, 0},
            {0, 1, 1}
        };
        assertArrayEquals(expected, actual);
    }

    // Every pixel should be measured exactly once. Each measurement records
    // 2 colors, so 3 x 2 pixels -> 12 colors.
    @Test
    void toBinaryArray_distanceCalledOncePerPixel() {
        // Arrange
        BufferedImage image = imageFilledWith(3, 2, 0x000064);
        FakeDistanceFinder fake = new FakeDistanceFinder();
        DistanceImageBinarizer binarizer = new DistanceImageBinarizer(fake, 0x000000, 10);

        // Act
        binarizer.toBinaryArray(image);

        // Assert
        assertEquals(2 * 3 * 2, fake.colorsSeen.size());
    }

    // ---------- toBufferedImage ----------

    // Turn a 2x2 grid of 1s and 0s into a picture. 1 is white, 0 is black.
    // getRGB gives back the see-through digits too, so we only compare the
    // last 6 digits.
    @Test
    void toBufferedImage_twoByTwo_paintsBlackAndWhiteCorrectly() {
        // Arrange
        int[][] grid = {
            {0, 1},
            {1, 0}
        };
        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(new FakeDistanceFinder(), 0x000000, 10);

        // Act
        BufferedImage actual = binarizer.toBufferedImage(grid);

        // Assert
        assertEquals(0x000000, actual.getRGB(0, 0) & 0xFFFFFF);
        assertEquals(0xFFFFFF, actual.getRGB(1, 0) & 0xFFFFFF);
        assertEquals(0xFFFFFF, actual.getRGB(0, 1) & 0xFFFFFF);
        assertEquals(0x000000, actual.getRGB(1, 1) & 0xFFFFFF);
    }

    // A grid with 2 rows and 3 columns should become a picture that is
    // 3 wide and 2 tall (not the other way around). The white pixels
    // should land in the right spots.
    @Test
    void toBufferedImage_nonSquareGrid_hasCorrectSizeAndPixelPlacement() {
        // Arrange
        int[][] grid = {
            {1, 0, 0},
            {0, 0, 1}
        };
        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(new FakeDistanceFinder(), 0x000000, 10);

        // Act
        BufferedImage actual = binarizer.toBufferedImage(grid);

        // Assert
        assertEquals(3, actual.getWidth());
        assertEquals(2, actual.getHeight());
        assertEquals(0xFFFFFF, actual.getRGB(0, 0) & 0xFFFFFF); // row 0, col 0
        assertEquals(0x000000, actual.getRGB(1, 0) & 0xFFFFFF); // row 0, col 1
        assertEquals(0xFFFFFF, actual.getRGB(2, 1) & 0xFFFFFF); // row 1, col 2
    }
    
    // A grid of all 0s should make a picture where every pixel is black.
    @Test
    void toBufferedImage_allZeros_everyPixelBlack() {
        // Arrange
        int[][] grid = {
            {0, 0, 0},
            {0, 0, 0}
        };
        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(new FakeDistanceFinder(), 0x000000, 10);

        // Act
        BufferedImage actual = binarizer.toBufferedImage(grid);

        // Assert
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 3; x++) {
                assertEquals(0x000000, actual.getRGB(x, y) & 0xFFFFFF);
            }
        }
    }

    // A grid of all 1s should make a picture where every pixel is white.
    @Test
    void toBufferedImage_allOnes_everyPixelWhite() {
        // Arrange
        int[][] grid = {
            {1, 1, 1},
            {1, 1, 1}
        };
        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(new FakeDistanceFinder(), 0x000000, 10);

        // Act
        BufferedImage actual = binarizer.toBufferedImage(grid);

        // Assert
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 3; x++) {
                assertEquals(0xFFFFFF, actual.getRGB(x, y) & 0xFFFFFF);
            }
        }
    }

    // The smallest possible grid, just one square, should still work.
    @Test
    void toBufferedImage_oneByOne_makesOnePixelImage() {
        // Arrange
        int[][] grid = { {1} };
        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(new FakeDistanceFinder(), 0x000000, 10);

        // Act
        BufferedImage actual = binarizer.toBufferedImage(grid);

        // Assert
        assertEquals(1, actual.getWidth());
        assertEquals(1, actual.getHeight());
        assertEquals(0xFFFFFF, actual.getRGB(0, 0) & 0xFFFFFF);
    }

    // Turn a picture into 1s and 0s, then back into a picture. Only the pixel that matched the target should end up white.he image with the right pixels white
    @Test
    void roundTrip_matchingPixelBecomesWhiteAndOthersBlack() {
        // Arrange
        BufferedImage image = imageFilledWith(2, 2, 0x000064); // far from target
        image.setRGB(0, 0, 0x000000); // the only match
        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(new FakeDistanceFinder(), 0x000000, 10);

        // Act
        BufferedImage actual = binarizer.toBufferedImage(binarizer.toBinaryArray(image));

        // Assert
        assertEquals(0xFFFFFF, actual.getRGB(0, 0) & 0xFFFFFF);
        assertEquals(0x000000, actual.getRGB(1, 0) & 0xFFFFFF);
        assertEquals(0x000000, actual.getRGB(0, 1) & 0xFFFFFF);
        assertEquals(0x000000, actual.getRGB(1, 1) & 0xFFFFFF);
    }
}
