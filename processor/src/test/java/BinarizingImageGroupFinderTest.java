import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.util.List;

import org.junit.jupiter.api.Test;

public class BinarizingImageGroupFinderTest {

    // Fake binarizer: returns a preset array and remembers what it was given
    private static class StubBinarizer implements ImageBinarizer {
        private final int[][] toReturn;
        BufferedImage receivedImage;
        int calls = 0;

        StubBinarizer(int[][] toReturn) {
            this.toReturn = toReturn;
        }

        @Override
        public int[][] toBinaryArray(BufferedImage image) {
            calls++;
            receivedImage = image;
            return toReturn;
        }

        @Override
        public BufferedImage toBufferedImage(int[][] image) {
            return null; // not used by BinarizingImageGroupFinder
        }
    }

    // Fake group finder: returns a preset list and remembers what it was given
    private static class StubGroupFinder implements BinaryGroupFinder {
        private final List<Group> toReturn;
        int[][] receivedArray;
        int calls = 0;

        StubGroupFinder(List<Group> toReturn) {
            this.toReturn = toReturn;
        }

        @Override
        public List<Group> findConnectedGroups(int[][] image) {
            calls++;
            receivedArray = image;
            return toReturn;
        }
    }

    private final BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);

    // The original image goes straight to the binarizer
    @Test
    void passesOriginalImageToBinarizer() {
        StubBinarizer binarizer = new StubBinarizer(new int[][] {{1, 0}, {0, 1}});
        StubGroupFinder groupFinder = new StubGroupFinder(List.of());
        ImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        finder.findConnectedGroups(image);

        assertSame(image, binarizer.receivedImage);
    }

    // The group finder gets the exact array the binarizer made
    @Test
    void passesBinarizedArrayToGroupFinder() {
        int[][] binary = {{1, 0}, {0, 1}};
        StubBinarizer binarizer = new StubBinarizer(binary);
        StubGroupFinder groupFinder = new StubGroupFinder(List.of());
        ImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        finder.findConnectedGroups(image);

        assertSame(binary, groupFinder.receivedArray);
    }

    // Groups come back exactly as the group finder gave them, same order
    @Test
    void returnsGroupsFromGroupFinderUnchanged() {
        List<Group> groups = List.of(
            new Group(4, new Coordinate(2, 2)),
            new Group(1, new Coordinate(0, 0))
        );
        StubBinarizer binarizer = new StubBinarizer(new int[][] {{1, 1}, {1, 1}});
        StubGroupFinder groupFinder = new StubGroupFinder(groups);
        ImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        List<Group> result = finder.findConnectedGroups(image);

        assertEquals(groups, result);
    }

    // No groups found means an empty list, not null
    @Test
    void noGroups_returnsEmptyList() {
        StubBinarizer binarizer = new StubBinarizer(new int[][] {{0, 0}, {0, 0}});
        StubGroupFinder groupFinder = new StubGroupFinder(List.of());
        ImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        List<Group> result = finder.findConnectedGroups(image);

        assertTrue(result.isEmpty());
    }

    // Each helper is called exactly once per image
    @Test
    void callsBinarizerAndGroupFinderOnceEach() {
        StubBinarizer binarizer = new StubBinarizer(new int[][] {{1}});
        StubGroupFinder groupFinder = new StubGroupFinder(List.of(new Group(1, new Coordinate(0, 0))));
        ImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        finder.findConnectedGroups(image);

        assertEquals(1, binarizer.calls);
        assertEquals(1, groupFinder.calls);
    }

    // Works with the real DfsBinaryGroupFinder when only the binarizer is faked
    // Group A (size 2): (x0,y0) (x1,y0) -> centroid (0,0) since 1/2 = 0
    // Group B (size 1): (x2,y1)         -> centroid (2,1)
    @Test
    void withRealDfsFinder_returnsExpectedGroups() {
        StubBinarizer binarizer = new StubBinarizer(new int[][] {
            {1, 1, 0},
            {0, 0, 1}
        });
        ImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, new DfsBinaryGroupFinder());

        List<Group> result = finder.findConnectedGroups(image);

        List<Group> expected = List.of(
            new Group(2, new Coordinate(0, 0)),
            new Group(1, new Coordinate(2, 1))
        );
        assertEquals(expected, result);
    }

    // Exceptions from the helpers are not swallowed
    @Test
    void exceptionsFromHelpersPropagate() {
        BinaryGroupFinder okGroupFinder = new StubGroupFinder(List.of());
        ImageBinarizer throwingBinarizer = new StubBinarizer(null) {
            @Override
            public int[][] toBinaryArray(BufferedImage img) {
                throw new IllegalArgumentException("bad image");
            }
        };
        assertThrows(IllegalArgumentException.class,
            () -> new BinarizingImageGroupFinder(throwingBinarizer, okGroupFinder)
                .findConnectedGroups(image));

        ImageBinarizer okBinarizer = new StubBinarizer(new int[][] {{1}});
        BinaryGroupFinder throwingGroupFinder = img -> {
            throw new NullPointerException("bad array");
        };
        assertThrows(NullPointerException.class,
            () -> new BinarizingImageGroupFinder(okBinarizer, throwingGroupFinder)
                .findConnectedGroups(image));
    }
}