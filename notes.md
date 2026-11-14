# Interfaces & Implementations
## ColorDistanceFinder
- Interface for a method that returns the distance between two colors.
- Each color is represented as 24-bit integers (0xRRGGBB).
- Requires bitwise operations to convert colors.
### Used By:
#### EuclideanColorDistance 
- The formula: sqrt((r1 - r2)^2 + (g1 - g2)^2 + (b1 - b2)^2)

## ImageBinarizer
- Interface for a method that returns a 2D array of ints containing 1s and 0s from a BufferedImage.
### Used By: 
#### DistanceImageBinarizer 
- Uses color distance to determine whether the pixel is a 1 or 0.

## BinaryGroupFinder
- Interface for a method that returns a list of Groups from an image.
- The image is represented as a 2D array of ints containing 1s and 0s.
- Groups are of 1s.
### Used by: 
#### DfsBinaryGroupFinder 
- Implementation using a Depth First Search
##### findConnectedGroups:
- This method is similar to the forest-fire implementation we did except using dfs instead of bfs.
- Returns a list of groups, a group is a record of the size of the cluster and the center coordinate of the cluster
- for this method we will need a visited set.
- we will need to loop over the whole graph and do a dfs on ever coordinate with 1 if its not in the visited set.
- Once we do dfs, we will need to create the Group record by finding the center coordinate of the cluster and find the size of the cluster, then add that to our result list. 

# Records

### Coordinate
- Stores two ints representing an x, y coordinate.

### Group
- Stores a Coordinate "centroid".
- Stores a size in pixels of the group
- Comparable.

# ImageSummaryApp
- The primary class using the implementations.
## main
- Takes 3 arguments:
    * Image path
    * Target color
    * Threshold
- The image is then binarized used the DistanceImageBinarizer using a EuclideanColorDistance.
- This image becomes a binaryImage which is passed to the BinarizingImageGroupFinder to return the pixel groups in the image.
- These groups are then printed to a the 'groups.csv' file.