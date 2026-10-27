import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

public class DfsBinaryGroupFinderTest {

    @Test
    void findConnectedGroups_singlePixel_returnsOneGroupOfSizeOne() {
        // Arrange: a 3x3 image with one white pixel at row 1, column 2
        int[][] image = {
            {0, 0, 0},
            {0, 0, 1},
            {0, 0, 0}
        };
        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        // Act
        List<Group> groups = finder.findConnectedGroups(image);

        // Assert: row 1, column 2 means x = 2, y = 1
        List<Group> expected = List.of(new Group(1, new Coordinate(2, 1)));
        assertEquals(expected, groups);
    }

    // Reminder: x = column, y = row

    // ---------- Basic cases ----------

    // All zeros -> empty list
    @Test
    void findConnectedGroups_allZeros_returnsEmptyList() {
        // Arrange
        int[][] image = {
            {0, 0, 0},
            {0, 0, 0},
            {0, 0, 0}
        };
        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        // Act
        List<Group> groups = finder.findConnectedGroups(image);

        // Assert
        List<Group> expected = List.of();
        assertEquals(expected, groups);
    }

    // All ones -> one group covering the whole image, centroid in the middle
    @Test
    void findConnectedGroups_allOnes_returnsCentroidInMiddle() {
        // Arrange
        int[][] image = {
            {1, 1, 1},
            {1, 1, 1},
            {1, 1, 1}
        };
        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        // Act
        List<Group> groups = finder.findConnectedGroups(image);

        // Assert
        List<Group> expected = List.of(new Group(9, new Coordinate(1, 1)));
        assertEquals(expected, groups);
    }


    // Single pixel at (0, 0) -> top-left corner works

    @Test
    void findConnectedGroups_topLeft_returnsCentroid() {
        // Arrange
        int[][] image = {
            {1, 0, 0},
            {0, 0, 0},
            {0, 0, 0}
        };
        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        // Act
        List<Group> groups = finder.findConnectedGroups(image);

        // Assert
        List<Group> expected = List.of(new Group(1, new Coordinate(0, 0)));
        assertEquals(expected, groups);
    }


    // Single pixel at the bottom-right corner -> edges don't go out of bounds

    @Test
    void findConnectedGroups_bottomRight_returnsCentroid() {
        // Arrange
        int[][] image = {
            {0, 0, 0},
            {0, 0, 0},
            {0, 0, 1}
        };
        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        // Act
        List<Group> groups = finder.findConnectedGroups(image);

        // Assert
        List<Group> expected = List.of(new Group(1, new Coordinate(2, 2)));
        assertEquals(expected, groups);
    }

    // 1x1 image containing a 1

    @Test
    void findConnectedGroups_oneByOneWithOne_returnsCentroid() {
        // Arrange
        int[][] image = {
            {1}
        };
        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        // Act
        List<Group> groups = finder.findConnectedGroups(image);

        // Assert
        List<Group> expected = List.of(new Group(1, new Coordinate(0, 0)));
        assertEquals(expected, groups);
    }

    // 1x1 image containing a 0

    @Test
    void findConnectedGroups_oneByOneWithZero_returnsEmptyList() {
        // Arrange
        int[][] image = {
            {0}
        };
        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        // Act
        List<Group> groups = finder.findConnectedGroups(image);

        // Assert
        List<Group> expected = List.of();
        assertEquals(expected, groups);
    }

    // ---------- Connectivity ----------

    // Two pixels side by side (horizontal) -> one group of size 2

    @Test
    void findConnectedGroups_twoPixelsSideBySide_returnsOneGroup() {
        // Arrange
        int[][] image = {
            {0, 0, 0},
            {1, 1, 0},
            {0, 0, 0}
        };
        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        // Act
        List<Group> groups = finder.findConnectedGroups(image);

        // Assert
        List<Group> expected = List.of(new Group(2, new Coordinate(0, 1)));
        assertEquals(expected, groups);
    }

    // Two pixels stacked (vertical) -> one group of size 2

    @Test
    void findConnectedGroups_twoPixelsStacked_returnsOneGroup() {
        // Arrange
        int[][] image = {
            {0, 0, 0},
            {0, 1, 0},
            {0, 1, 0}
        };
        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        // Act
        List<Group> groups = finder.findConnectedGroups(image);

        // Assert
        List<Group> expected = List.of(new Group(2, new Coordinate(1, 1)));
        assertEquals(expected, groups);
    }

