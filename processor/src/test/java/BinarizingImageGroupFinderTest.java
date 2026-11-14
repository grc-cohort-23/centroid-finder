import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for BinarizingImageGroupFinder.
 *
 * This class's only job is to connect two other objects:
 *   image -> binarizer -> 2D array -> groupFinder -> list of groups
 *
 * So the tests use FAKE versions of both (no external libraries, just JUnit).
 * Each fake records what it received and returns something we chose ahead of
 * time. That lets us check that BinarizingImageGroupFinder passes the right
 * thing to the right place, without depending on whether the real binarizer
 * or the real DFS group finder are correct.
 */
public class BinarizingImageGroupFinderTest {

    // ------------------------------------------------------------------
    // Fakes
    // ------------------------------------------------------------------

    /**
     * Fake binarizer: remembers which image it got, counts how many times
     * it was called, and returns a 2D array the test chooses.
     */
    private static class FakeBinarizer implements ImageBinarizer {
        private final int[][] arrayToReturn;
        BufferedImage receivedImage;
        int callCount = 0;

        FakeBinarizer(int[][] arrayToReturn) {
            this.arrayToReturn = arrayToReturn;
        }

        @Override
        public int[][] toBinaryArray(BufferedImage image) {
            receivedImage = image;
            callCount++;
            return arrayToReturn;
        }

        @Override
        public BufferedImage toBufferedImage(int[][] image) {
            // Not used by BinarizingImageGroupFinder. Fail loudly if it is.
            throw new UnsupportedOperationException("toBufferedImage should not be called");
        }
    }

    /**
     * Fake group finder: remembers which array it got, counts calls, and
     * returns a list the test chooses.
     */
    private static class FakeGroupFinder implements BinaryGroupFinder {
        private final List<Group> groupsToReturn;
        int[][] receivedArray;
        int callCount = 0;

        FakeGroupFinder(List<Group> groupsToReturn) {
            this.groupsToReturn = groupsToReturn;
        }

        @Override
        public List<Group> findConnectedGroups(int[][] image) {
            receivedArray = image;
            callCount++;
            return groupsToReturn;
        }
    }

    // Small helpers so each test reads clearly
    private static BufferedImage anyImage() {
        return new BufferedImage(3, 2, BufferedImage.TYPE_INT_RGB);
    }

    private static int[][] anyArray() {
        return new int[][] {
            {1, 0, 1},
            {0, 1, 0}
        };
    }

    private static List<Group> someGroups() {
        List<Group> groups = new ArrayList<>();
        groups.add(new Group(5, new Coordinate(1, 2)));
        groups.add(new Group(2, new Coordinate(3, 4)));
        return groups;
    }

    // ------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------

    @Test
    void findConnectedGroups_passesImageToBinarizer() {
        // The exact image given to our method should be the one the
        // binarizer receives (not a copy, not null).
        FakeBinarizer binarizer = new FakeBinarizer(anyArray());
        FakeGroupFinder groupFinder = new FakeGroupFinder(someGroups());
        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        BufferedImage image = anyImage();
        finder.findConnectedGroups(image);

        assertSame(image, binarizer.receivedImage);
    }

    @Test
    void findConnectedGroups_passesBinarizerOutputToGroupFinder() {
        // Whatever array the binarizer returns must be what the group
        // finder receives. Catches passing the wrong variable (e.g. a new
        // empty array, or forgetting to use binaryImage).
        int[][] binaryArray = anyArray();
        FakeBinarizer binarizer = new FakeBinarizer(binaryArray);
        FakeGroupFinder groupFinder = new FakeGroupFinder(someGroups());
        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        finder.findConnectedGroups(anyImage());

        assertSame(binaryArray, groupFinder.receivedArray);
    }

    @Test
    void findConnectedGroups_returnsGroupFinderResult() {
        // The list the group finder returns should come straight back.
        // This is the test that fails if the method still says "return null".
        List<Group> expected = someGroups();
        FakeBinarizer binarizer = new FakeBinarizer(anyArray());
        FakeGroupFinder groupFinder = new FakeGroupFinder(expected);
        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        List<Group> result = finder.findConnectedGroups(anyImage());

        assertSame(expected, result);
    }

    @Test
    void findConnectedGroups_noGroupsFound_returnsEmptyList() {
        // Edge case: an image with no matching pixels. The group finder
        // returns an empty list, and that empty list (not null) comes back.
        List<Group> empty = new ArrayList<>();
        FakeBinarizer binarizer = new FakeBinarizer(new int[][] {{0, 0}, {0, 0}});
        FakeGroupFinder groupFinder = new FakeGroupFinder(empty);
        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        List<Group> result = finder.findConnectedGroups(anyImage());

        assertSame(empty, result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findConnectedGroups_callsEachHelperExactlyOnce() {
        // The work should be done once per call: binarize once, find
        // groups once. Catches accidental duplicate calls (slow on big
        // images) or skipping a step.
        FakeBinarizer binarizer = new FakeBinarizer(anyArray());
        FakeGroupFinder groupFinder = new FakeGroupFinder(someGroups());
        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        finder.findConnectedGroups(anyImage());

        assertEquals(1, binarizer.callCount, "binarizer should be called once");
        assertEquals(1, groupFinder.callCount, "group finder should be called once");
    }

    @Test
    void findConnectedGroups_calledTwice_usesEachNewImage() {
        // Calling the method again with a different image should binarize
        // that NEW image. Catches code that caches the first result.
        FakeBinarizer binarizer = new FakeBinarizer(anyArray());
        FakeGroupFinder groupFinder = new FakeGroupFinder(someGroups());
        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        BufferedImage first = anyImage();
        BufferedImage second = anyImage();
        finder.findConnectedGroups(first);
        finder.findConnectedGroups(second);

        assertSame(second, binarizer.receivedImage);
        assertEquals(2, binarizer.callCount);
        assertEquals(2, groupFinder.callCount);
    }
}