# Notes 
- The App takes in a image from a file and converts too black and white depending on the distance of the target color.
- The Threshold is parsed into a a 24-bit integer (Hex color)

BinaryGroupFinder.java
- The file is trying to return a list of sums divided by the ammount of image pixels
- Groups are sorted in desc order by group

imageBinarizer.java
- Converts the buffed image into a 2D array where each pixel represent black or white

ColorDistanceFinder.java
- This file trys too find the distance between colors
- The color adds up to be red, green and blue

EuclideanColorDistance.java
- This file has the variables for the colors ranging from 0 to 255.
- Implement using the variables to get the distance value of between the two colors.