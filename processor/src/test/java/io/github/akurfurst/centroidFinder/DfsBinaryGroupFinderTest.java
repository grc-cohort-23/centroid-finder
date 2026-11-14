package io.github.akurfurst.centroidFinder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

class DfsBinaryGroupFinderTest {
    private final DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

    @Test
    void returnsNoGroupsForAnImageWithoutForegroundPixels() {
        assertEquals(List.of(), finder.findConnectedGroups(new int[][] {
                { 0, 0 },
                { 0, 0 }
        }));
    }

    @Test
    void findsASingleForegroundPixel() {
        assertEquals(List.of(new Group(1, new Coordinate(1, 0))),
                finder.findConnectedGroups(new int[][] {
                        { 0, 1 }
                }));
    }

    @Test
    void findsHorizontallyConnectedPixels() {
        assertEquals(List.of(new Group(3, new Coordinate(1, 0))),
                finder.findConnectedGroups(new int[][] {
                        { 1, 1, 1 }
                }));
    }

    @Test
    void findsVerticallyConnectedPixels() {
        assertEquals(List.of(new Group(3, new Coordinate(0, 1))),
                finder.findConnectedGroups(new int[][] {
                        { 1 },
                        { 1 },
                        { 1 }
                }));
    }

    @Test
    void connectsPixelsThroughAnOrthogonalPath() {
        assertEquals(List.of(new Group(4, new Coordinate(0, 0))),
                finder.findConnectedGroups(new int[][] {
                        { 1, 1, 0 },
                        { 0, 1, 0 },
                        { 0, 1, 0 }
                }));
    }

    @Test
    void doesNotConnectPixelsDiagonally() {
        assertEquals(List.of(
                        new Group(1, new Coordinate(1, 1)),
                        new Group(1, new Coordinate(0, 0))),
                finder.findConnectedGroups(new int[][] {
                        { 1, 0 },
                        { 0, 1 }
                }));
    }

    // Confirms centroids are properly indentified
    @Test
    void computesCentroidsUsingIntegerDivision() {
        assertEquals(List.of(new Group(3, new Coordinate(0, 0))),
                finder.findConnectedGroups(new int[][] {
                        { 1, 1 },
                        { 1, 0 }
                }));
    }

    // Confirms groups are properly sized
    @Test
    void returnsGroupsInDescendingSizeOrder() {
        assertEquals(List.of(
                        new Group(3, new Coordinate(2, 2)),
                        new Group(2, new Coordinate(5, 4)),
                        new Group(2, new Coordinate(0, 0)),
                        new Group(1, new Coordinate(5, 0))),
                finder.findConnectedGroups(new int[][] {
                        { 1, 1, 0, 0, 0, 1, 0 },
                        { 0, 0, 0, 0, 0, 0, 0 },
                        { 0, 0, 1, 1, 0, 0, 0 },
                        { 0, 0, 1, 0, 0, 0, 0 },
                        { 0, 0, 0, 0, 0, 1, 1 }
                }));
    }

    // If sizes are equal, ensures centroid coordinates are then compared.
    @Test
    void breaksEqualSizeTiesByDescendingCentroidCoordinates() {
        assertEquals(List.of(
                        new Group(2, new Coordinate(4, 0)),
                        new Group(2, new Coordinate(0, 3)),
                        new Group(2, new Coordinate(0, 0))),
                finder.findConnectedGroups(new int[][] {
                        { 1, 1, 0, 0, 1, 1 },
                        { 0, 0, 0, 0, 0, 0 },
                        { 0, 0, 0, 0, 0, 0 },
                        { 1, 0, 0, 0, 0, 0 },
                        { 1, 0, 0, 0, 0, 0 }
                }));
    }

    @Test
    void treatsAnAllForegroundImageAsOneGroup() {
        assertEquals(List.of(new Group(6, new Coordinate(1, 0))),
                finder.findConnectedGroups(new int[][] {
                        { 1, 1, 1 },
                        { 1, 1, 1 }
                }));
    }

    @Test
    void rejectsANullImage() {
        assertThrows(NullPointerException.class, () -> finder.findConnectedGroups(null));
    }

    @Test
    void rejectsANullRow() {
        assertThrows(NullPointerException.class,
                () -> finder.findConnectedGroups(new int[][] { null }));
    }

    @Test
    void rejectsAnEmptyImage() {
        assertThrows(NullPointerException.class,
                () -> finder.findConnectedGroups(new int[0][]));
    }

    @Test
    void rejectsAnEmptyRow() {
        assertThrows(NullPointerException.class,
                () -> finder.findConnectedGroups(new int[][] { {} }));
    }

    @Test
    void rejectsRowsWithDifferentLengths() {
        assertThrows(IllegalArgumentException.class,
                () -> finder.findConnectedGroups(new int[][] {
                        { 1, 0 },
                        { 1 }
                }));
    }

    @Test
    void rejectsValuesAboveOne() {
        assertThrows(IllegalArgumentException.class,
                () -> finder.findConnectedGroups(new int[][] { { 2 } }));
    }

    @Test
    void rejectsNegativeValues() {
        assertThrows(IllegalArgumentException.class,
                () -> finder.findConnectedGroups(new int[][] { { -1 } }));
    }
}
