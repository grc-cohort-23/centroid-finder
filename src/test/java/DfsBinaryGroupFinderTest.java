import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;
 
    public class DfsBinaryGroupFinderTest {
    private final BinaryGroupFinder finder = new DfsBinaryGroupFinder();
 
    private static Group group(int size, int x, int y) {
        return new Group(size, new Coordinate(x, y));
    
}


// without output
 @Test
    public void testNullImage() {
        assertThrows(NullPointerException.class, () -> finder.findConnectedGroups(null));
    }
 
    @Test
    public void testNullRow() {
        int[][] image = {{1, 0}, null, {0, 1}};
        assertThrows(NullPointerException.class, () -> finder.findConnectedGroups(image));
    }


    // w zeros 
     @Test
    public void testSingleZero() {
        assertTrue(finder.findConnectedGroups(new int[][] {{0}}).isEmpty());
    }
 
    @Test
    public void testAllZeros() {
        int[][] image = {{0, 0, 0}, {0, 0, 0}};
        assertTrue(finder.findConnectedGroups(image).isEmpty());
    }

     @Test
    public void testNonRectangularLongerLaterRow() {
        int[][] image = {{1, 0}, {0, 1, 1}};
        assertThrows(IllegalArgumentException.class, () -> finder.findConnectedGroups(image));
    }

    @Test
    public void testHorizontalLine() {
        assertEquals(List.of(group(3, 1, 0)), finder.findConnectedGroups(new int[][] {{1, 1, 1}}));
    }
 
    @Test
    public void testVerticalLine() {
        int[][] image = {{1}, {1}, {1}, {1}};
        // sumY = 0+1+2+3 = 6, 6 / 4 = 1
        assertEquals(List.of(group(4, 0, 1)), finder.findConnectedGroups(image));
    }

     @Test
    public void testDiagonalPixelsAreNotConnected() {
        int[][] image = {
            {1, 0},
            {0, 1}
        };
        // equal size, so the larger x comes first
        assertEquals(List.of(group(1, 1, 1), group(1, 0, 0)), finder.findConnectedGroups(image));
    }
     @Test
    public void testTiesBrokenByXDescending() {
        int[][] image = {{1, 0, 1, 0, 1}};
        assertEquals(List.of(group(1, 4, 0), group(1, 2, 0), group(1, 0, 0)),
            finder.findConnectedGroups(image));
    }
 

}