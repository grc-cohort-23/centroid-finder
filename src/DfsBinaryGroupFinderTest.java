import static org.junit.jupiter.api.Assertions.*;
 
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// [[   |   Created BY AI   |   Tests generated without human intervention, implementation of tested file independently human made     ]]

public class DfsBinaryGroupFinderTest {
 
    private DfsBinaryGroupFinder finder;
 
    @BeforeEach
    void setUp() {
        finder = new DfsBinaryGroupFinder();
    }
 
    // ---- Helpers: adjust these two if Group/Coordinate use different accessor names ----
 
    private static void assertGroup(Group g, int size, int x, int y) {
        assertEquals(size, g.size(), "group size");
        assertEquals(x, g.centroid().x(), "centroid x");
        assertEquals(y, g.centroid().y(), "centroid y");
    }
 
    private static boolean hasGroup(List<Group> groups, int size, int x, int y) {
        for (Group g : groups) {
            if (g.size() == size && g.centroid().x() == x && g.centroid().y() == y) return true;
        }
        return false;
    }
 
    private static void assertSizesDescending(List<Group> groups) {
        for (int i = 1; i < groups.size(); i++) {
            assertTrue(groups.get(i - 1).size() >= groups.get(i).size(),
                    "groups should be in descending order of size");
        }
    }
 
    // ---- Basic cases ----
 
    @Test
    void singlePixel() {
        List<Group> groups = finder.findConnectedGroups(new int[][]{{1}});
        assertEquals(1, groups.size());
        assertGroup(groups.get(0), 1, 0, 0);
    }
 
    @Test
    void allZerosReturnsNoGroups() {
        List<Group> groups = finder.findConnectedGroups(new int[][]{
            {0, 0},
            {0, 0}
        });
        assertTrue(groups.isEmpty());
    }
 
    @Test
    void horizontalLine() {
        List<Group> groups = finder.findConnectedGroups(new int[][]{{1, 1, 1}});
        assertEquals(1, groups.size());
        assertGroup(groups.get(0), 3, 1, 0);
    }
 
    @Test
    void verticalLineUsesIntegerDivision() {
        List<Group> groups = finder.findConnectedGroups(new int[][]{
            {1},
            {1},
            {1},
            {1}
        });
        assertEquals(1, groups.size());
        // y centroid = (0+1+2+3)/4 = 6/4 = 1
        assertGroup(groups.get(0), 4, 0, 1);
    }
 
    @Test
    void fullGridIsOneGroup() {
        List<Group> groups = finder.findConnectedGroups(new int[][]{
            {1, 1, 1},
            {1, 1, 1},
            {1, 1, 1}
        });
        assertEquals(1, groups.size());
        assertGroup(groups.get(0), 9, 1, 1);
    }
 
    // ---- Connectivity ----
 
    @Test
    void diagonalPixelsAreNotConnected() {
        List<Group> groups = finder.findConnectedGroups(new int[][]{
            {1, 0},
            {0, 1}
        });
        assertEquals(2, groups.size());
        assertTrue(hasGroup(groups, 1, 0, 0));
        assertTrue(hasGroup(groups, 1, 1, 1));
    }
 
    @Test
    void checkerboardGivesManySingletons() {
        List<Group> groups = finder.findConnectedGroups(new int[][]{
            {1, 0, 1},
            {0, 1, 0},
            {1, 0, 1}
        });
        assertEquals(5, groups.size());
        for (Group g : groups) assertEquals(1, g.size());
    }
 
    @Test
    void uShapeIsOneGroup() {
        List<Group> groups = finder.findConnectedGroups(new int[][]{
            {1, 0, 1},
            {1, 0, 1},
            {1, 1, 1}
        });
        assertEquals(1, groups.size());
        // sumX = 7, sumY = 8, count = 7 -> (1, 1)
        assertGroup(groups.get(0), 7, 1, 1);
    }
 
