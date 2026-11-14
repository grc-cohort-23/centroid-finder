** Image Summary App **
- takes in 3 command line arguments: the image path, hex color, and color threshold
- It then loads the image using ImageIO.read image path file 
- It turns the color from hex string to a 24-bit integer (0xRRGGBB)
- It builds a new DistanceImageBinarizer(new EuclideanColorDistance()), target, threshold
- Then it calls toBinaryArray and toBufferedImage, then writes binarized.png
-Then it builds a new BinarizingImageGroupFinder, a DfsBinaryGroupFinder() and then callse findConnectedGroups(inputImage)
- then we write each group's toCsvRow() to groups.csv
