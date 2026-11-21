package io.github.nadia5129.centroidfinder;

import java.util.ArrayList;
import java.util.Collections;
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

        int height = image.length;
        int width = image[0].length;

        //Every pixel gets one number: index = y * width + x. From an index we can get the
        //position back with x = index % width and y = index / width. Keeping plain ints on
        //the stack (instead of a new Coordinate object for every pixel) is much faster.
        boolean[] visited = new boolean[height * width];
        //A pixel is marked visited when it is pushed, so each pixel is pushed at most once
        //and the stack never needs more than height * width places.
        int[] stack = new int[height * width];

        List<Group> groups = new ArrayList<>();

        //Go through every pixel
        for (int row = 0; row < height; row++){
            for (int col = 0; col < width; col++){
                int start = row * width + col;
                if (image[row][col] == 1 && !visited[start]){
                    visited[start] = true;
                    int top = 0; //how many pixels are on the stack
                    stack[top++] = start;
                    int count = 0;
                    int sumX = 0;
                    int sumY = 0;
                    while (top > 0){
                        int index = stack[--top]; //pop
                        int x = index % width;
                        int y = index / width;
                        count++;
                        sumX += x;
                        sumY += y;
                        //right, left, down and up neighbors (the same order as before)
                        if (x + 1 < width) top = pushIfWhite(image, visited, stack, top, x + 1, y, width);
                        if (x - 1 >= 0) top = pushIfWhite(image, visited, stack, top, x - 1, y, width);
                        if (y + 1 < height) top = pushIfWhite(image, visited, stack, top, x, y + 1, width);
                        if (y - 1 >= 0) top = pushIfWhite(image, visited, stack, top, x, y - 1, width);
                    }
                    groups.add(new Group(count, new Coordinate(sumX / count, sumY / count)));
                }
            }
        }
        Collections.sort(groups, Collections.reverseOrder());
        return groups;
    }

    //Pushes pixel (x, y) onto the stack if it is white and not visited yet.
    //Returns the new number of pixels on the stack.
    private static int pushIfWhite(int[][] image, boolean[] visited, int[] stack, int top, int x, int y, int width) {
        int index = y * width + x;
        if (image[y][x] == 1 && !visited[index]) {
            visited[index] = true;
            stack[top] = index;
            return top + 1;
        }
        return top;
    }
}
