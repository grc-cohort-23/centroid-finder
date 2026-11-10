package io.github.nadia5129.centroidfinder;

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
        if (image == null) throw new NullPointerException("image is null!");
        //null row
        for (int row = 0; row < image.length; row++){
            if (image[row] == null){
                throw new NullPointerException("null in row");
            }
        }
        //empty array
        if (image.length == 0) throw new IllegalArgumentException("array is empty");
        //different length rows
        for (int rowNum = 0; rowNum < image.length; rowNum++){
            if (image[rowNum].length != image[0].length){
                throw new IllegalArgumentException("all row lengths must be the same");
            }
        }
        //input must be 1 or 0s else throw exception
        for(int row = 0; row < image.length; row++){
            for(int col = 0; col < image[row].length; col++){
                if(image[row][col] != 0 && image[row][col] != 1){
                    throw new IllegalArgumentException("values can only be 1 and 0");
                }
            }
        }

        boolean[][] visited = new boolean[image.length][image[0].length];
        Deque<Coordinate> workDeque = new ArrayDeque<>();

        List<Group> groups = new ArrayList<>();
        
        //Go through every pixel
        for (int row = 0; row < image.length; row++){
            for (int col = 0; col < image[row].length; col++){
                if (image[row][col] == 1 && visited[row][col] == false){
                    visited[row][col] = true;
                    workDeque.push(new Coordinate(col, row));
                    int count = 0;
                    int sumX = 0;
                    int sumY = 0;
                    while(!workDeque.isEmpty()){                            Coordinate currentPixel = workDeque.pop();
                          count++;
                        sumX += currentPixel.x();
                        sumY += currentPixel.y();
                        //right neighbor check
                       int nx = currentPixel.x() + 1;
                        int ny = currentPixel.y();
                        if (ny >= 0 && ny < image.length && nx >= 0 && nx < image[0].length && image[ny][nx] == 1 && visited[ny][nx] == false ){
                        visited[ny][nx] = true;
                            workDeque.push(new Coordinate(nx, ny));
                        }
                        //left neighbor check
                        nx = currentPixel.x() - 1;
                        ny = currentPixel.y();
                        if (ny >= 0 && ny < image.length && nx >= 0 && nx < image[0].length && image[ny][nx] == 1 && visited[ny][nx] == false ){
                            visited[ny][nx] = true;
                            workDeque.push(new Coordinate(nx, ny));
                        }
                        //down neighbor check
                        nx = currentPixel.x();
                        ny = currentPixel.y() + 1;
                        if (ny >= 0 && ny < image.length && nx >= 0 && nx < image[0].length && image[ny][nx] == 1 && visited[ny][nx] == false ){
                                visited[ny][nx] = true;
                                workDeque.push(new Coordinate(nx, ny));
                            }
                            //above neighbor check
                            nx = currentPixel.x();
                            ny = currentPixel.y() - 1;
                            if (ny >= 0 && ny < image.length && nx >= 0 && nx < image[0].length && image[ny][nx] == 1 && visited[ny][nx] == false ){
                                visited[ny][nx] = true;
                                workDeque.push(new Coordinate(nx, ny));
                            }
                        }
                        groups.add(new Group(count, new Coordinate(sumX / count, sumY / count)));
                    }
                }
            }
            Collections.sort(groups, Collections.reverseOrder());
        return groups;
    }
    
}
