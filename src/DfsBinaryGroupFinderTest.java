import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

public class DfsBinaryGroupFinderTest {

    private final BinaryGroupFinder finder = new DfsBinaryGroupFinder();

    // Image with no 1s should give back an empty list, not null
    @Test
    void allZeros_returnsEmptyList() {
        int[][] image = {
            {0, 0, 0},
            {0, 0, 0}
        };

        List<Group> result = finder.findConnectedGroups(image);

        assertTrue(result.isEmpty());
    }

    // Two separate groups, largest should come first
    // Group A (size 3): (x0,y0) (x1,y0) (x0,y1) -> centroid (0,0)
    // Group B (size 2): (x3,y1) (x3,y2)          -> centroid (3,1) since 3/2 = 1
    @Test
    void twoSeparateGroups_sortedLargestFirst() {
        int[][] image = {
            {1, 1, 0, 0},
            {1, 0, 0, 1},
            {0, 0, 0, 1}
        };

        List<Group> result = finder.findConnectedGroups(image);

        List<Group> expected = List.of(
            new Group(3, new Coordinate(0, 0)),
            new Group(2, new Coordinate(3, 1))
        );
        assertEquals(expected, result);
    }

    // Diagonal pixels do NOT connect, so this is two groups of size 1.
    // Same size, so the tie breaks on descending x: (1,1) comes before (0,0)
    @Test
    void diagonalPixels_areSeparateGroups_tieBrokenByDescendingX() {
        int[][] image = {
            {1, 0},
            {0, 1}
        };

        List<Group> result = finder.findConnectedGroups(image);

        List<Group> expected = List.of(
            new Group(1, new Coordinate(1, 1)),
            new Group(1, new Coordinate(0, 0))
        );
        assertEquals(expected, result);
    }

    // Whole 2 row x 3 column image is one group of 6.
    // x sum = 0+1+2+0+1+2 = 6, 6/6 = 1
    // y sum = 0+0+0+1+1+1 = 3, 3/6 = 0 (integer division, not 0.5)
    @Test
    void fullImageOfOnes_singleGroup_centroidUsesIntegerDivision() {
        int[][] image = {
            {1, 1, 1},
            {1, 1, 1}
        };

        List<Group> result = finder.findConnectedGroups(image);

        assertEquals(1, result.size());
        assertEquals(new Group(6, new Coordinate(1, 0)), result.get(0));
    }

    // Bad input: nulls throw NullPointerException, everything else invalid throws IllegalArgumentException
    @Test
    void invalidInput_throwsExpectedExceptions() {
        // null image and null row
        assertThrows(NullPointerException.class, () -> finder.findConnectedGroups(null));
        assertThrows(NullPointerException.class,
            () -> finder.findConnectedGroups(new int[][] {{1, 0}, null}));

        // empty image, empty row
        assertThrows(IllegalArgumentException.class,
            () -> finder.findConnectedGroups(new int[0][0]));
        assertThrows(IllegalArgumentException.class,
            () -> finder.findConnectedGroups(new int[][] {{}}));

        // not rectangular
        assertThrows(IllegalArgumentException.class,
            () -> finder.findConnectedGroups(new int[][] {{1, 0}, {1}}));

        // value that is not 0 or 1
        assertThrows(IllegalArgumentException.class,
            () -> finder.findConnectedGroups(new int[][] {{1, 2}, {0, 1}}));
    }
}