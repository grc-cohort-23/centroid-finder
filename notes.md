This application takes three command-line arguments therefore we know what type of input to expect...

read command-line inputs → load an image → turn it into black and white → find white-pixel groups → save the results.

CSV = Comma-Separated Values
A CSV file is a plain text file that stores data in rows and columns.

For args.length < 3 It checks if the user entered fewer than three arguments. If they did, the program prints a message showing how to run the application and stops running.

The imports:
import java.awt.image.BufferedImage; -
 holds a picture in memory so the program can look at each pixels colors.

import java.io.File; -
points to a file on your computer like image.png

import java.io.PrintWriter; -
writes text into a file. Here it creates groups.csv

import java.util.List; -
A container that holds many items in order. Here it holds all the groups the program finds. 

import javax.imageio.ImageIO; -
opens an image file and saves an image file. It loads your input picture and saves binarized.png

TODO (nadia) line 57 -69

This part is where the program reads the input values from the command line and stores them in variables so the rest of the code can use them. The code:

String inputImagePath = args[0];
String hexTargetColor = args[1];
int threshold = 0;

This gives names to the first two arguments and declares a variable for the threshold. The image path selects the file the user wants to analyze, and the target color tells the program what color to look for. The threshold is the value used to decide how close a pixel needs to be to that target color before it is counted as part of the selection.

The code then tries to convert the third argument from text to an integer:

threshold = Integer.parseInt(args[2]);

The threshold arrives as text, so Integer.parseInt converts it to an integer. The try/catch handles input that is not a valid integer. On failure, the program prints an error and returns. The try block means "attempt this operation." If the conversion works, threshold gets the converted value and the program continues. If it does not work, it throws a NumberFormatException, and the catch block handles that problem by printing a clear message and stopping the program before it continues with invalid data.

(jessica) line 71 -81

This section is where the program actually turns the image into a black-and-white version. First, it calls:

int[][] binaryArray = binarizer.toBinaryArray(inputImage);

This creates a two-dimensional array where each pixel is converted to either 1 or 0 depending on whether it is close enough to the target color. Then it does:

BufferedImage binaryImage = binarizer.toBufferedImage(binaryArray);

This turns the binary array back into an image so the program can save it as a real PNG file. The try/catch around ImageIO.write checks whether the file was saved correctly. If something goes wrong, it prints an error message instead of crashing the whole program.

(paris) line 83-100

These lines are the part where the program turns the processed image into meaningful group data. First, it creates a group-finder object:

ImageGroupFinder groupFinder = new BinarizingImageGroupFinder(binarizer, new DfsBinaryGroupFinder());
List<Group> groups = groupFinder.findConnectedGroups(inputImage);

This tells the program to use the same binarization rules to decide which pixels are white, and then find connected groups of those white pixels using depth-first search. A connected group is a cluster of touching white pixels. The result is stored in a List<Group>, so each group can be analyzed separately.

After that, the program writes those groups into a CSV file:

try (PrintWriter writer = new PrintWriter("groups.csv")) {
    for (Group group : groups) {
        writer.println(group.toCsvRow());
    }
}

This opens groups.csv and writes one line per group. Each line is a summary of that group, such as its size and centroid coordinates. This is important because the program is not only producing a binarized image; it is also converting the image data into a spreadsheet-friendly file that can be used for analysis or comparison.


CLASS RESPONSIBILITIES:

ImageSummaryApp.java - This is the main driver of the whole program. It reads the arguments, loads the image, applies the target color logic, saves the binarized image, finds connected white groups, and writes the results to a CSV file.

DistanceImageBinarizer.java - This class is responsible for turning the full-color image into a black-and-white image by comparing each pixel's color to the target color. It decides whether a pixel should be white or black based on the threshold.

BinarizingImageGroupFinder.java - This class connects the binarization step to the grouping step. It takes the image, turns it into a binary array, and then asks a group-finding algorithm to find connected white regions.

EuclideanColorDistance.java - This class calculates the distance between two RGB colors by using the Euclidean distance formula in 3D color space. It is the numerical way the program decides how similar a pixel is to the target color.

DfsBinaryGroupFinder.java - This is the actual connected-component search algorithm that finds groups of white pixels.

Group.java - This record stores a group's size and centroid. It also formats a group as a CSV row like "size,x,y".

Coordinate.java - This record represents a location in the image using x and y values. The origin is the top-left pixel.

FLOW OF DATA: the heart of the project

1. The program reads the input image path, target color, and threshold from the command line.
2. It loads the image into a BufferedImage object so each pixel can be examined.
3. It compares every pixel's color to the target color using Euclidean distance.
4. If the distance is less than the threshold, the pixel is treated as white (1). Otherwise, it is black (0).
5. The binary array is turned back into an image and saved as binarized.png.
6. The white pixels are scanned to find connected groups using DFS. Pixels are connected only up, down, left, and right.
7. Each group is summarized by size and centroid.
8. The group information is written to groups.csv in CSV format.

Explain the interfaces

ImageBinarizer.java - This interface defines the contract for converting between a color image and a binary image. Any class that implements it must know how to turn an image into a 2D array of 0s and 1s and then turn that array back into an image.

ImageGroupFinder.java - This interface defines the method for detecting connected groups in an image. Its purpose is to abstract away the exact group-finding algorithm.

BinaryGroupFinder.java - This interface defines how to find connected groups in a binary 2D array. It is focused on the image-processing logic rather than reading files or saving results.

ColorDistanceFinder.java - This interface defines a way to compare two colors numerically. It lets the app swap in different distance formulas without changing the rest of the code.

Meaning of the output files

The sample input image contains a picture with visible shapes or blobs that should match a target color. The program uses the threshold to decide which pixels are close enough to that target color to count as the object of interest.

The PNG output, binarized.png, is a black-and-white version of the image. White pixels represent the pixels that matched the target color closely enough, and black pixels represent everything else.

The CSV output, groups.csv, contains one line per connected group. Each row has the format "size,x,y". The size tells how many pixels are in that group, and x and y are the centroid coordinates for the group center.

This is useful because the image itself is visual, but the CSV file turns the result into structured data that can be read by spreadsheets or saved for analysis.

Why this design?

The program uses a target color and threshold instead of checking for one exact color value because real images may have small variations in lighting, shading, and noise. A threshold allows the program to tolerate slight differences in RGB values while still identifying the same object.

It converts the image to binary first because once the image is reduced to 0s and 1s, connected groups are much easier to detect. This simplifies the problem from analyzing colors to analyzing shapes.

It finds connected groups instead of just counting pixels because the image may contain multiple separate objects. Grouping connected white pixels lets the program identify each object as a separate region.

It saves the results as CSV because the program is not only making an image; it is also summarizing the objects in a format that is compact, readable, and easy to analyze outside the program.

Short Glossary

Binary image - an image represented by only two values, usually 0 and 1, instead of many color values.
Threshold - the cutoff value used to decide whether a pixel is close enough to a target color to be considered white.
Connected components - groups of pixels that touch each other up, down, left, or right.
Centroid - the center point of a group, calculated by averaging all pixel coordinates in the group.
RGB - the red, green, and blue values that make up a color.
Euclidean distance - the straight-line distance between two points in 3D color space.
CSV - a file format where rows are separated by newlines and values are separated by commas.