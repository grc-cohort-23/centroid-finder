
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;

public class DfsBinaryGroupFinderTest {

    @Test
    void testNoGroups() {
        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        int[][] image = {
            {0, 0, 0},
            {0, 0, 0},
            {0, 0, 0}
        };

        List<Group> groups = finder.findConnectedGroups(image);

        assertTrue(groups.isEmpty());
    }

    @Test
    void testSingleGroup() {
        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        int[][] image = {
            {1, 1, 0},
            {1, 1, 0},
            {0, 0, 0}
        };

        List<Group> groups = finder.findConnectedGroups(image);

        assertEquals(1, groups.size());
    }

    @Test
    void testMultipleGroups() {
        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        int[][] image = {
            {1, 1, 0, 0},
            {1, 0, 0, 1},
            {0, 0, 1, 1}
        };

        List<Group> groups = finder.findConnectedGroups(image);

        assertEquals(2, groups.size());
    }

    @Test
    void testLargeConnectedGroup() {
        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        int[][] image = new int[20][20];

        for (int r = 0; r < image.length; r++) {
            for (int c = 0; c < image[r].length; c++) {
                image[r][c] = 1;
            }
        }

        List<Group> groups = finder.findConnectedGroups(image);

        assertEquals(1, groups.size());
        assertEquals(0, new Group(400, new Coordinate(9, 9))
                .compareTo(groups.get(0)));
    }

    @Test
    void testDiagonalPixels() {
        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        int[][] image = {
            {1, 0, 0},
            {0, 1, 0},
            {0, 0, 1}
        };

        List<Group> groups = finder.findConnectedGroups(image);

        assertEquals(3, groups.size());
    }
}