    @Test
    void ringWithHoleIsOneGroup() {
        List<Group> groups = finder.findConnectedGroups(new int[][]{
            {1, 1, 1},
            {1, 0, 1},
            {1, 1, 1}
        });
        assertEquals(1, groups.size());
        assertGroup(groups.get(0), 8, 1, 1);
    }
 
    // ---- Multiple groups and ordering ----
 
    @Test
    void twoSeparateGroups() {
        List<Group> groups = finder.findConnectedGroups(new int[][]{
            {1, 1, 0, 0},
            {1, 0, 0, 1},
            {0, 0, 0, 1}
        });
        assertEquals(2, groups.size());
        // Group A: (0,0),(1,0),(0,1) -> size 3, centroid (1/3, 1/3) = (0, 0)
        assertTrue(hasGroup(groups, 3, 0, 0));
        // Group B: (3,1),(3,2) -> size 2, centroid (3, 3/2) = (3, 1)
        assertTrue(hasGroup(groups, 2, 3, 1));
        assertSizesDescending(groups);
    }
 
    @Test
    void groupsAreSortedDescending() {
        List<Group> groups = finder.findConnectedGroups(new int[][]{
            {1, 0, 1, 1, 1, 0, 1, 1}
        });
        assertEquals(3, groups.size());
        assertSizesDescending(groups);
        assertEquals(3, groups.get(0).size());
        assertEquals(2, groups.get(1).size());
        assertEquals(1, groups.get(2).size());
    }
 
    // ---- Non-square images (catches row/column index mix-ups) ----
 
    @Test
    void wideImage() {
        List<Group> groups = finder.findConnectedGroups(new int[][]{
            {0, 0, 0, 0, 1},
            {0, 0, 0, 0, 1}
        });
        assertEquals(1, groups.size());
        // (4,0),(4,1) -> centroid (4, 1/2) = (4, 0)
        assertGroup(groups.get(0), 2, 4, 0);
    }
 
    @Test
    void tallImage() {
        List<Group> groups = finder.findConnectedGroups(new int[][]{
            {1, 0},
            {1, 0},
            {1, 0},
            {1, 0},
            {1, 1}
        });
        assertEquals(1, groups.size());
        // sumX = 1, sumY = 0+1+2+3+4+4 = 14, count = 6 -> (0, 2)
        assertGroup(groups.get(0), 6, 0, 2);
    }
 
    // ---- Misc ----
 
    @Test
    void doesNotModifyInputImage() {
        int[][] image = {
            {1, 0, 1},
            {1, 1, 0}
        };
        int[][] copy = {
            {1, 0, 1},
            {1, 1, 0}
        };
        finder.findConnectedGroups(image);
        assertArrayEquals(copy, image);
    }
 
    @Test
    void largeSingleGroup() {
        int n = 50;
        int[][] image = new int[n][n];
        for (int[] row : image) java.util.Arrays.fill(row, 1);
 
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(1, groups.size());
        // sumX = 50 * (0+...+49) = 61250; 61250 / 2500 = 24
        assertGroup(groups.get(0), 2500, 24, 24);
    }

    //Multiple bodies test
    @Test
    void multipleBodiesSeparatedByZeros() {
        List<Group> groups = finder.findConnectedGroups(new int[][]{
            {1, 1, 0, 1, 1},
            {1, 1, 0, 1, 1},
            {0, 0, 0, 0, 0},
            {1, 0, 0, 1, 1},
            {1, 0, 0, 0, 0}
        });
        assertEquals(4, groups.size());
        // top-left 2x2: size 4, centroid (2/4, 2/4) = (0, 0)
        assertTrue(hasGroup(groups, 4, 0, 0));
        // top-right 2x2: size 4, centroid (14/4, 2/4) = (3, 0)
        assertTrue(hasGroup(groups, 4, 3, 0));
        // bottom-left vertical pair: size 2, centroid (0, 7/2) = (0, 3)
        assertTrue(hasGroup(groups, 2, 0, 3));
        // bottom-right horizontal pair: size 2, centroid (7/2, 3) = (3, 3)
        assertTrue(hasGroup(groups, 2, 3, 3));
        assertSizesDescending(groups);
    }
}