    // Two pixels touching only diagonally -> two separate groups of size 1

    @Test
    void findConnectedGroups_twoPixelsDiagonal_returnsTwoSeparateGroups() {
        // Arrange
        int[][] image = {
            {0, 0, 0},
            {1, 0, 0},
            {0, 1, 0}
        };
        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        // Act
        List<Group> groups = finder.findConnectedGroups(image);

        // Assert
        List<Group> expected = List.of(
            new Group(1, new Coordinate(1, 2)),
            new Group(1, new Coordinate(0, 1))
        );
        assertEquals(expected, groups);
    }

    // L-shape -> one group (DFS follows the turn)

    @Test
    void findConnectedGroups_lShaped_returnsOneGroup() {
        // Arrange
        int[][] image = {
            {1, 0, 0},
            {1, 0, 0},
            {1, 1, 0}
        };
        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        // Act
        List<Group> groups = finder.findConnectedGroups(image);

        // Assert
        List<Group> expected = List.of(
            new Group(4, new Coordinate(0, 1))
        );
        assertEquals(expected, groups);
    }

    // U-shape or snake -> one group (DFS goes back on itself)

    @Test
    void findConnectedGroups_uShaped_returnsOneGroup() {
        // Arrange
        int[][] image = {
            {1, 0, 1},
            {1, 0, 1},
            {1, 1, 1}
        };
        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        // Act
        List<Group> groups = finder.findConnectedGroups(image);

        // Assert
        List<Group> expected = List.of(
            new Group(7, new Coordinate(1, 1))
        );
        assertEquals(expected, groups);
    }

    // Ring with a hole of 0s in the middle -> one group

    @Test
    void findConnectedGroups_ringShaped_returnsOneGroup() {
        // Arrange
        int[][] image = {
            {1, 1, 1},
            {1, 0, 1},
            {1, 1, 1}
        };
        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        // Act
        List<Group> groups = finder.findConnectedGroups(image);

        // Assert
        List<Group> expected = List.of(
            new Group(8, new Coordinate(1, 1))
        );
        assertEquals(expected, groups);
    }

    // Two groups separated by a column of 0s

    @Test
    void findConnectedGroups_separatedByColumn_returnsTwoGroups() {
        // Arrange
        int[][] image = {
            {1, 0, 1},
            {1, 0, 1},
            {1, 0, 1}
        };
        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        // Act
        List<Group> groups = finder.findConnectedGroups(image);

        // Assert
        List<Group> expected = List.of(
            new Group(3, new Coordinate(2, 1)),
            new Group(3, new Coordinate(0, 1))
        );
        assertEquals(expected, groups);
    }

    // ---------- Centroid math ----------

    // Centroid needs integer division (e.g. pixels at x=0 and x=1 -> x=0)

    @Test
    void findConnectedGroups_centroidWithRemainder_usesIntegerDivision() {
        // Arrange
        int[][] image = {
            {0, 0, 0, 0},
            {1, 1, 1, 1},
            {0, 0, 0, 0}
        };
        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        // Act
        List<Group> groups = finder.findConnectedGroups(image);

        // Assert
        List<Group> expected = List.of(
            new Group(4, new Coordinate(1, 1))
        );
        assertEquals(expected, groups);
    }

    // Non-symmetric shape where you calculate the centroid by hand

    @Test
    void findConnectedGroups_nonSymmetricShape_returnsCorrectCentroid() {
        // Arrange
        int[][] image = {
            {0, 0, 0, 1},
            {1, 1, 1, 1},
            {0, 0, 0, 1}
        };
        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        // Act
        List<Group> groups = finder.findConnectedGroups(image);

        // Assert
        List<Group> expected = List.of(
            new Group(6, new Coordinate(2, 1))
        );
        assertEquals(expected, groups);
    }

    // ---------- Image shapes ----------

    // Wide image (1 row, many columns)

    @Test
    void findConnectedGroups_wideImage_returnsTwoGroups() {
        // Arrange
        int[][] image = {
            {1, 1, 0, 1, 1, 1}
        };

        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        // Act
        List<Group> groups = finder.findConnectedGroups(image);

        // Assert
        List<Group> expected = List.of(
            new Group(3, new Coordinate(4, 0)),
            new Group(2, new Coordinate(0, 0))
        );
        assertEquals(expected, groups);
    }

