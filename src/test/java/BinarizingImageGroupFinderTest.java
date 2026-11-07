import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
/* 
To make sure I know what part are breaking in the 3 classes, I need to fake two helpers w "pretent version". So they kinda guarentee gonna do it right and if there is something wrong, I know exactly where I did wrong.
So make two pretend classes
- Binarizer
- Group finder
Picture reach the binarizer -> the binarizer's answer reach group finder -> the final answer comeback unchanged (nothing found mean an empty list) -> each helper use exactly once.
*/
public class BinarizingImageGroupFinderTest {

    private static class FakeBinarizer implements ImageBinarizer {
        private final int[][] arrayToReturn;
        BufferedImage receivedImage;
        int calls = 0;

        FakeBinarizer(int[][] arrayToReturn) {
            this.arrayToReturn = arrayToReturn;
        }

        @Override
        public int[][] toBinaryArray(BufferedImage image) {
            calls++;
            receivedImage = image;
            return arrayToReturn;
        }

        @Override
        public BufferedImage toBufferedImage(int[][] image) {
            throw new UnsupportedOperationException("not used by BinarizingImageGroupFinder");
        }
    }

    /** Fake group finder: returns a fixed list and remembers what it was given. */
    private static class FakeGroupFinder implements BinaryGroupFinder {
        private final List<Group> groupsToReturn;
        int[][] receivedArray;
        int calls = 0;

        FakeGroupFinder(List<Group> groupsToReturn) {
            this.groupsToReturn = groupsToReturn;
        }

        @Override
        public List<Group> findConnectedGroups(int[][] image) {
            calls++;
            receivedArray = image;
            return groupsToReturn;
        }
    }

    private static BufferedImage blankImage(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    // ---------- wiring tests (using fakes) ----------

    @Test
    public void testPassesImageToBinarizer() {
        FakeBinarizer binarizer = new FakeBinarizer(new int[][] {{0}});
        FakeGroupFinder groupFinder = new FakeGroupFinder(new ArrayList<>());
        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        BufferedImage image = blankImage(1, 1);
        finder.findConnectedGroups(image);

        // the exact same image object should reach the binarizer
        assertSame(image, binarizer.receivedImage);
    }

    @Test
    public void testPassesBinaryArrayToGroupFinder() {
        int[][] binary = {{1, 0}, {0, 1}};
        FakeBinarizer binarizer = new FakeBinarizer(binary);
        FakeGroupFinder groupFinder = new FakeGroupFinder(new ArrayList<>());
        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        finder.findConnectedGroups(blankImage(2, 2));

        // whatever the binarizer returns should be handed to the group finder
        assertSame(binary, groupFinder.receivedArray);
    }

    @Test
    public void testReturnsGroupFinderResult() {
        List<Group> groups = List.of(
            new Group(5, new Coordinate(2, 3)),
            new Group(1, new Coordinate(0, 0))
        );
        FakeBinarizer binarizer = new FakeBinarizer(new int[][] {{1}});
        FakeGroupFinder groupFinder = new FakeGroupFinder(groups);
        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        List<Group> result = finder.findConnectedGroups(blankImage(1, 1));

        // the group finder's list should come back unchanged
        assertEquals(groups, result);
    }

    @Test
    public void testReturnsEmptyListWhenNoGroups() {
        FakeBinarizer binarizer = new FakeBinarizer(new int[][] {{0, 0}, {0, 0}});
        FakeGroupFinder groupFinder = new FakeGroupFinder(new ArrayList<>());
        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        List<Group> result = finder.findConnectedGroups(blankImage(2, 2));

        assertTrue(result.isEmpty());
    }

    @Test
    public void testCallsEachHelperExactlyOnce() {
        FakeBinarizer binarizer = new FakeBinarizer(new int[][] {{1}});
        FakeGroupFinder groupFinder = new FakeGroupFinder(new ArrayList<>());
        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        finder.findConnectedGroups(blankImage(1, 1));

        assertEquals(1, binarizer.calls);
        assertEquals(1, groupFinder.calls);
    }

    // ---------- integration test (using the real classes) ----------

    @Test
    public void testWithRealClassesFindsRedGroups() {
        int red = 0xFF0000;
        ImageBinarizer binarizer =
            new DistanceImageBinarizer(new EuclideanColorDistance(), red, 10);
        BinaryGroupFinder groupFinder = new DfsBinaryGroupFinder();
        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        // 3 wide, 2 tall. New images start all black.
        // R R .
        // . . R
        BufferedImage image = blankImage(3, 2);
        image.setRGB(0, 0, red);
        image.setRGB(1, 0, red);
        image.setRGB(2, 1, red);

        List<Group> result = finder.findConnectedGroups(image);

        // group 1: (0,0) and (1,0) -> size 2, centroid x = (0+1)/2 = 0, y = 0
        // group 2: (2,1) alone     -> size 1, centroid (2, 1)
        // bigger group comes first (descending order)
        List<Group> expected = List.of(
            new Group(2, new Coordinate(0, 0)),
            new Group(1, new Coordinate(2, 1))
        );
        assertEquals(expected, result);
    }
}