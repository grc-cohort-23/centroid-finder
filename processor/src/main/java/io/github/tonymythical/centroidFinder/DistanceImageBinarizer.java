package io.github.tonymythical.centroidFinder;

import java.awt.image.BufferedImage;

/**
 * An implementation of the ImageBinarizer interface that uses color distance
 * to determine whether each pixel should be black or white in the binary image.
 * 
 * Optimized for high-throughput video processing using bulk pixel buffer extraction
 * and squared Euclidean color distance comparison to eliminate square root calculation.
 */
public class DistanceImageBinarizer implements ImageBinarizer {
    private final ColorDistanceFinder distanceFinder;
    private final int threshold;
    private final int targetColor;
    private final int targetR;
    private final int targetG;
    private final int targetB;
    private final int thresholdSq;
    private final boolean isEuclidean;

    /**
     * Constructs a DistanceImageBinarizer using the given ColorDistanceFinder,
     * target color, and threshold.
     *
     * @param distanceFinder an object that computes the distance between two colors
     * @param targetColor the reference color as a 24-bit hex RGB integer (0xRRGGBB)
     * @param threshold the distance threshold used to decide whether a pixel is white or black
     */
    public DistanceImageBinarizer(ColorDistanceFinder distanceFinder, int targetColor, int threshold) {
        this.distanceFinder = distanceFinder;
        this.targetColor = targetColor;
        this.threshold = threshold;
        this.targetR = (targetColor >> 16) & 0xFF;
        this.targetG = (targetColor >> 8) & 0xFF;
        this.targetB = targetColor & 0xFF;
        this.thresholdSq = threshold * threshold;
        this.isEuclidean = distanceFinder instanceof EuclideanColorDistance;
    }

    /**
     * Converts the given BufferedImage into a binary 2D array using color distance and a threshold.
     * Uses bulk pixel reading for optimal performance across video frames.
     *
     * @param image the input RGB BufferedImage
     * @return a 2D binary array where 1 represents white and 0 represents black
     */
    @Override
    public int[][] toBinaryArray(BufferedImage image) {
        if (image == null) {
            throw new NullPointerException("Cant be null.");
        }

        int width = image.getWidth();
        int height = image.getHeight();
        int[][] binaryArray = new int[height][width];

        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        int idx = 0;
        if (isEuclidean) {
            for (int y = 0; y < height; y++) {
                int[] row = binaryArray[y];
                for (int x = 0; x < width; x++) {
                    int rgb = pixels[idx++];
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;

                    int dr = r - targetR;
                    int dg = g - targetG;
                    int db = b - targetB;
                    row[x] = (dr * dr + dg * dg + db * db < thresholdSq) ? 1 : 0;
                }
            }
        } else {
            for (int y = 0; y < height; y++) {
                int[] row = binaryArray[y];
                for (int x = 0; x < width; x++) {
                    int rgb = pixels[idx++] & 0xFFFFFF;
                    double distance = distanceFinder.distance(rgb, targetColor);
                    row[x] = (distance < threshold) ? 1 : 0;
                }
            }
        }
        return binaryArray;
    }

    /**
     * Converts a binary 2D array into a BufferedImage.
     *
     * @param image a 2D array of 0s and 1s representing the binary image
     * @return a BufferedImage where black and white pixels are represented with standard RGB hex values
     */
    @Override
    public BufferedImage toBufferedImage(int[][] image) {
        if (image == null || image.length == 0 || image[0].length == 0) {
            throw new IllegalArgumentException("Image array is invalid.");
        }

        int width = image[0].length;
        int height = image.length;
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int[] outPixels = new int[width * height];
        int idx = 0;
        for (int y = 0; y < height; y++) {
            int[] row = image[y];
            for (int x = 0; x < width; x++) {
                outPixels[idx++] = (row[x] == 1) ? 0xFFFFFF : 0x000000;
            }
        }
        bufferedImage.setRGB(0, 0, width, height, outPixels, 0, width);
        return bufferedImage;
    }
}
