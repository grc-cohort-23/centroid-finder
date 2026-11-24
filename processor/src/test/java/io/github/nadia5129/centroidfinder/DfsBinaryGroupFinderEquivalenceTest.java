package io.github.nadia5129.centroidfinder;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

// The faster DfsBinaryGroupFinder (int[] stack) must find exactly the same groups,
// centroids and order as the original version (Coordinate objects on an ArrayDeque).
public class DfsBinaryGroupFinderEquivalenceTest {

    private final BinaryGroupFinder original = new OriginalDfsBinaryGroupFinder();
    private final BinaryGroupFinder fast = new DfsBinaryGroupFinder();

    // Many random images of many sizes and densities give identical results
    @Test
    void findConnectedGroups_randomImages_sameAsOriginal() {
        // Arrange
        Random random = new Random(12345);
        for (int round = 0; round < 2000; round++) {
            int height = 1 + random.nextInt(30);
            int width = 1 + random.nextInt(30);
            double density = random.nextDouble();
            int[][] image = new int[height][width];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    image[y][x] = random.nextDouble() < density ? 1 : 0;
                }
            }

            // Act
            List<Group> expected = original.findConnectedGroups(image);
            List<Group> actual = fast.findConnectedGroups(image);

            // Assert
            assertEquals(expected, actual, "round " + round);
        }
    }

    // Edge cases: one pixel, all white, all black, a single row, a single column
    @Test
    void findConnectedGroups_edgeCases_sameAsOriginal() {
        // Arrange
        int[][][] images = {
            { { 1 } },
            { { 0 } },
            { { 1, 1, 1 }, { 1, 1, 1 } },
            { { 0, 0, 0 }, { 0, 0, 0 } },
            { { 1, 0, 1, 1, 0, 1 } },
            { { 1 }, { 1 }, { 0 }, { 1 } },
        };

        for (int[][] image : images) {
            // Act + Assert
            assertEquals(original.findConnectedGroups(image), fast.findConnectedGroups(image));
        }
    }

    // A pixel at the end of one row must not join the first pixel of the next row
    @Test
    void findConnectedGroups_whiteAtRowEndAndNextRowStart_twoGroups() {
        // Arrange: (2,0) and (0,1) are next to each other in index order, but not touching
        int[][] image = {
            { 0, 0, 1 },
            { 1, 0, 0 },
        };

        // Act
        List<Group> groups = fast.findConnectedGroups(image);

        // Assert
        assertEquals(2, groups.size());
        assertEquals(original.findConnectedGroups(image), groups);
    }

    // A big all-white frame (like a real video frame) works and matches
    @Test
    void findConnectedGroups_fullFrameAllWhite_sameAsOriginal() {
        // Arrange
        int[][] image = new int[720][816];
        for (int[] row : image) {
            java.util.Arrays.fill(row, 1);
        }

        // Act + Assert
        assertEquals(original.findConnectedGroups(image), fast.findConnectedGroups(image));
    }
}
