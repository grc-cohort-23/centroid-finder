import static org.junit.jupiter.api.Assertions.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;


public class DistanceImageBinarizerTest {
    
 @Test
    public void testExactTargetColor() {
    int target = 0xFF0000;

     ColorDistanceFinder finder = (a, b) ->
          a == b ? 0.0 : 100.0;

     DistanceImageBinarizer binarizer =
      new DistanceImageBinarizer(finder, target, 10);

        BufferedImage input = new BufferedImage(
         1, 1, BufferedImage.TYPE_INT_RGB
        );
        input.setRGB(0, 0, target);

        int[][] result = binarizer.toBinaryArray(input);

     assertEquals(1, result[0][0]);
    }

@Test
public void testDistanceEqualToThresholdIsBlack() {
     int target = 0xFF0000;

     ColorDistanceFinder finder = (a, b) -> 10.0;

     DistanceImageBinarizer binarizer =
        new DistanceImageBinarizer(finder, target, 10);

     BufferedImage input = new BufferedImage(
          1, 1, BufferedImage.TYPE_INT_RGB );

    int[][] result = binarizer.toBinaryArray(input);

     assertEquals(0, result[0][0]);
    }

 @Test
public void testAllZerosProduceBlackImage() {
     int[][] pixels = {
           {0, 0},
          {0, 0}
        };

 ColorDistanceFinder finder = (a, b) -> 0.0;

     DistanceImageBinarizer binarizer =
          new DistanceImageBinarizer(finder, 0, 10);

     BufferedImage result = binarizer.toBufferedImage(pixels);

       assertEquals(2, result.getWidth());
     assertEquals(2, result.getHeight());

     for (int y = 0; y < result.getHeight(); y++) {
         for (int x = 0; x < result.getWidth(); x++) {
               assertEquals(0x000000, result.getRGB(x, y)
                   & 0x00FFFFFF);
            }
        }
    }

 @Test
    public void testAllOnesProduceWhiteImage() {
        int[][] pixels = {
            {1, 1},
            {1, 1}
        };

        ColorDistanceFinder finder = (a, b) -> 0.0;

        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(finder, 0, 10);

        BufferedImage result = binarizer.toBufferedImage(pixels);

        for (int y = 0; y < result.getHeight(); y++) {
            for (int x = 0; x < result.getWidth(); x++) {
                assertEquals(0xFFFFFF, result.getRGB(x, y)
                    & 0x00FFFFFF);
            }
        }
    }

 @Test
    public void testMixedPixelsKeepCorrectPositions() {
        int[][] pixels = {
            {1, 0},
            {0, 1}
        };

        ColorDistanceFinder finder = (a, b) -> 0.0;

        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(finder, 0, 10);

        BufferedImage result = binarizer.toBufferedImage(pixels);

        assertEquals(0xFFFFFF, result.getRGB(0, 0) & 0x00FFFFFF);
        assertEquals(0x000000, result.getRGB(1, 0) & 0x00FFFFFF);
        assertEquals(0x000000, result.getRGB(0, 1) & 0x00FFFFFF);
        assertEquals(0xFFFFFF, result.getRGB(1, 1) & 0x00FFFFFF);
    }
        @Test
    public void testDistanceJustBelowThresholdIsWhite() {
        ColorDistanceFinder finder = (a, b) -> 9.99;
        DistanceImageBinarizer binarizer = new DistanceImageBinarizer(finder, 0xFF0000, 10);
        BufferedImage input = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

        int[][] result = binarizer.toBinaryArray(input);

        assertEquals(1, result[0][0]);
    }

    @Test
    public void testNonSquareImageKeepsRowsAndColumns() {
        int target = 0xFF0000;
        ColorDistanceFinder finder = (a, b) -> a == b ? 0.0 : 100.0;
        DistanceImageBinarizer binarizer = new DistanceImageBinarizer(finder, target, 10);
        BufferedImage input = new BufferedImage(3, 2, BufferedImage.TYPE_INT_RGB);
        input.setRGB(2, 0, target);

        int[][] result = binarizer.toBinaryArray(input);

        assertEquals(2, result.length);
        assertEquals(3, result[0].length);
        assertArrayEquals(new int[] {0, 0, 1}, result[0]);
        assertArrayEquals(new int[] {0, 0, 0}, result[1]);
    }
    // a mock test? it write down what it receive then give back a fixed num.
    @Test
    public void testFinderReceivesColorsWithoutAlpha() {
        List<Integer> pixelsSeen = new ArrayList<>();
        List<Integer> targetsSeen = new ArrayList<>();
        ColorDistanceFinder finder = (a, b) -> {
            pixelsSeen.add(a);
            targetsSeen.add(b);
            return 0.0;
        };
        DistanceImageBinarizer binarizer = new DistanceImageBinarizer(finder, 0x00FF00, 10);
        BufferedImage input = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        input.setRGB(0, 0, 0x123456);

        binarizer.toBinaryArray(input);

        assertEquals(List.of(0x123456), pixelsSeen);
        assertEquals(List.of(0x00FF00), targetsSeen);
    }
    // check array -> image
    @Test
    public void testToBufferedImageNonSquare() {
        int[][] pixels = {
            {1, 0, 0},
            {0, 0, 0}
        };
        DistanceImageBinarizer binarizer = new DistanceImageBinarizer((a, b) -> 0.0, 0, 10);
 
        BufferedImage result = binarizer.toBufferedImage(pixels);
 
        assertEquals(3, result.getWidth());
        assertEquals(2, result.getHeight());
        assertEquals(0xFFFFFF, result.getRGB(0, 0) & 0x00FFFFFF);
        assertEquals(0x000000, result.getRGB(2, 0) & 0x00FFFFFF);
        assertEquals(0x000000, result.getRGB(0, 1) & 0x00FFFFFF);
    }
 
    @Test
    public void testRoundTripKeepsPattern() {
        int[][] pixels = {
            {1, 0, 1},
            {0, 1, 0}
        };
        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(new EuclideanColorDistance(), 0xFFFFFF, 10);
 
        BufferedImage image = binarizer.toBufferedImage(pixels);
        int[][] result = binarizer.toBinaryArray(image);
 
        assertArrayEquals(pixels[0], result[0]);
        assertArrayEquals(pixels[1], result[1]);
    }
}

