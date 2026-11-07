import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

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
    * (x:0, y:0). Y increases downward and X increases to the right. For example,
    * (row:4, column:7) corresponds to (x:7, y:4).
    *
    * The method returns a list of sorted groups. The group's size is the number 
    * of pixels in the group. The centroid of the group
    * is computed as the average of each of the pixel locations across each dimension.
    * For example, the x coordinate of the centroid is the sum of all the x
    * coordinates of the pixels in the group divided by the number of pixels in that group.
    * Similarly, the y coordinate of the centroid is the sum of all the y
    * coordinates of the pixels in the group divided by the number of pixels in that group.
    * The division should be done as INTEGER DIVISION.
    *
    * The groups are sorted in DESCENDING order according to Group's compareTo method.
    * 
    * @param image a rectangular 2D array containing only 1s and 0s
    * @return the found groups of connected pixels in descending order
    */
    @Override
    public List<Group> findConnectedGroups(int[][] image) {

          validate(image);
 
        int rows = image.length;
        int cols = image[0].length;
        boolean[][] visited = new boolean[rows][cols];
        List<Group> groups = new ArrayList<>();
 
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (image[row][col] == 1 && !visited[row][col]) {
                    groups.add(pixelGroup(image, visited, row, col));
                }
            }
        }
 
        groups.sort(Collections.reverseOrder());
        return groups;
    }

        private Group pixelGroup(int[][] image, boolean[][] visited, int startRow, int startCol) {
        // up down left right
        int[] dRow = {-1, 1, 0, 0};
        int[] dCol = {0, 0, -1, 1};
 
        long sumX = 0;
        long sumY = 0;
        int size = 0;
 
        Deque<int[]> stack = new ArrayDeque<>();
        visited[startRow][startCol] = true;
        stack.push(new int[] {startRow, startCol});
 
        while (!stack.isEmpty()) {
            int[] cell = stack.pop();
            int row = cell[0];
            int col = cell[1];
 
            size++;
            sumX += col; // x is the column
            sumY += row; // y is the row
            
             for (int i = 0; i < dRow.length; i++) {
                int nextRow = row + dRow[i];
                int nextCol = col + dCol[i];
                if (nextRow >= 0 && nextRow < image.length && nextCol >= 0 && nextCol < image[0].length
                && image[nextRow][nextCol] == 1 && !visited[nextRow][nextCol]) {
                    visited[nextRow][nextCol] = true;
                    stack.push(new int[] {nextRow, nextCol});
                }
            }
        }
 
        return new Group(size, new Coordinate((int) (sumX / size), (int) (sumY / size)));
    }


    // private method validate
    // throw image cant be null, row cant be null, image cant be empty
    //
    private void validate(int[][] image) {
        if (image == null) {
            throw new NullPointerException("image must not be null");
        }
        for (int[] row : image) {
            if (row == null) {
                throw new NullPointerException("image rows must not be null");
            }
        }
        if (image.length == 0 || image[0].length == 0) {
            throw new IllegalArgumentException("image must not be empty");
        }
        int width = image[0].length;
        for (int[] row : image) {
            if (row.length != width) {
                throw new IllegalArgumentException("image must be rectangular");
            }
            for (int pixel : row) {
                if (pixel != 0 && pixel != 1) {
                    throw new IllegalArgumentException("image must contain only 0s and 1s");
                }
            }
        }
    }
}
        
