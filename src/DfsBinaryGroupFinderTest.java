import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

public class DfsBinaryGroupFinderTest {

    private final DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

    // ---------- Basic cases ----------

    @Test
    public void testSinglePixel() {
        int[][] image = {
            {1}
        };

        List<Group> expected = List.of(new Group(1, new Coordinate(0, 0)));
        List<Group> actual = finder.findConnectedGroups(image);

        assertEquals(expected, actual);
    }

    @Test
    public void testAllZeros() {
        int[][] image = {
            {0, 0},
            {0, 0}
        };

        List<Group> actual = finder.findConnectedGroups(image);

        assertTrue(actual.isEmpty());
    }

    @Test
    public void testAllOnes() {
        int[][] image = {
            {1, 1, 1},
            {1, 1, 1}
        };

        // x sum = 6, y sum = 3, size 6 -> (1, 0)
        List<Group> expected = List.of(new Group(6, new Coordinate(1, 0)));
        List<Group> actual = finder.findConnectedGroups(image);

        assertEquals(expected, actual);
    }

    @Test
    public void testHorizontalNeighborsConnect() {
        int[][] image = {
            {1, 1}
        };

        List<Group> expected = List.of(new Group(2, new Coordinate(0, 0)));
        List<Group> actual = finder.findConnectedGroups(image);

        assertEquals(expected, actual);
    }

    @Test
    public void testVerticalNeighborsConnect() {
        int[][] image = {
            {1},
            {1}
        };

        List<Group> expected = List.of(new Group(2, new Coordinate(0, 0)));
        List<Group> actual = finder.findConnectedGroups(image);

        assertEquals(expected, actual);
    }

    @Test
    public void testDiagonalDoesNotConnect() {
        int[][] image = {
            {1, 0},
            {0, 1}
        };

        List<Group> actual = finder.findConnectedGroups(image);

        assertEquals(2, actual.size());
        assertTrue(actual.contains(new Group(1, new Coordinate(0, 0))));
        assertTrue(actual.contains(new Group(1, new Coordinate(1, 1))));
    }

    @Test
    public void testLShape() {
        int[][] image = {
            {1, 0, 0},
            {1, 0, 0},
            {1, 1, 1}
        };

        // x sum = 3, y sum = 7, size 5 -> (0, 1)
        List<Group> expected = List.of(new Group(5, new Coordinate(0, 1)));
        List<Group> actual = finder.findConnectedGroups(image);

        assertEquals(expected, actual);
    }

    @Test
    public void testUShape() {
        int[][] image = {
            {1, 0, 1},
            {1, 0, 1},
            {1, 1, 1}
        };

        // x sum = 7, y sum = 8, size 7 -> (1, 1)
        List<Group> expected = List.of(new Group(7, new Coordinate(1, 1)));
        List<Group> actual = finder.findConnectedGroups(image);

        assertEquals(expected, actual);
    }

    // ---------- Centroid ----------

    @Test
    public void testCentroidXAndYNotSwapped() {
        int[][] image = {
            {0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0},
            {0, 0, 1, 1, 1}
        };

        // Pixels at x = 2,3,4 and y = 2 -> (3, 2)
        List<Group> expected = List.of(new Group(3, new Coordinate(3, 2)));
        List<Group> actual = finder.findConnectedGroups(image);

        assertEquals(expected, actual);
    }

    @Test
    public void testCentroidUsesIntegerDivision() {
        int[][] image = {
            {0, 0, 1, 1}
        };

        // x = (2 + 3) / 2 = 2.5 -> 2 with integer division
        List<Group> expected = List.of(new Group(2, new Coordinate(2, 0)));
        List<Group> actual = finder.findConnectedGroups(image);

        assertEquals(expected, actual);
    }

    // ---------- Sorting ----------

    @Test
    public void testGroupsSortedBySizeDescending() {
        int[][] image = {
            {1, 0, 1, 1},
            {0, 0, 0, 0},
            {1, 1, 1, 0}
        };

        List<Group> expected = List.of(
            new Group(3, new Coordinate(1, 2)),
            new Group(2, new Coordinate(2, 0)),
            new Group(1, new Coordinate(0, 0))
        );
        List<Group> actual = finder.findConnectedGroups(image);

        assertEquals(expected, actual);
    }

    @Test
    public void testSameSizeGroupsFollowCompareTo() {
        int[][] image = {
            {1, 0, 1}
        };

        List<Group> actual = finder.findConnectedGroups(image);

        // Whatever the tie-break is, the result should match a descending sort
        List<Group> sorted = new ArrayList<>(actual);
        sorted.sort(Collections.reverseOrder());

        assertEquals(2, actual.size());
        assertEquals(sorted, actual);
    }

    // ---------- Shapes ----------

