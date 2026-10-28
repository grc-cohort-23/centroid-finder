import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

public class BinarizingImageGroupFinderTest {
    private final BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(
        new DistanceImageBinarizer(new EuclideanColorDistance(), 0xFFFFFF, 1),
        new DfsBinaryGroupFinder()
    );

    @Test
    public void testLargeAllBlackImageHasNoGroups() {
        int[][] binary = new int[20][20];

        assertTrue(finder.findConnectedGroups(toImage(binary)).isEmpty());
    }

    @Test
    public void testLargeAllWhiteImageIsOneGroup() {
        int[][] binary = new int[20][20];
        for (int[] row : binary) {
            Arrays.fill(row, 1);
        }

        List<Group> actual = finder.findConnectedGroups(toImage(binary));

        assertEquals(List.of(new Group(400, new Coordinate(9, 9))), actual);
    }

    @Test
    public void testLargeCheckerboardKeepsDiagonalPixelsSeparate() {
        int[][] binary = new int[20][20];
        for (int y = 0; y < binary.length; y++) {
            for (int x = 0; x < binary[y].length; x++) {
                binary[y][x] = (x + y) % 2 == 0 ? 1 : 0;
            }
        }

        List<Group> actual = finder.findConnectedGroups(toImage(binary));

        assertEquals(200, actual.size());
        assertTrue(actual.stream().allMatch(group -> group.size() == 1));
        assertDescending(actual);
    }

    @Test
    public void testLargeImageFindsSeparatedGroupsAndOrdersThemBySize() {
        int[][] binary = new int[20][20];
        fillRectangle(binary, 0, 0, 8, 7);
        fillRectangle(binary, 10, 0, 5, 4);
        for (int y = 10; y < 16; y++) {
            binary[y][19] = 1;
        }
        binary[19][0] = 1;

        List<Group> expected = List.of(
            new Group(56, new Coordinate(3, 3)),
            new Group(20, new Coordinate(12, 1)),
            new Group(6, new Coordinate(19, 12)),
            new Group(1, new Coordinate(0, 19))
        );

        List<Group> actual = finder.findConnectedGroups(toImage(binary));

        assertEquals(expected, actual);
        assertDescending(actual);
    }

    @Test
    public void testLargeSerpentineComponentRemainsConnected() {
        int[][] binary = new int[20][20];
        for (int y = 0; y < 20; y += 2) {
            Arrays.fill(binary[y], 1);
            if (y < 19) {
                binary[y + 1][(y / 2) % 2 == 0 ? 19 : 0] = 1;
            }
        }

        List<Group> actual = finder.findConnectedGroups(toImage(binary));

        assertEquals(List.of(new Group(210, new Coordinate(9, 9))), actual);
    }

    @Test
    public void testLargeImageWithHoleAndIsolatedInnerGroup() {
        int[][] binary = new int[20][20];
        for (int[] row : binary) {
            Arrays.fill(row, 1);
        }
        for (int y = 6; y < 14; y++) {
            Arrays.fill(binary[y], 6, 14, 0);
        }
        fillRectangle(binary, 8, 8, 3, 2);

        List<Group> expected = List.of(
            new Group(336, new Coordinate(9, 9)),
            new Group(6, new Coordinate(9, 8))
        );

        List<Group> actual = finder.findConnectedGroups(toImage(binary));

        assertEquals(expected, actual);
        assertDescending(actual);
    }

    private BufferedImage toImage(int[][] binary) {
        BufferedImage image = new BufferedImage(binary[0].length, binary.length, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < binary.length; y++) {
            for (int x = 0; x < binary[y].length; x++) {
                image.setRGB(x, y, binary[y][x] == 1 ? 0xFFFFFF : 0x000000);
            }
        }
        return image;
    }

    private void fillRectangle(int[][] binary, int x, int y, int width, int height) {
        for (int row = y; row < y + height; row++) {
            for (int column = x; column < x + width; column++) {
                binary[row][column] = 1;
            }
        }
    }

    private void assertDescending(List<Group> groups) {
        List<Group> sorted = new ArrayList<>(groups);
        sorted.sort(Collections.reverseOrder());
        assertEquals(sorted, groups);
    }
}
