import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class DfsBinaryGroupFinderTest {

  @Test
  public void testNullImage() {
    DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

    assertThrows(NullPointerException.class, () -> {
      finder.findConnectedGroups(null);
    });
  }

  @Test
  public void testNullRow() {
    DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

    int[][] image = {
        { 1, 0, 1 },
        null,
        { 0, 1, 0 }
    };

    assertThrows(NullPointerException.class, () -> {
      finder.findConnectedGroups(image);
    });
  }

  @Test
  public void testEmptyImage() {
    DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

    int[][] image = {};

    assertThrows(IllegalArgumentException.class, () -> {
      finder.findConnectedGroups(image);
    });
  }
  @Test
public void testEmptyRow() {
    DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

    int[][] image = {
        {1, 0, 1},
        {},
        {0, 1, 0}
    };

    assertThrows(IllegalArgumentException.class, () -> {
        finder.findConnectedGroups(image);
    });
}
@Test
public void testAllZeros() {
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
public void testMultipleConnectedPixels() {
    DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

    int[][] image = {
        {1, 1, 0},
        {0, 1, 0},
        {0, 1, 0}
    };

    List<Group> groups = finder.findConnectedGroups(image);

    assertEquals(1, groups.size());
    assertEquals(new Group(4, new Coordinate(0, 0)), groups.get(0));
}
@Test
public void testCentroidCalculation() {
    DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

    int[][] image = {
        {0, 1, 0, 0},
        {0, 1, 1, 0},
        {0, 0, 1, 0}
    };

    List<Group> groups = finder.findConnectedGroups(image);

    assertEquals(1, groups.size());
    assertEquals(new Group(4, new Coordinate(1, 1)), groups.get(0));
}
@Test
public void testDescendingSortOrder() {
    DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

    int[][] image = {
        {1, 0, 1, 1},
        {0, 0, 0, 1},
        {1, 1, 0, 0}
    };

    List<Group> groups = finder.findConnectedGroups(image);

    assertEquals(3, groups.size());

    assertEquals(new Group(3, new Coordinate(2, 0)), groups.get(0));
    assertEquals(new Group(2, new Coordinate(0, 2)), groups.get(1));
    assertEquals(new Group(1, new Coordinate(0, 0)), groups.get(2));
}
}