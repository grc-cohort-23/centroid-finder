import java.util.ArrayList;
import java.util.List;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Collections;

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
        //get image's boundaries
        isValidImage(image);
        int rows = image.length;
        int columns = image[0].length;
        //track visited positions with boolean array
        boolean [][] visited = new boolean[rows][columns];
        //create a new List<Group> groups to track all the groups
        List<Group> groups = new ArrayList<>();

        //do a nested for loop looping through the rows and columns, and call the dfs method
        for(int row = 0; row<rows; row++){
            for(int col = 0; col<columns; col++){
                if(image[row][col] == 1 && !visited[row][col]){
                    groups.add(findGroupDfs(image, visited, row, col));
                }
            }
        }
        
        //and return the array list groups in a  sorted order 

        //after validating the image we then need to find groups within the image

        //when finding groups we find the average to find the centroid x sum divided by x count and same for y 

        //then we need to sort the groups 

        Collections.sort(groups, Collections.reverseOrder());
        return groups;
    }
    // first helper method we need to validate image 
    // is valid if the 2d array is containing only 1's and 0's 
    // if null we throw nullPointerException error 
    // if not valid 2d array then we throw an Invalid argumaent exception error
   public static void isValidImage(int[][] image) {
    if (image == null) {
        throw new NullPointerException("Image is null");
    }
    // Check for null rows before accessing any row's length.
    for (int[] row : image) {
        if (row == null) throw new NullPointerException("Image rows are null");
    }
    if (image.length == 0) throw new IllegalArgumentException("Image cannot be empty");
    
    int columns = image[0].length;

    if (columns == 0) throw new IllegalArgumentException("Image rows cannot be empty");
    

    for (int[] row : image) {
        if (row.length != columns) throw new IllegalArgumentException("Image must be rectangular");

        for (int pixel : row) {
            if (pixel != 0 && pixel != 1) throw new IllegalArgumentException("Image must contain only 0s and 1s");
            }
        }
    }

    //private method for dfs search to return the total number of pixels in one group
    private static Group findGroupDfs(int[][] image, boolean[][] visited, int startRow, int startCol){
        int rows = image.length;
        int columns = image[0].length;

        int size = 0;
        int sumX = 0;
        int sumY = 0;

        int [][] directions = {
            {-1, 0}, //Up
            {1, 0}, //Down
            {0, -1}, //Left
            {0, 1} //Right
        };
        

        visited[startRow][startCol] = true;

        Deque<Coordinate> queue = new ArrayDeque<>();

        queue.addLast(new Coordinate(startCol, startRow));

        
        while(!queue.isEmpty()) {
            Coordinate current =  queue.removeFirst();
            int row = current.y();
            int column = current.x();

            size++;
            sumX += column;
            sumY += row;

            for (int[] dir : directions) {
                int nextRow = row + dir[0];
                int nextColumn = column + dir[1];
                
                if(nextRow >= 0 && nextRow < rows && nextColumn >= 0 && nextColumn < columns && image[nextRow][nextColumn] == 1 && !visited[nextRow][nextColumn]){
                    visited[nextRow][nextColumn] = true;
                    queue.addLast(new Coordinate(nextColumn, nextRow));
                }
            }
        }
    Coordinate centroid = new Coordinate(
        (int)(sumX/size) , (int)(sumY/size)
    );
    return new Group(size, centroid);
    }
}
