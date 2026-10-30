import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;

class DfsBinaryGroupFinderTest {

    private final DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

    private static Group group(int size, int x, int y) {
        return new Group(size, new Coordinate(x, y));
    }

    @Test
    void findsTwoConnectedGroupsAndTheirCentroids() {
        int[][] image = {
            {1, 1, 0, 0},
            {0, 1, 0, 1},
            {0, 0, 0, 1}
        };

        assertEquals(
            List.of(group(3, 0, 0), group(2, 3, 1)),
            finder.findConnectedGroups(image)
        );
    }

    @Test
    void returnsEmptyListForAllZeros() {
        int[][] image = {
            {0, 0, 0},
            {0, 0, 0}
        };

        assertEquals(List.of(), finder.findConnectedGroups(image));
    }

    @Test
    void countsSinglePixel() {
        assertEquals(
            List.of(group(1, 0, 0)),
            finder.findConnectedGroups(new int[][] {{1}})
        );
    }

    @Test
    void countsOneLargeConnectedRectangle() {
        int[][] image = {
            {1, 1, 1},
            {1, 1, 1}
        };

        assertEquals(
            List.of(group(6, 1, 0)),
            finder.findConnectedGroups(image)
        );
    }

    @Test
    void diagonalPixelsAreNotConnected() {
        int[][] image = {
            {1, 0, 0},
            {0, 1, 0},
            {0, 0, 1}
        };

        assertEquals(
            List.of(group(1, 2, 2), group(1, 1, 1), group(1, 0, 0)),
            finder.findConnectedGroups(image)
        );
    }

    @Test
    void sortsByDescendingSize() {
        int[][] image = {
            {1, 0, 0, 0},
            {0, 0, 0, 1},
            {0, 0, 0, 1},
            {0, 0, 0, 1}
        };

        assertEquals(
            List.of(group(3, 3, 2), group(1, 0, 0)),
            finder.findConnectedGroups(image)
        );
    }

    @Test
    void breaksSizeTiesByDescendingCentroidX() {
        int[][] image = {
            {1, 0, 0, 1},
            {1, 0, 0, 1}
        };

        assertEquals(
            List.of(group(2, 3, 0), group(2, 0, 0)),
            finder.findConnectedGroups(image)
        );
    }

    @Test
    void breaksSizeAndXTiesByDescendingCentroidY() {
        int[][] image = {
            {1, 1, 0},
            {0, 0, 0},
            {1, 1, 0}
        };

        assertEquals(
            List.of(group(2, 0, 2), group(2, 0, 0)),
            finder.findConnectedGroups(image)
        );
    }

    @Test
    void usesIntegerDivisionForCentroids() {
        int[][] image = {
            {1, 1},
            {0, 1}
        };

        // x = (0 + 1 + 1) / 3 = 0; y = (0 + 0 + 1) / 3 = 0.
        assertEquals(
            List.of(group(3, 0, 0)),
            finder.findConnectedGroups(image)
        );
    }

    @Test
    void handlesSingleRowAndSingleColumn() {
        assertEquals(
            List.of(group(2, 1, 0), group(1, 4, 0)),
            finder.findConnectedGroups(new int[][] {{0, 1, 1, 0, 1}})
        );

        assertEquals(
            List.of(group(2, 0, 1), group(1, 0, 4)),
            finder.findConnectedGroups(new int[][] {
                {0}, {1}, {1}, {0}, {1}
            })
        );
    }

    @Test
    void canBeCalledAgainWithSameImage() {
        int[][] image = {
            {1, 0},
            {0, 1}
        };
        List<Group> expected = List.of(group(1, 1, 1), group(1, 0, 0));

        assertEquals(expected, finder.findConnectedGroups(image));
        assertEquals(expected, finder.findConnectedGroups(image));
        assertArrayEquals(new int[][] {{1, 0}, {0, 1}}, image);
    }

    @Test
    void rejectsNullImage() {
        assertThrows(NullPointerException.class,
            () -> finder.findConnectedGroups(null));
    }

    @Test
    void rejectsNullFirstRow() {
        assertThrows(NullPointerException.class,
            () -> finder.findConnectedGroups(new int[][] {null, {1}}));
    }

    @Test
    void rejectsNullLaterRow() {
        assertThrows(NullPointerException.class,
            () -> finder.findConnectedGroups(new int[][] {{1}, null}));
    }

    @Test
    void rejectsEmptyImage() {
        assertThrows(IllegalArgumentException.class,
            () -> finder.findConnectedGroups(new int[0][]));
    }

    @Test
    void rejectsEmptyRows() {
        assertThrows(IllegalArgumentException.class,
            () -> finder.findConnectedGroups(new int[][] {{}}));
    }

    @Test
    void rejectsNonRectangularImage() {
        assertThrows(IllegalArgumentException.class,
            () -> finder.findConnectedGroups(new int[][] {{1, 0}, {1}}));
    }

    @Test
    void rejectsValuesOtherThanZeroAndOne() {
        assertThrows(IllegalArgumentException.class,
            () -> finder.findConnectedGroups(new int[][] {{0, 2}}));

        assertThrows(IllegalArgumentException.class,
            () -> finder.findConnectedGroups(new int[][] {{1, -1}}));
    }
}
