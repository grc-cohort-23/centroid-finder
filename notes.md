## DfsBinaryGroupFinder

- This class takes a binary rectangular array that represents an image. It is 0-indexed and returns a list of sorted groups. The size of the group is the number of pixels in that group. The centroid is the coordinates determining the center of that group. This should be determined using integer division!

- Tracks visited pixels so each pixel is counted only once.
- Checks that the array is rectangular and contains only 0s and 1s.

## EuclideanColorDistance

- This class takes two hexadecimal values and finds the Euclidean distance between those two values. The form is 0xRRGGBB. A larger number means the values are far from each other; a smaller number means they are more similar.

- Treats red, green, and blue as three separate dimensions.
- Comparing a color with itself gives a distance of 0.

## DistanceImageBinarizer

- This class uses color distance to determine if a pixel should be black or white in a binary image. Black is the default; white only appears in areas that are less than the threshold for that color.
- Removes the alpha channel before comparing colors.

## BinarizingImageGroupFinder

- This class converts an RGB image into a 2D array, then runs through that array to connect white pixel groups together.
- Delegates the work to those two objects.

## Coordinate

- A record that keeps the x and y values of a certain point in an image. We will almost certainly need to use this!

- The x value represents the column, and y represents the row.
- Record values cannot be changed after creation.

## Group

- Another record that stores the number of pixels in it, as well as the Coordinate designated as that group's centroid.
- to Cs v Row formats the group as size,x,y.
- compareTo compares size first, then centroid x, then centroid y.

## BinaryGroupFinder

- Interface for DfsBinaryGroupFinder.
- Declares a method that accepts an int[][].
- Requires implementations to return groups in descending order.

## ColorDistanceFinder

- Interface for EuclideanColorDistance.

## ImageBinarizer

- Interface for DistanceImageBinarizer.

## ImageGroupFinder

- Interface for BinarizingImageGroupFinder.

## ImageSummaryApp

- Contains our main method. Command line takes in an image path, a target hex color, and an integer threshold for binarization. The method loads the image, parses the color values into integers, and binarizes by comparing each pixel's value to the target value. All values within the threshold of the target are marked as white; all others are black.

- Next, it converts these values into a new PNG of black and white pixels. Then it finds clusters of white pixels, counted horizontally and vertically, not diagonally. The size of the groups and the centroid for each group are computed. Finally, it creates a new CSV file containing the coordinates and the size of each group.

-  Prints a usage message when fewer than three arguments are provided.