    @Test
    public void testTwentyByTwentyGridReturnsAllGroupsLargestFirst() {
        int[][] image = new int[20][20];
        fillRectangle(image, 0, 0, 5, 5);
        fillRectangle(image, 7, 0, 3, 4);
        fillRectangle(image, 13, 0, 2, 2);
        image[19][19] = 1;

        List<Group> expected = List.of(
            new Group(25, new Coordinate(2, 2)),
            new Group(12, new Coordinate(8, 1)),
            new Group(4, new Coordinate(13, 0)),
            new Group(1, new Coordinate(19, 19))
        );
        List<Group> actual = finder.findConnectedGroups(image);

        assertEquals(expected.size(), actual.size());
        assertTrue(actual.containsAll(expected));
        assertEquals(expected, actual);
        assertDescendingGroupSizes(actual);
    }

    @Test
    public void testTwentyByTwentyGridFindsIrregularShapesAndSortsBySize() {
        int[][] image = new int[20][20];
        fillRectangle(image, 0, 0, 3, 3);
        image[1][1] = 0;

        image[0][6] = 1;
        image[0][7] = 1;
        image[0][8] = 1;
        image[1][7] = 1;
        image[2][7] = 1;

        fillRectangle(image, 13, 0, 1, 3);
        fillRectangle(image, 17, 5, 2, 1);
        image[19][19] = 1;

        List<Group> expected = List.of(
            new Group(8, new Coordinate(1, 1)),
            new Group(5, new Coordinate(7, 0)),
            new Group(3, new Coordinate(13, 1)),
            new Group(2, new Coordinate(17, 5)),
            new Group(1, new Coordinate(19, 19))
        );
        List<Group> actual = finder.findConnectedGroups(image);

        assertEquals(expected.size(), actual.size());
        assertTrue(actual.containsAll(expected));
        assertEquals(expected, actual);
        assertDescendingGroupSizes(actual);
    }

    @Test
    public void testWideGrid() {
        int[][] image = {
            {1, 1, 1, 1, 1}
        };

        // x sum = 10, size 5 -> (2, 0)
        List<Group> expected = List.of(new Group(5, new Coordinate(2, 0)));
        List<Group> actual = finder.findConnectedGroups(image);

        assertEquals(expected, actual);
    }

    @Test
    public void testTallGrid() {
        int[][] image = {
            {1},
            {1},
            {1},
            {1},
            {1}
        };

        // y sum = 10, size 5 -> (0, 2)
        List<Group> expected = List.of(new Group(5, new Coordinate(0, 2)));
        List<Group> actual = finder.findConnectedGroups(image);

        assertEquals(expected, actual);
    }

    @Test
    public void testGroupsInAllCorners() {
        int[][] image = {
            {1, 0, 1},
            {0, 0, 0},
            {1, 0, 1}
        };

        List<Group> actual = finder.findConnectedGroups(image);

        assertEquals(4, actual.size());
        assertTrue(actual.contains(new Group(1, new Coordinate(0, 0))));
        assertTrue(actual.contains(new Group(1, new Coordinate(2, 0))));
        assertTrue(actual.contains(new Group(1, new Coordinate(0, 2))));
        assertTrue(actual.contains(new Group(1, new Coordinate(2, 2))));
    }

    // ---------- Errors ----------

    @Test
    public void testNullArrayThrows() {
        assertThrows(NullPointerException.class, () -> finder.findConnectedGroups(null));
    }

    @Test
    public void testNullRowThrows() {
        int[][] image = {
            {1, 0},
            null
        };

        assertThrows(NullPointerException.class, () -> finder.findConnectedGroups(image));
    }

    @Test
    public void testEmptyArrayThrows() {
        int[][] image = new int[0][0];

        assertThrows(IllegalArgumentException.class, () -> finder.findConnectedGroups(image));
    }

    @Test
    public void testJaggedArrayThrows() {
        int[][] image = {
            {1, 0, 1},
            {1, 0}
        };

        assertThrows(IllegalArgumentException.class, () -> finder.findConnectedGroups(image));
    }

    @Test
    public void testInvalidValueThrows() {
        int[][] image = {
            {1, 2},
            {0, 1}
        };

        assertThrows(IllegalArgumentException.class, () -> finder.findConnectedGroups(image));
    }

    private void fillRectangle(int[][] image, int x, int y, int width, int height) {
        for (int row = y; row < y + height; row++) {
            for (int column = x; column < x + width; column++) {
                image[row][column] = 1;
            }
        }
    }

    private void assertDescendingGroupSizes(List<Group> groups) {
        for (int i = 1; i < groups.size(); i++) {
            assertTrue(
                groups.get(i - 1).size() >= groups.get(i).size(),
                "Group sizes should be in descending order"
            );
        }
    }
}
