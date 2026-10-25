import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;
import java.util.HashSet;

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
        //Null references and 0 lengths addressed, code can proceed assuming valid values.
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
        // Null and validity check block complete.

        boolean[][] visited = new boolean[height][width];
        List<Group> groups = new LinkedList<>();

        // 4-way connectivity: up, down, left, right
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                if (image[r][c] == 1 && !visited[r][c]) {
                    // Start DFS for a new connected group
                    List<Coordinate> pixels = new LinkedList<>();
                    Stack<int[]> stack = new Stack<>();

                    stack.push(new int[]{r, c});
                    visited[r][c] = true;

                    while (!stack.isEmpty()) {
                        int[] curr = stack.pop();
                        int cr = curr[0];
                        int cc = curr[1];

                        // Note: x is column (cc), y is row (cr)
                        pixels.add(new Coordinate(cc, cr));

                        for (int[] dir : directions) {
                            int nr = cr + dir[0];
                            int nc = cc + dir[1];

                            if (nr >= 0 && nr < height && nc >= 0 && nc < width) {
                                if (image[nr][nc] == 1 && !visited[nr][nc]) {
                                    visited[nr][nc] = true;
                                    stack.push(new int[]{nr, nc});
                                }
                            }
                        }
                    }

                    // Compute size and centroid (using integer division)
                    int size = pixels.size();
                    long sumX = 0;
                    long sumY = 0;
                    for (Coordinate p : pixels) {
                        sumX += p.x();
                        sumY += p.y();
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

    private int[] computeAverageOfList(List<Group> groups) {
        Set<Integer> visitedCols = new HashSet<>();
        Set<Integer> visitedRows = new HashSet<>();
        for(Group group : groups){
            int x = group.centroid().x();
            int y = group.centroid().y();
            if(!visitedCols.contains(x)) visitedCols.add(x);
            if(!visitedRows.contains(x)) visitedRows.add(y);
        }
        int[] average = new int[2];
        int total = 0;
        int count = 0;
        for(int n : visitedCols){ total += n; count++; }
        average[0] = total / count; total = 0; count = 0;
        //Find average of rows
        for(int n : visitedRows){ total += n; count++; }
        average[1] = total / count; total = 0; count = 0;
        //Return array containing averages
        return average;
    }
    


}
