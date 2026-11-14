 import static org.junit.jupiter.api.Assertions.*;
import java.awt.image.BufferedImage;
import java.util.List;
import org.junit.jupiter.api.Test;

public class BinarizingImageGroupFinderTest {

    // Pretend binarizer: returns the array we give it and remembers the image it received.
    private static class FakeBinarizer implements ImageBinarizer {
        private final int[][] arrayToReturn;
        BufferedImage receivedImage;

        FakeBinarizer(int[][] arrayToReturn) {
            this.arrayToReturn = arrayToReturn;
        }

        @Override
        public int[][] toBinaryArray(BufferedImage image) {
            receivedImage = image;
            return arrayToReturn;
        }

        @Override
        public BufferedImage toBufferedImage(int[][] image) {
            return null; 
        }
    }

    // Pretend group finder: returns the groups we give it and remembers the array it received.
    private static class FakeGroupFinder implements BinaryGroupFinder {
        private final List<Group> groupsToReturn;
        int[][] receivedArray;

        FakeGroupFinder(List<Group> groupsToReturn) {
            this.groupsToReturn = groupsToReturn;
        }

        @Override
        public List<Group> findConnectedGroups(int[][] image) {
            receivedArray = image;
            return groupsToReturn;
        }
    }

    // The image we give the finder should go straight to the binarizer, unchanged.
    @Test
    void passesImageToBinarizer() {
        FakeBinarizer binarizer = new FakeBinarizer(new int[][] {{1}});
        FakeGroupFinder groupFinder = new FakeGroupFinder(List.of());
        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);
        BufferedImage image = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);

        finder.findConnectedGroups(image);

        assertSame(image, binarizer.receivedImage);
    }

    // Whatever black-and-white array the binarizer produces should be handed to the group finder, unchanged.
    @Test
    void passesBinarizerOutputToGroupFinder() {
        int[][] binaryArray = {
            {1, 0},
            {0, 1}
        };
        FakeBinarizer binarizer = new FakeBinarizer(binaryArray);
        FakeGroupFinder groupFinder = new FakeGroupFinder(List.of());
        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        finder.findConnectedGroups(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB));

        assertSame(binaryArray, groupFinder.receivedArray);
    }

    //The list of groups the group finder gives back is exactly what the finder returns to us nothing added, removed, or reordered
    @Test
    void returnsGroupFinderResult() {
        List<Group> expected = List.of(
            new Group(5, new Coordinate(1, 1)),
            new Group(2, new Coordinate(0, 0)));
        FakeBinarizer binarizer = new FakeBinarizer(new int[][] {{1}});
        FakeGroupFinder groupFinder = new FakeGroupFinder(expected);
        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        List<Group> actual = finder.findConnectedGroups(
            new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB));

        assertEquals(expected, actual);
    }
}