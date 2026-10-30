import java.awt.image.BufferedImage;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BinarizingImageGroupFinderTest {

    @Test
    public void passesImageAndBinaryArrayCorrectly() {
        BufferedImage image =
                new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

        int[][] binary = {{1}};

        List<Group> expected =
                List.of(new Group(1, new Coordinate(0, 0)));

        ImageBinarizer fakeBinarizer = new ImageBinarizer() {
            @Override
            public int[][] toBinaryArray(BufferedImage input) {
                assertSame(image, input);
                return binary;
            }

            @Override
            public BufferedImage toBufferedImage(int[][] input) {
                throw new AssertionError("Should not be called");
            }
        };

        BinaryGroupFinder fakeGroupFinder = input -> {
            assertSame(binary, input);
            return expected;
        };

        BinarizingImageGroupFinder finder =
                new BinarizingImageGroupFinder(
                        fakeBinarizer, fakeGroupFinder
                );

        assertSame(expected, finder.findConnectedGroups(image));
    }

    @Test
    public void returnsEmptyListWhenNoGroupsAreFound() {
        ImageBinarizer fakeBinarizer = createFakeBinarizer(
                new int[][] {{0}}
        );

        List<Group> expected = List.of();

        BinaryGroupFinder fakeGroupFinder = input -> expected;

        BinarizingImageGroupFinder finder =
                new BinarizingImageGroupFinder(
                        fakeBinarizer, fakeGroupFinder
                );

        BufferedImage image =
                new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

        assertEquals(expected, finder.findConnectedGroups(image));
    }

    @Test
    public void preservesAllGroupsAndTheirOrder() {
        ImageBinarizer fakeBinarizer = createFakeBinarizer(
                new int[][] {{1, 1, 0, 1}}
        );

        List<Group> expected = List.of(
                new Group(2, new Coordinate(0, 0)),
                new Group(1, new Coordinate(3, 0))
        );

        BinaryGroupFinder fakeGroupFinder = input -> expected;

        BinarizingImageGroupFinder finder =
                new BinarizingImageGroupFinder(
                        fakeBinarizer, fakeGroupFinder
                );

        BufferedImage image =
                new BufferedImage(4, 1, BufferedImage.TYPE_INT_RGB);

        assertEquals(expected, finder.findConnectedGroups(image));
    }

    private ImageBinarizer createFakeBinarizer(int[][] binary) {
        return new ImageBinarizer() {
            @Override
            public int[][] toBinaryArray(BufferedImage image) {
                return binary;
            }

            @Override
            public BufferedImage toBufferedImage(int[][] image) {
                throw new AssertionError("Should not be called");
            }
        };
    }
}