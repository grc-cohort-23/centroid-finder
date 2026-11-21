package io.github.nadia5129.centroidfinder;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.util.List;

import org.junit.jupiter.api.Test;

public class BinarizingImageGroupFinderTest {

    // A pretend binarizer. It ignores the colors in the image and always
    // returns the grid we give it. It also remembers which image it was handed.
    private static class FakeBinarizer implements ImageBinarizer {
        int[][] gridToReturn;
        BufferedImage imageSeen;
        int timesCalled = 0;

        FakeBinarizer(int[][] gridToReturn) {
            this.gridToReturn = gridToReturn;
        }

        @Override
        public int[][] toBinaryArray(BufferedImage image) {
            imageSeen = image;
            timesCalled++;
            return gridToReturn;
        }

        @Override
        public BufferedImage toBufferedImage(int[][] image) {
            // Not used by BinarizingImageGroupFinder
            return null;
        }
    }

    // A pretend group finder. It doesn't search for anything and always
    // returns the groups we give it. It also remembers which grid it was handed.
    private static class FakeGroupFinder implements BinaryGroupFinder {
        List<Group> groupsToReturn;
        int[][] gridSeen;
        int timesCalled = 0;

        FakeGroupFinder(List<Group> groupsToReturn) {
            this.groupsToReturn = groupsToReturn;
        }

        @Override
        public List<Group> findConnectedGroups(int[][] image) {
            gridSeen = image;
            timesCalled++;
            return groupsToReturn;
        }
    }

    // A pretend distance finder: exact color match = 0, anything else = 1000.
    // Stands in for EuclideanColorDistance until Wave 3 is done.
    private static class ExactMatchDistance implements ColorDistanceFinder {
        @Override
        public double distance(int colorA, int colorB) {
            return colorA == colorB ? 0 : 1000;
        }
    }

    @Test
    void findConnectedGroups_returnsWhatGroupFinderReturns() {
        // Arrange
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        FakeBinarizer binarizer = new FakeBinarizer(new int[][] {
            {1, 0},
            {0, 0}
        });
        List<Group> fakeGroups = List.of(new Group(1, new Coordinate(0, 0)));
        FakeGroupFinder groupFinder = new FakeGroupFinder(fakeGroups);
        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        // Act
        List<Group> groups = finder.findConnectedGroups(image);

        // Assert
        assertEquals(fakeGroups, groups);
    }

    // ---------- Handoffs (use the fakes' "seen" fields) ----------

    // The image passed in is the exact same one the binarizer receives
    @Test
    void findConnectedGroups_passesImageToBinarizer() {
        // Arrange
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        FakeBinarizer binarizer = new FakeBinarizer(new int[][] {
            {1, 0},
            {0, 0}
        });
        List<Group> fakeGroups = List.of(new Group(1, new Coordinate(0, 0)));
        FakeGroupFinder groupFinder = new FakeGroupFinder(fakeGroups);
        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        // Act
        finder.findConnectedGroups(image);
        // Assert
        assertSame(image, binarizer.imageSeen);
    }

    // The grid the binarizer returns is the exact same one the group finder receives
    @Test
    void findConnectedGroups_passesGridToGroupFinder() {
        // Arrange
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        FakeBinarizer binarizer = new FakeBinarizer(new int[][] {
            {1, 0},
            {0, 0}
        });
        List<Group> fakeGroups = List.of(new Group(1, new Coordinate(0, 0)));
        FakeGroupFinder groupFinder = new FakeGroupFinder(fakeGroups);
        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        // Act
        finder.findConnectedGroups(image);
        // Assert
        assertSame(binarizer.gridToReturn, groupFinder.gridSeen);
    }

    // Each helper is called exactly once
    @Test
    void findConnectedGroups_callsEachHelperOnce() {
        // Arrange
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        FakeBinarizer binarizer = new FakeBinarizer(new int[][] {
            {1, 0},
            {0, 0}
        });
        List<Group> fakeGroups = List.of(new Group(1, new Coordinate(0, 0)));
        FakeGroupFinder groupFinder = new FakeGroupFinder(fakeGroups);
        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        // Act
        finder.findConnectedGroups(image);
        // Assert
        assertEquals(1, binarizer.timesCalled);
        assertEquals(1, groupFinder.timesCalled);
    }

    // ---------- Results ----------

    // Group finder returns an empty list -> result is an empty list
    @Test
    void findConnectedGroups_noGroupsFound_returnsEmptyList() {
        // Arrange
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        FakeBinarizer binarizer = new FakeBinarizer(new int[][] {
            {0, 0},
            {0, 0}
        });
        List<Group> fakeGroups = List.of();
        FakeGroupFinder groupFinder = new FakeGroupFinder(fakeGroups);
        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        List<Group> groups = finder.findConnectedGroups(image);

        assertEquals(List.of(), groups);
    }

    // Group finder returns several groups -> same groups come back in the same order
    @Test
    void findConnectedGroups_severalGroups_returnsSameGroupsInOrder() {
        // Arrange
        BufferedImage image = new BufferedImage(5, 3, BufferedImage.TYPE_INT_RGB);
        FakeBinarizer binarizer = new FakeBinarizer(new int[][] {
            {1, 1, 1, 0, 1},
            {0, 0, 0, 0, 0},
            {1, 1, 0, 0, 0}
        });
        // These match the grid above, listed biggest first
        List<Group> fakeGroups = List.of(
            new Group(3, new Coordinate(1, 0)),   // top row, left side
            new Group(2, new Coordinate(0, 2)),   // bottom row
            new Group(1, new Coordinate(4, 0))    // top row, far right
        );
        FakeGroupFinder groupFinder = new FakeGroupFinder(fakeGroups);
        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        // Act
        List<Group> groups = finder.findConnectedGroups(image);

        // Assert
        assertEquals(fakeGroups, groups);
    }

    // ---------- Real pieces together ----------

    // Real DistanceImageBinarizer + real DfsBinaryGroupFinder on a small painted image
    @Test
    void findConnectedGroups_realBinarizerAndDfs_findsCorrectGroups() {
        // Arrange
        // The image we paint (1 = white, 0 = black), 4 wide and 3 tall:
        //         x=0 x=1 x=2 x=3
        //   y=0 {  1,  1,  0,  0 }
        //   y=1 {  1,  0,  0,  1 }
        //   y=2 {  0,  0,  0,  1 }
        BufferedImage image = new BufferedImage(4, 3, BufferedImage.TYPE_INT_RGB);
        // setRGB takes (x, y), so column first
        image.setRGB(0, 0, 0xFFFFFF);
        image.setRGB(1, 0, 0xFFFFFF);
        image.setRGB(0, 1, 0xFFFFFF);
        image.setRGB(3, 1, 0xFFFFFF);
        image.setRGB(3, 2, 0xFFFFFF);

        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(new ExactMatchDistance(), 0xFFFFFF, 10);
        DfsBinaryGroupFinder groupFinder = new DfsBinaryGroupFinder();
        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        // Act
        List<Group> groups = finder.findConnectedGroups(image);

        // Assert
        List<Group> expected = List.of(
            // L-shape: (0,0), (1,0), (0,1) -> x = 1/3 = 0, y = 1/3 = 0
            new Group(3, new Coordinate(0, 0)),
            // right column: (3,1), (3,2) -> x = 6/2 = 3, y = 3/2 = 1
            new Group(2, new Coordinate(3, 1))
        );
        assertEquals(expected, groups);
    }
}