    // Tall image (many rows, 1 column)

    @Test
    void findConnectedGroups_tallImage_returnsTwoGroups() {
        // Arrange
        int[][] image = {
            {1},
            {1},
            {0},
            {1},
            {1},
            {1}
        };
        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        // Act
        List<Group> groups = finder.findConnectedGroups(image);

        // Assert
        List<Group> expected = List.of(
            new Group(3, new Coordinate(0, 4)),
            new Group(2, new Coordinate(0, 0))
        );
        assertEquals(expected, groups);
    }

    // Non-square image (e.g. 3x5) -> x and y aren't swapped

    @Test
    void findConnectedGroups_nonSquareImage_findsPixelsInFarColumns() {
        // Arrange
        int[][] image = {
            {0, 0, 0, 0, 0},
            {0, 0, 0, 1, 1},
            {0, 0, 0, 0, 1}
        };
        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        // Act
        List<Group> groups = finder.findConnectedGroups(image);

        // Assert
        List<Group> expected = List.of(new Group(3, new Coordinate(3, 1)));
        assertEquals(expected, groups);
    }

    // ---------- Sorting (descending: size, then x, then y) ----------

    // Groups of different sizes -> biggest first
    @Test
    void findConnectedGroups_differentSizes_sortsLargestFirst() {
        // Arrange
        int[][] image = {
            {1, 0, 1, 1},
            {0, 0, 1, 0}
        };

        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        // Act
        List<Group> groups = finder.findConnectedGroups(image);

        // Assert
        List<Group> expected = List.of(
            new Group(3, new Coordinate(2, 0)),
            new Group(1, new Coordinate(0, 0))
        );

        assertEquals(expected, groups);
    }
    // Same size, different x -> bigger x first
    @Test
    void findConnectedGroups_sameSize_sortsByXDescending() {
        // Arrange
        int[][] image = {
            {1, 0, 1}
        };
        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();
        // Act
        List<Group> groups = finder.findConnectedGroups(image);
        // Assert
        List<Group> expected = List.of(
            new Group(1, new Coordinate(2, 0)),
            new Group(1, new Coordinate(0, 0))
        );

        assertEquals(expected, groups);
    }
    // Same size, same x, different y -> bigger y first
    @Test
    void findConnectedGroups_sameSizeAndX_sortsByYDescending() {
        // Arrange
        int[][] image = {
            {1},
            {0},
            {1}
        };
        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();
        // Act
        List<Group> groups = finder.findConnectedGroups(image);
        // Assert
        List<Group> expected = List.of(
            new Group(1, new Coordinate(0, 2)),
            new Group(1, new Coordinate(0, 0))
        );

        assertEquals(expected, groups);
    }

    // ---------- Errors ----------

    // Null image -> NullPointerException

    @Test
    void findConnectedGroups_nullImage_throwsNullPointerException() {
        // Arrange
        int[][] image = null;
        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        // Act + Assert
        assertThrows(NullPointerException.class, () -> finder.findConnectedGroups(image));
    }

    // One null row -> NullPointerException

    @Test
    void findConnectedGroups_nullRow_throwsNullPointerException() {
        // Arrange
        int[][] image = {
            {0, 1, 0},
            null,
            {1, 0, 1}
        };
        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        // Act + Assert
        assertThrows(NullPointerException.class, () -> finder.findConnectedGroups(image));
    }

    // Empty array (new int[0][0]) -> IllegalArgumentException

    @Test
    void findConnectedGroups_emptyArray_throwsIllegalArgumentException() {
        // Arrange
        int[][] image = new int [0][0];
        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> finder.findConnectedGroups(image));
    }

    // Rows of different lengths -> IllegalArgumentException

    @Test
    void findConnectedGroups_differentLengthRows_throwsIllegalArgumentException() {
        // Arrange
        int[][] image = {
            {1, 0, 1, 1},
            {0, 0, 1}
        };
        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> finder.findConnectedGroups(image));
    }

    // A value that isn't 0 or 1 (like 2 or -1) -> IllegalArgumentException

    @Test
    void findConnectedGroups_illegalValue_throwsIllegalArgumentException() {
        // Arrange
        int[][] image = {
            {1, 0, 2, 1},
            {0, 0, 1, 0}
        };
        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> finder.findConnectedGroups(image));
    }
}
