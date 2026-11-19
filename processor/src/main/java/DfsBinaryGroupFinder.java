
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ArrayDeque;
import java.util.Deque;

public class DfsBinaryGroupFinder implements BinaryGroupFinder {
    /**
     * Finds connected pixel groups of 1s in an integer array representing a binary
     * image.
     * 
     * The input is a non-empty rectangular 2D array containing only 1s and 0s.
     * If the array or any of its subarrays are null, a NullPointerException
     * is thrown. If the array is otherwise invalid, an IllegalArgumentException
     * is thrown.
     *
     * Pixels are considered connected vertically and horizontally, NOT diagonally.
     * The top-left cell of the array (row:0, column:0) is considered to be
     * coordinate
     * (x:0, y:0). Y increases downward and X increases to the right. For example,
     * (row:4, column:7) corresponds to (x:7, y:4).
     *
     * The method returns a list of sorted groups. The group's size is the number
     * of pixels in the group. The centroid of the group
     * is computed as the average of each of the pixel locations across each
     * dimension.
     * For example, the x coordinate of the centroid is the sum of all the x
     * coordinates of the pixels in the group divided by the number of pixels in
     * that group.
     * Similarly, the y coordinate of the centroid is the sum of all the y
     * coordinates of the pixels in the group divided by the number of pixels in
     * that group.
     * The division should be done as INTEGER DIVISION.
     *
     * The groups are sorted in DESCENDING order according to Group's compareTo
     * method.
     * 
     * @param image a rectangular 2D array containing only 1s and 0s
     * @return the found groups of connected pixels in descending order
     */
    @Override
    public List<Group> findConnectedGroups(int[][] image) {
        if (image == null) {
            throw new NullPointerException("Image cannot be null");
        }

        if (image.length == 0) {
            throw new IllegalArgumentException("Image cannot be empty");
        }
        // Check each row
        for (int[] row : image) {
            if (row == null) {
                throw new NullPointerException("Image row cannot be null");
            }

            if (row.length == 0) {
                throw new IllegalArgumentException("Image row cannot be empty");
            }
        }
        List<Group> groups = new ArrayList<>();
        boolean[][] visited = new boolean[image.length][image[0].length];
        for (int r = 0; r < image.length; r++) {
            for (int c = 0; c < image[0].length; c++) {
                if (!visited[r][c] && image[r][c] == 1) {
                    int[] totals = new int[3];

                    dfs(image, r, c, visited, totals);

                    int x = totals[1] / totals[0];
                    int y = totals[2] / totals[0];

                    Group group = new Group(totals[0], new Coordinate(x, y));
                    groups.add(group);
                }
            }
        }
        groups.sort(Collections.reverseOrder());
        return groups;
    }

    private void dfs(
            int[][] image, int r, int c,
            boolean[][] visited, int[] totals) {

        Deque<Coordinate> stack = new ArrayDeque<>();

        stack.push(new Coordinate(c, r));
        visited[r][c] = true;

        int[][] directions = {
                { 0, 1 }, // DOWN
                { 0, -1 }, // UP
                { 1, 0 }, // RIGHT
                { -1, 0 } // LEFT
        };

        while (!stack.isEmpty()) {
            Coordinate current = stack.pop();

            int x = current.x();
            int y = current.y();

            totals[0]++;
            totals[1] += x;
            totals[2] += y;

            for (int[] direction : directions) {
                int nextX = x + direction[0];
                int nextY = y + direction[1];

                if (nextY >= 0 && nextY < image.length
                        && nextX >= 0 && nextX < image[0].length
                        && !visited[nextY][nextX]
                        && image[nextY][nextX] == 1) {

                    visited[nextY][nextX] = true;
                    stack.push(new Coordinate(nextX, nextY));
                }
            }
        }
    }
}
