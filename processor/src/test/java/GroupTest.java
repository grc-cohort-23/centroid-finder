
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class GroupTest {

    @Test
    public void testCompareBySize() {
        Group small = new Group(2, new Coordinate(9, 9));
        Group large = new Group(5, new Coordinate(0, 0));

        assertTrue(small.compareTo(large) < 0);
        assertTrue(large.compareTo(small) > 0);
    }

    @Test
    public void testCompareByX() {
        Group first = new Group(5, new Coordinate(2, 9));
        Group second = new Group(5, new Coordinate(4, 0));

        assertTrue(first.compareTo(second) < 0);
    }

    @Test
    public void testCompareByY() {
        Group first = new Group(5, new Coordinate(2, 3));
        Group second = new Group(5, new Coordinate(2, 7));

        assertTrue(first.compareTo(second) < 0);
    }

    @Test
    public void testCompareEqualGroups() {
        Group first = new Group(5, new Coordinate(2, 3));
        Group second = new Group(5, new Coordinate(2, 3));

        assertEquals(0, first.compareTo(second));
    }

    @Test
    public void testCsvRow() {
        Group group = new Group(10, new Coordinate(4, 7));

        assertEquals("10,4,7", group.toCsvRow());
    }

    @Test
    public void testCsvRowAtOrigin() {
        Group group = new Group(1, new Coordinate(0, 0));

        assertEquals("1,0,0", group.toCsvRow());
    }
}
