import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

        // List<Group> answer = new ArrayList<>();

        //make a graph of the image to traverse over to get the groups
        // groups should all be unconnected islands so traversing the keys
        // and maintaining a visited set should identify valid islands of 
        // image pixals to make into groups 
        Map<Point, ArrayList<Point>> graph = new HashMap<>();
        
        for(int r = 0 ;  r< image.length ; r++){
            for(int c = 0 ; c < image[0].length ; c++){

                if(image[r][c] == 0){
                    continue;
                }

                Point curPoint = new Point(r, c);
                
                ArrayList<Point> neighbors = getNeighbors(graph, image, curPoint);
                graph.putIfAbsent(curPoint, neighbors);
                
                // System.out.println(curPoint+ "  |  Neighbors: "+  neighbors); // for debugging ONLY
            }
        }

        List<Group> answer = imageDFS(graph);
        // System.out.println(" GROUP: "+ answer); // for debugging ONLY
        return answer;
    }


    /*
        Record Definition of a row & column pair used for BFS
    */
    public record Point(int row, int col){};
    
    /*
        Stack based BFS over a graph to identify pixel islands within the image matrix 

        loops over all keys of the graph and if it has not already been visited begin a 
        bfs and return the island as a Group

        Returs a List of groups made from the original image matrix
    */
    public List<Group> imageDFS(Map<Point, ArrayList<Point>> graph){
        List<Group> connectedGroupList = new ArrayList<>();
        Stack<Point> stack = new Stack<>();
        HashSet<Point> visited = new HashSet<>(); 
        
        for(Point key : graph.keySet()){

            if(visited.contains(key)){continue;}

            //Initial set up for dfs of a Key/Point
            stack.push(key);
            visited.add(key);
            // ArrayList<Group> curGroupList = new ArrayList<>();

            int groupX = key.col();
            int groupY = key.row();
            int groupSize = 1; // default to 1 so no potential division by zero

            //Stack Based DFS
            while(!stack.isEmpty()) { 
                Point cur = stack.pop();
                      
                for(Point neighbor : graph.get(cur)){
                    if(visited.add(neighbor)){

                        stack.push(neighbor);
                        groupSize++;
                        groupY += neighbor.row();
                        groupX += neighbor.col();
                    }
                }
            }

            Group curGroup = new Group(groupSize, new Coordinate(groupX/groupSize, groupY/groupSize));

            connectedGroupList.add(curGroup);
            //System.out.println("CUR GROUP: "+ curGroup); // for debugging ONLY
        }

        Collections.sort(connectedGroupList);
        return connectedGroupList;
    }


    /* 
        gets the neighboring points of the provided Point 
    */
    public ArrayList<Point> getNeighbors(Map<Point, ArrayList<Point>>  graph, int[][] image, Point cur){
        int rLen = image.length;
        int cLen = image[0].length;

        // define directions for DFS/BFS
        int[][] dirs = new int[][]{
            {-1,0}, //up
            {1,0},  //down
            {0,-1}, //left
            {0,1},  //right
        };
        
        ArrayList<Point> neighbors = new ArrayList<>();

        for(int[] dir: dirs){

            //verify a valid neigbor to add to neighbor list
            if( cur.row() + dir[0] >= 0   &&
                cur.row() + dir[0] < rLen &&
                cur.col() + dir[1]>= 0    &&
                cur.col() + dir[1] < cLen &&
                image[cur.row() + dir[0]][cur.col() + dir[1]] == 1
            ){
                neighbors.add(new Point(cur.row() + dir[0], cur.col() + dir[1]));
            }
        }
        return neighbors;
    }

    public void main(String[] args) {
        int[][] testImgData = 
        {
                   // 0,1,2,3,4,5
            /*0*/    {1,1,1,0,1,1},
            /*1*/    {0,0,0,0,1,1},
            /*2*/    {1,1,0,0,0,0},
            /*3*/    {1,1,0,1,1,1},
            /*4*/    {1,1,0,1,1,1},
        };
        findConnectedGroups(testImgData);
    }
}
