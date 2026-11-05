package video;
 
import static org.junit.jupiter.api.Assertions.assertEquals;
 
import java.awt.image.BufferedImage;
import java.util.List;
 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
 
import image.BinarizingImageGroupFinder;
import image.Coordinate;
import image.DfsBinaryGroupFinder;
import image.DistanceImageBinarizer;
import image.EuclideanColorDistance;
import image.Group;
import image.ImageBinarizer;
import image.ImageGroupFinder;
import video.LargestCentroidLocator;
import video.TimedFrame;
import video.TimedGroup;

public class LargestCentroidLocatorTest {
    
    private static final int TARGET = 0xFF0000; // pure red
    private static final int THRESHOLD = 10;
    private static final Group NO_MATCH = new Group(0, new Coordinate(-1, -1));
 
    private LargestCentroidLocator locator;
 
    @BeforeEach
    void setUp() {
        ImageBinarizer binarizer = new DistanceImageBinarizer(new EuclideanColorDistance(), TARGET, THRESHOLD);
        ImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, new DfsBinaryGroupFinder());
        locator = new LargestCentroidLocator(finder);
    }
 
    // ---------- helpers ----------
 
    /** All-black frame (a new TYPE_INT_RGB image starts out black). */
    private static BufferedImage blank(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }
 
    /** Fills a w-by-h rectangle whose top-left corner is (x, y). */
    private static void fill(BufferedImage img, int x, int y, int w, int h, int color) {
        for (int j = y; j < y + h; j++) {
            for (int i = x; i < x + w; i++) {
                img.setRGB(i, j, color);
            }
        }
    }
 
    // ---------- integration tests: real binarizer + DFS finder ----------
 
    @Test
    void noMatchingPixels_returnsSentinelGroup() {
        TimedGroup result = locator.locate(new TimedFrame(0.0, blank(5, 5)));
 
        assertEquals(NO_MATCH, result.group());
    }
 
    @Test
    void singleMatchingPixel_returnsThatPixel() {
        BufferedImage img = blank(5, 5);
        img.setRGB(3, 1, TARGET);
 
        TimedGroup result = locator.locate(new TimedFrame(0.0, img));
 
        assertEquals(new Group(1, new Coordinate(3, 1)), result.group());
    }
 
    @Test
    void squareBlob_centroidIsItsCenter() {
        BufferedImage img = blank(10, 10);
        fill(img, 2, 2, 3, 3, TARGET); // covers x=2..4, y=2..4
 
        TimedGroup result = locator.locate(new TimedFrame(0.0, img));
 
        assertEquals(new Group(9, new Coordinate(3, 3)), result.group());
    }
 
    @Test
    void twoBlobs_returnsTheLargerOne() {
        BufferedImage img = blank(12, 12);
        fill(img, 0, 0, 2, 2, TARGET);   // 4 pixels
        fill(img, 6, 6, 3, 3, TARGET);   // 9 pixels, centroid (7,7)
 
        TimedGroup result = locator.locate(new TimedFrame(0.0, img));
 
        assertEquals(new Group(9, new Coordinate(7, 7)), result.group());
    }
 
    @Test
    void equalSizedBlobs_tieBreaksTowardLargerCoordinates() {
        BufferedImage img = blank(8, 8);
        img.setRGB(1, 1, TARGET);
        img.setRGB(5, 5, TARGET);
 
        TimedGroup result = locator.locate(new TimedFrame(0.0, img));
 
        // groups sort descending by (size, x, y), so (5,5) comes first
        assertEquals(new Group(1, new Coordinate(5, 5)), result.group());
    }
 
    @Test
    void centroidUsesIntegerDivision() {
        BufferedImage img = blank(6, 6);
        img.setRGB(1, 2, TARGET);
        img.setRGB(2, 2, TARGET); // x sum = 3, 3 / 2 = 1
 
        TimedGroup result = locator.locate(new TimedFrame(0.0, img));
 
        assertEquals(new Group(2, new Coordinate(1, 2)), result.group());
    }
 
    @Test
    void diagonalPixels_areNotConnected() {
        BufferedImage img = blank(5, 5);
        img.setRGB(1, 1, TARGET);
        img.setRGB(2, 2, TARGET);
 
        TimedGroup result = locator.locate(new TimedFrame(0.0, img));
 
        // two groups of size 1, not one group of size 2
        assertEquals(1, result.group().size());
    }
 
    @Test
    void colorsWithinThresholdMatch_colorsOutsideDoNot() {
        BufferedImage img = blank(6, 6);
        img.setRGB(1, 1, 0xFA0000); // distance 5 from target  -> matches
        img.setRGB(4, 4, 0xF00000); // distance 15 from target -> does not match
 
        TimedGroup result = locator.locate(new TimedFrame(0.0, img));
 
        // if (4,4) had matched too, the tie-break would have picked it instead
        assertEquals(new Group(1, new Coordinate(1, 1)), result.group());
    }
 
    @Test
    void timestampIsCopiedFromTheFrame() {
        TimedGroup result = locator.locate(new TimedFrame(2.5, blank(3, 3)));
 
        assertEquals(2.5, result.time(), 1e-9);
    }
 
    // ---------- unit tests: stub finder, no image package involved ----------
 
    @Test
    void finderReturnsNull_returnsSentinelGroup() {
        LargestCentroidLocator stubbed = new LargestCentroidLocator(img -> null);
 
        TimedGroup result = stubbed.locate(new TimedFrame(1.0, blank(1, 1)));
 
        assertEquals(NO_MATCH, result.group());
        assertEquals(1.0, result.time(), 1e-9);
    }
 
    @Test
    void finderReturnsEmptyList_returnsSentinelGroup() {
        LargestCentroidLocator stubbed = new LargestCentroidLocator(img -> List.of());
 
        TimedGroup result = stubbed.locate(new TimedFrame(1.0, blank(1, 1)));
 
        assertEquals(NO_MATCH, result.group());
    }
 
    @Test
    void finderReturnsGroups_usesTheFirstOne() {
        Group first = new Group(5, new Coordinate(1, 2));
        Group second = new Group(2, new Coordinate(9, 9));
        LargestCentroidLocator stubbed = new LargestCentroidLocator(img -> List.of(first, second));
 
        TimedGroup result = stubbed.locate(new TimedFrame(4.0, blank(1, 1)));
 
        assertEquals(first, result.group());
        assertEquals(4.0, result.time(), 1e-9);
    }

}
