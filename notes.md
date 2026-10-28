KJ's AGREEMENT

# KJ's Notes

## Group
int x, int y

## ImageSummaryApp
- Takes in a target image, hex color, and a threshold (for what?)
- Parses over arguments in input command
- Binarize, buffer, write out image.

## ColorDistanceFinder
"Defines an interface for computing the distance between two colors."
 - Uses hex codes
 - How would we calculate the distance between colors? (this calculation is made in ImageSummaryApp using EuclidianColorDistance)

 ## ImageBinarizer
 "An interface for converting between RBG.. and binary (B&W)"
- Binary image represented as int[][].
- 1 = white, 0 = black
- Contains two methods:

**toBinaryArray(BufferedImage image)**
BufferedImage -> int[][]

**toBufferedImage(int[][] image)**
int[][] -> BufferedImage

## EuclideanColorDistance implements ColorDistanceFinder
Does what it says on the lid.

`sqrt((r1 - r2)^2 + (g1 - g2)^2 + (b1 - b2)^2)`

death, taxes, and calculus (pain.png)

## ImageGroupFinder
"Find connected groups in an image."
whatever that means

**findConnectedGroups(BufferedImage image)**
BufferedImage -> `List<Group>`

## BinaryGroupFinder
"Finds connected pixel groups of 1s in an integer Array representing a binary image.
IT'S THE ISLAND PROBLEM

- Connected adjacently only, not diagonally.
- `centroid = (x.avg, y.avg) // int division`
- Sorted in descending order.
- Throws NullPointerException if image = null or `image[0]` = null
- Throws IllegalArgumentException invalid otherwise.
- Sort using Group's compareTo method. 

**findConnectedGroups(int[][] image)**
int[][] -> `List<Group>`

## DfsBinaryGroupFinder implements BinaryGroupFinder
if this is DFS then the implemented class must be BFS

DENNYS AGREEMENT

## ImageSummaryApp
Command-line program that takes an image, a target color, and a threshold. It marks the pixels close to that color as white and everything else as black, saves that black-and-white image, then finds the connected white blobs and writes a summary of them to a CSV file.

## ImageGroupfinder
- Input: a BufferedImage
- Output: a List<Group> of the connected regions found in that image

## Imagebianizer

- toBinaryArray(BufferedImage image) takes a color image and returns an int[][] where each pixel is 1 (white) or 0 (black). It doesn’t decide how to pick white vs black; the implementing class does. In your app,  DistanceImageBinarizer makes a pixel white if its color is within the threshold distance of the target color.

- toBufferedImage(int[][] image) goes the other way. It turns that grid of 1s and 0s back into an actual image, using 0xFFFFFF for white and 0x000000 for black.

## Group

- Represents a group of contiguous pixels in an image. [x, y] coordinated instead of [row, column]
- Returns a negative integer, zero, or a positive integer if this group is less than, equal to, or greater than the specified group
- Groups size tell the number of pixels in gruop.