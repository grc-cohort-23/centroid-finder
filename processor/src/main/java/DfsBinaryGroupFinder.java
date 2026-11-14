import java.util.List;
import java.util.ArrayList;

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
        if (image == null) {
            throw new NullPointerException();
        }
        if (image.length == 0) {
            throw new IllegalArgumentException();
        }
        if (image[0] == null) {
            throw new NullPointerException();
        }
        if (image[0].length == 0) {
            throw new IllegalArgumentException();
        }

        int rows = image.length;
        int cols = image[0].length;

        for (int[] row : image) {
            if (row == null) {
                throw new NullPointerException();
            }

            if (row.length != cols) {
                throw new IllegalArgumentException();
            }

            for (int value : row) {
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException();
                }
            }
        }

        List<Group> groups = new ArrayList<>();
        boolean[][] visited = new boolean[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                if (image[r][c] == 1 && !visited[r][c]) {
                    int[] data = new int[3];
                    dfs(image, visited, r, c, data);

                    int size = data[0];
                    int x = data[1] / size;
                    int y = data[2] / size;

                    groups.add(new Group(size, new Coordinate(x, y)));
                }
            }
        }

        groups.sort((a, b) -> b.compareTo(a));

        return groups;
    }

    private void dfs(int[][] image, boolean[][] visited,
                 int r, int c, int[] data) {

        if (r < 0 || r >= image.length ||
            c < 0 || c >= image[0].length ||
            visited[r][c] || image[r][c] == 0) {
            return;
        }

        visited[r][c] = true;

        data[0]++;
        data[1] += c;
        data[2] += r;

        dfs(image, visited, r - 1, c, data);
        dfs(image, visited, r + 1, c, data);
        dfs(image, visited, r, c - 1, data);
        dfs(image, visited, r, c + 1, data);
    }
    
}
