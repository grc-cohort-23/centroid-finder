import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

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
        List<Group> clusters = new ArrayList<>();
        Set<Coordinate> visited = new HashSet<>();
        if (image.length == 0) throw new NullPointerException("Image cannot be null");
        for(int r = 0; r < image.length; r++){
            if (image[r].length == 0) throw new NullPointerException("Image cannot contain null subarray");
            if (image[r].length != image[0].length) throw new IllegalArgumentException("Image must be rectangular");
            for(int c = 0; c < image[r].length; c++){
                Coordinate current = new Coordinate(c, r);
                if (!visited.contains(current)) {
                    if(image[current.y()][current.x()] == 1){
                        clusters.add(dfs(image, current, visited));
                    } else if (image[current.y()][current.x()] != 0) throw new IllegalArgumentException("Must contain only 1s and 0s");  
                }   
            }
        }

        clusters.sort((a,b) -> {
            return -(a.compareTo(b));
        });
        return clusters;
    }

    //dfs search. In here we will need to find the size of the cluster, and calculate the center of the cluster. Return the size and 
    //center as a group. 
    private Group dfs(int[][] image, Coordinate start, Set<Coordinate> visited){
        int totalX = 0;
        int totalY = 0;
        int size = 0;
        Stack<Coordinate> stack = new Stack<>();
        stack.push(start);
        while(!stack.isEmpty()){
            Coordinate current = stack.pop();
            if(visited.contains(current)) continue;
            visited.add(current);
            totalX += current.x();
            totalY += current.y();
            size++;
            for(Coordinate move : possibleMoves(image, current)) stack.push(move);

        }
        int avgX = totalX / size;
        int avgY = totalY / size;
        return new Group(size, new Coordinate(avgX, avgY));
    }

    private List<Coordinate> possibleMoves(int[][] image, Coordinate current) {
        List<Coordinate> list = new ArrayList<>();

        int[][] directions = {
            {-1,  0},
            { 1,  0},
            { 0, -1},
            { 0,  1}
        };

        for (var dir : directions) {
            int row = current.y() + dir[0];
            int col = current.x() + dir[1];
            if (row < 0 || row >= image.length || col < 0 || col >= image[row].length) continue;
            if (image[row][col] == 0) continue;
            list.add(new Coordinate(col, row));
        }

        return list;
    }
    
}
