package io.github.tonymythical.centroidFinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Finds connected pixel groups of 1s in an integer array representing a binary image.
 * Uses an optimized depth-first search with zero object allocations during traversal.
 */
public class DfsBinaryGroupFinder implements BinaryGroupFinder {
    /**
     * Finds connected pixel groups of 1s in an integer array representing a binary image.
     * 
     * The input is a non-empty rectangular 2D array containing only 1s and 0s.
     * If the array or any of its subarrays are null, a NullPointerException
     * is thrown. If the array is otherwise invalid, an IllegalArgumentException
     * is thrown.
     *
     * Pixels are considered connected vertically and horizontally, NOT diagonally.
     * The top-left cell of the array (row:0, column:0) is considered to be coordinate
     * (x:0, y:0). Y increases downward and X increases to the right.
     *
     * The groups are sorted in DESCENDING order according to Group's compareTo method.
     * 
     * @param image a rectangular 2D array containing only 1s and 0s
     * @return the found groups of connected pixels in descending order
     */
    @Override
    public List<Group> findConnectedGroups(int[][] image) {
        if (image == null || image.length == 0) {
            throw new NullPointerException("Image array cannot be null or empty");
        }

        int height = image.length;
        int width = image[0].length;

        for (int[] row : image) {
            if (row == null || row.length == 0 || row.length != width) {
                throw new NullPointerException("Image rows cannot be null, empty, or non-rectangular");
            }
            for (int val : row) {
                if (val != 0 && val != 1) {
                    throw new IllegalArgumentException("Image must contain only 1s and 0s");
                }
            }
        }

        boolean[][] visited = new boolean[height][width];
        List<Group> groups = new ArrayList<>();
        int[] stack = new int[height * width];

        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                if (image[r][c] == 1 && !visited[r][c]) {
                    int stackTop = 0;
                    stack[stackTop++] = r * width + c;
                    visited[r][c] = true;

                    int size = 0;
                    long sumX = 0;
                    long sumY = 0;

                    while (stackTop > 0) {
                        int curr = stack[--stackTop];
                        int cr = curr / width;
                        int cc = curr % width;

                        size++;
                        sumX += cc;
                        sumY += cr;

                        // Check 4-way neighbors: up, down, left, right
                        if (cr > 0 && image[cr - 1][cc] == 1 && !visited[cr - 1][cc]) {
                            visited[cr - 1][cc] = true;
                            stack[stackTop++] = (cr - 1) * width + cc;
                        }
                        if (cr + 1 < height && image[cr + 1][cc] == 1 && !visited[cr + 1][cc]) {
                            visited[cr + 1][cc] = true;
                            stack[stackTop++] = (cr + 1) * width + cc;
                        }
                        if (cc > 0 && image[cr][cc - 1] == 1 && !visited[cr][cc - 1]) {
                            visited[cr][cc - 1] = true;
                            stack[stackTop++] = cr * width + (cc - 1);
                        }
                        if (cc + 1 < width && image[cr][cc + 1] == 1 && !visited[cr][cc + 1]) {
                            visited[cr][cc + 1] = true;
                            stack[stackTop++] = cr * width + (cc + 1);
                        }
                    }

                    int centroidX = (int) (sumX / size);
                    int centroidY = (int) (sumY / size);
                    groups.add(new Group(size, new Coordinate(centroidX, centroidY)));
                }
            }
        }

        groups.sort(Collections.reverseOrder());
        return groups;
    }
}
