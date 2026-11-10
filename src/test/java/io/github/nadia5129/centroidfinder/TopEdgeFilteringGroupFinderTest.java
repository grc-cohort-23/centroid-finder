package io.github.nadia5129.centroidfinder;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.util.List;
import org.junit.jupiter.api.Test;

public class TopEdgeFilteringGroupFinderTest {

    // Fake ImageGroupFinder: always returns the groups we give it,
    // and remembers which image it was asked about
    private static class FakeGroupFinder implements ImageGroupFinder {
        private final List<Group> groups;
        BufferedImage lastImage;

        FakeGroupFinder(List<Group> groups) {
            this.groups = groups;
        }

        @Override
        public List<Group> findConnectedGroups(BufferedImage image) {
            lastImage = image;
            return groups;
        }
    }

    private final BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);

    // A big strip at the top and a smaller salamander lower down -> only the salamander is kept
    @Test
    void findConnectedGroups_stripBiggerThanSalamander_removesStrip() {
        // Arrange
        Group strip = new Group(6765, new Coordinate(307, 7));
        Group salamander = new Group(6305, new Coordinate(40, 578));
        FakeGroupFinder fake = new FakeGroupFinder(List.of(strip, salamander));
        TopEdgeFilteringGroupFinder finder = new TopEdgeFilteringGroupFinder(fake, 30);

        // Act
        List<Group> result = finder.findConnectedGroups(image);

        // Assert
        assertEquals(List.of(salamander), result);
    }

    // A group exactly on the margin line (y == margin) is kept
    @Test
    void findConnectedGroups_centroidOnMargin_isKept() {
        // Arrange
        Group onLine = new Group(50, new Coordinate(100, 30));
        FakeGroupFinder fake = new FakeGroupFinder(List.of(onLine));
        TopEdgeFilteringGroupFinder finder = new TopEdgeFilteringGroupFinder(fake, 30);

        // Act
        List<Group> result = finder.findConnectedGroups(image);

        // Assert
        assertEquals(List.of(onLine), result);
    }

    // A group one pixel above the margin line (y == margin - 1) is removed
    @Test
    void findConnectedGroups_centroidJustAboveMargin_isRemoved() {
        // Arrange
        Group justAbove = new Group(50, new Coordinate(100, 29));
        FakeGroupFinder fake = new FakeGroupFinder(List.of(justAbove));
        TopEdgeFilteringGroupFinder finder = new TopEdgeFilteringGroupFinder(fake, 30);

        // Act
        List<Group> result = finder.findConnectedGroups(image);

        // Assert
        assertTrue(result.isEmpty());
    }

    // Only groups at the top -> empty list (so the tracker will write -1,-1)
    @Test
    void findConnectedGroups_allGroupsAtTop_returnsEmpty() {
        // Arrange
        FakeGroupFinder fake = new FakeGroupFinder(List.of(
            new Group(6000, new Coordinate(300, 5)),
            new Group(300, new Coordinate(800, 20))));
        TopEdgeFilteringGroupFinder finder = new TopEdgeFilteringGroupFinder(fake, 30);

        // Act
        List<Group> result = finder.findConnectedGroups(image);

        // Assert
        assertTrue(result.isEmpty());
    }

    // The kept groups stay in the same order (biggest first)
    @Test
    void findConnectedGroups_keepsOriginalOrder() {
        // Arrange
        Group big = new Group(500, new Coordinate(10, 200));
        Group strip = new Group(400, new Coordinate(50, 3));
        Group medium = new Group(300, new Coordinate(20, 300));
        Group small = new Group(100, new Coordinate(30, 400));
        FakeGroupFinder fake = new FakeGroupFinder(List.of(big, strip, medium, small));
        TopEdgeFilteringGroupFinder finder = new TopEdgeFilteringGroupFinder(fake, 30);

        // Act
        List<Group> result = finder.findConnectedGroups(image);

        // Assert
        assertEquals(List.of(big, medium, small), result);
    }

    // No groups found at all -> empty list
    @Test
    void findConnectedGroups_noGroups_returnsEmpty() {
        // Arrange
        FakeGroupFinder fake = new FakeGroupFinder(List.of());
        TopEdgeFilteringGroupFinder finder = new TopEdgeFilteringGroupFinder(fake, 30);

        // Act + Assert
        assertTrue(finder.findConnectedGroups(image).isEmpty());
    }

    // Margin of 0 -> nothing is removed
    @Test
    void findConnectedGroups_zeroMargin_keepsEverything() {
        // Arrange
        Group atTop = new Group(100, new Coordinate(5, 0));
        Group lower = new Group(50, new Coordinate(5, 200));
        FakeGroupFinder fake = new FakeGroupFinder(List.of(atTop, lower));
        TopEdgeFilteringGroupFinder finder = new TopEdgeFilteringGroupFinder(fake, 0);

        // Act + Assert
        assertEquals(List.of(atTop, lower), finder.findConnectedGroups(image));
    }

    // The same image is passed through to the wrapped group finder
    @Test
    void findConnectedGroups_passesImageToWrappedFinder() {
        // Arrange
        FakeGroupFinder fake = new FakeGroupFinder(List.of());
        TopEdgeFilteringGroupFinder finder = new TopEdgeFilteringGroupFinder(fake, 30);

        // Act
        finder.findConnectedGroups(image);

        // Assert
        assertSame(image, fake.lastImage);
    }

    // Negative margin makes no sense -> IllegalArgumentException
    @Test
    void constructor_negativeMargin_throwsException() {
        FakeGroupFinder fake = new FakeGroupFinder(List.of());

        assertThrows(IllegalArgumentException.class, () -> new TopEdgeFilteringGroupFinder(fake, -1));
    }
}
