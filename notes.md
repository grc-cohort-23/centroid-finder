ImageSummaryApp.java 

- This is the main file that runs the program. It loads an a image, finds the pixels close to a target color. 
- Takes image path and target color.
- Provides three arguments
- Loads the image:UsesinputImage = ImageIO.read(new File(inputImagePath));
- black and white image: Uses code: DistanceImageBinarizer to turn the image black and white.
 
 EuclideanColorDistance: 
 - Calculates the difference between two colors by comparing their red,green, and blue (rgb) values. 



BinarizingImageGroupFinder:
-takes an image and converts it to a binary using ImageBinarizer and then passes that array to BinaryGroupFinder and returns the list of groups.



 
 