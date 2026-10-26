## Image Summary App
Groups colors into 0s or 1s, which convert to either black or white. Which then gets sorted into groups, from smallest to largest, due to clusters. Which are colorized pixels that are touching each other. 

## ImagesGroup Finder
- Where we find connected group in an images and got sorted descending

## ImageBinarizer
- will convert BufferedImage into binary 2d array (convert back and forth between color images nd black-n-white 0/1 arrays.)

## BinarizingImageGroupFinder
Implementation of ImageGroupFinder that first binarizes given image and then finds groups of white pixels.

## BinaryGroupFinder
 returns a list of sorted groups. The group's size is the number of pixels in the group. The centroid of the group is computed as the average of each of the pixel locations across each dimension.

 ## ColorDistanceFinder
 Defines an interface for computing the distance between two colors, with 24-bit, using red, green, and blue

 ## Coordinate
 Shows location of image or array, usually with x,y (row, column)

 ## DfsBinaryGroupFinder
  The method returns a list of sorted groups. The group's size is the number of pixels in the group. The centroid of the group
 is computed as the average of each of the pixel locations across each dimension.

 ## DistanceImageBinarizer
 An implementation of the ImageBinarizer interface that uses color distance to determine whether each pixel should be black or white in the binary image. Constructs a DistanceImageBinarizer using the given ColorDistanceFinder,  target color, and threshold.


## Group
- Where we will find that blobs of white where we will find their center and x,y and also the size of it.
- We gonna compare their size and return something with (size, x,y)

## EuclideanColorDistance
- Where we will return euclidean color distance between two hex RGB colors which basically tell us how different two colors are.

## Wave 4 Notes 
- java.awt.image.BufferedImage is a Java class that lets you store, access, and modify images directly in your program's memory.

java — Java's standard library
awt — Abstract Window Toolkit, a library for graphics and user interfaces.
image — A package containing classes for working with images
BufferedImage — A class representing pixel data

In Java, getRGB() and setRGB() are methods of BufferedImage that let you read and change the color of individual pixels in an image.

getRGB() — Read a pixel's color
getRGB(x, y) returns the color of a pixel at the specified coordinates as an int (integer).

 = > int pixel = image.getRGB(10, 20);

This means:
- 10 is the horizontal coordinate (x).
- 20 is the vertical coordinate (y).
- pixel stores the color information as an integer.

setRGB() — Change a pixel's color
setRGB(x, y, rgb) changes the color of a pixel at the specified coordinates.

= > image.setRGB(10, 20, Color.RED.getRGB());

This changes the pixel at (10, 20) to red.
- 10, 20 identify the pixel.
- Color.RED.getRGB() converts the red color into the integer format that setRGB() expects.

## Mock and fakes notes
- Stub: a pretend helper that just give back a fixed answer. For example: "Meo Meo is a fat cat." so the helper will never print out "Meo Meo is skinny" or "Meo Meo is a fat dog". She is a cat and always a fat cat
- Fake: a pretend helper that actually work but in a simpler way than a real one. For example: If the two color are equal, return 0 else 100.
- Mock (spy): a pretend helper that keep notes on how it was used, so the test can ask it afterward like:"how many times were you call?"