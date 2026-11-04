## Matthew

Read through ImageSummaryApp in detail with your partner. Understand what each part does. This will involve looking through and reading ALL of the other classes records and interfaces. This will take a long time, but it is worth it! Do not skimp on this part, you will regret it! Also look at the sampleInput and sampleOutput folders to understand what comes in and what goes out.

As you read through the files, take notes in notes.md to help you and your partner understand. Make frequent commits to your notes.

The ImageSummaryApp.java serves to summarize the functionality of our app into understandable pieces, to translate this into plain english I will detail my findings on interlocking functionality below

Conclusion: The ImageSummaryApp file utilizes three parameters to customize its targeting, saving its findings on the image as a file named groups.csv that contains coordinate values of outlined objects of a specific color.
``<input_image>`` ``<hex_target_color>`` ``<threshold>``

1: File checks for proper argument structure. (Threshold, Image, Color)
2: Two custom actors, the ColorDistanceFinder and DistanceImageBinarizer work to 

## Anthony
It appears we are searching for three items specifically, `<input_image>` `<hex_target_color>` `<threshold>`
We want to take a .png file and binarize it into something requiring a Depth-First-Search.
We need to make sure it loads the input image using `ImageIO` and parses the hex target color into a 24 bit integer (`0xRRGGBB`).
* Initializes the `DistanceImageBinarizer` with `EuclideanColorDistance`.
* Generates and saves the binarized image to disk as `binarized.png`.
* Uses `BinarizingImageGroupFinder` paired with `DfsBinaryGroupFinder` to extract connected pixel groups.
* Iterates through the sorted list of groups and writes the summary metrics (`size,x,y`) to `groups.csv` using `group.toCsvRow()`.


**`EuclideanColorDistance`**: Extracts red, green, and blue channels via bitwise operations (`>> 16`, `>> 8`, `& 0xFF`) and computes the 3D Euclidean distance between two color values.

**`DistanceImageBinarizer`**: Converts a `BufferedImage` into a 2D integer array (1 for white, 0 for black) by checking if a pixel's Euclidean distance to the `targetColor` is strictly less than the `threshold`. Also handles converting binary arrays back to standard RGB `BufferedImage` instances.


**`Coordinate`: A record representing an `(x, y)` coordinate where the top left is `(0, 0)`, `x` increases to the right, and `y` increases downward.

`Group`: Represents a connected component with a size and a centroid `Coordinate`. Implements `Comparable<Group>` to order groups in **descending** order: first by **size**, then by **centroid x**, and finally by **centroid y**. Includes `toCsvRow()` for CSV formatting.

`BinaryGroupFinder`: Defines the contract for locating contiguous blocks of `1`s in a 2D integer array using horizontal and vertical connectivity.

`DfsBinaryGroupFinder`: Implements DFS traversal to group adjacent white pixels, computing integer division centroids for each group and sorting them.

`BinarizingImageGroupFinder`: Implements `ImageGroupFinder` by bridging the image binarizer and binary group finder, allowing raw `BufferedImage` inputs to be processed directly into sorted `Group` lists.

## Yousif

BinarizingImageGroupFinder implements ImageGroupFinder and coordinates ImageBinarizer and BinaryGroupFinder. Its purpose is to transform an input image into binary data, pass that data to the grouping component, and then give the resulting groups. This avoids duplicating the DFS or color-dinstance logic. It doesn't perform DFS or calculate color dinstances itself. it delegates these operations to other methods/components.

EuclideanColorDistance: Measures the color similarity

DistanceImageBinarizer: Converts image into an Integer 2d array (int[][]) of 0s and 1s

DfsBinaryGrouperFinder: find connected groups of 1s

Group + Coordinate files: Stores group sizes and centroids

groups.csv: final results
