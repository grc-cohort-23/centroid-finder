import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

public class LargestGroupSelectorTest {

    @Test
    public void testLargestGroupInMiddle() {
        LargestGroupSelector selector = new LargestGroupSelector();

        List<Group> groups = List.of(
            new Group(4, new Coordinate(2, 3)),
            new Group(10, new Coordinate(8, 6)),
            new Group(7, new Coordinate(1, 9))
        );

        Coordinate result = selector.select(groups);

        assertEquals(new Coordinate(8, 6), result);
    }

    @Test
    public void testEmptyList() {
        LargestGroupSelector selector = new LargestGroupSelector();

        Coordinate result = selector.select(List.of());

        assertEquals(new Coordinate(-1, -1), result);
    }

    @Test
    public void testEqualSizesKeepsFirstGroup() {
        LargestGroupSelector selector = new LargestGroupSelector();

        List<Group> groups = List.of(
            new Group(10, new Coordinate(2, 3)),
            new Group(10, new Coordinate(8, 6))
        );

        Coordinate result = selector.select(groups);

        assertEquals(new Coordinate(2, 3), result);
    }
}