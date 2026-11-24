package io.github.tonymythical.centroidFinder;

/**
 * Calculates Euclidean color distance between RGB colors in 3D color space.
 */
public class EuclideanColorDistance implements ColorDistanceFinder {
    /**
     * Returns the euclidean color distance between two hex RGB colors.
     * 
     * Each color is represented as a 24-bit integer in the form 0xRRGGBB, where
     * RR is the red component, GG is the green component, and BB is the blue component,
     * each ranging from 0 to 255.
     * 
     * The Euclidean color distance is calculated by treating each color as a point
     * in 3D space (red, green, blue) and applying the Euclidean distance formula:
     * 
     * sqrt((r1 - r2)^2 + (g1 - g2)^2 + (b1 - b2)^2)
     * 
     * @param colorA the first color as a 24-bit hex RGB integer
     * @param colorB the second color as a 24-bit hex RGB integer
     * @return the Euclidean distance between the two colors
     */
    @Override
    public double distance(int colorA, int colorB) {
        int dr = getR(colorA) - getR(colorB);
        int dg = getG(colorA) - getG(colorB);
        int db = getB(colorA) - getB(colorB);
        return Math.sqrt(dr * dr + dg * dg + db * db);
    }

    /**
     * Returns the squared Euclidean color distance between two hex RGB colors.
     * Useful for performance-sensitive comparisons that avoid square root operations.
     *
     * @param colorA the first color as a 24-bit hex RGB integer
     * @param colorB the second color as a 24-bit hex RGB integer
     * @return the squared Euclidean distance
     */
    public double distanceSquared(int colorA, int colorB) {
        int dr = getR(colorA) - getR(colorB);
        int dg = getG(colorA) - getG(colorB);
        int db = getB(colorA) - getB(colorB);
        return dr * dr + dg * dg + db * db;
    }

    /**
     * Extracts the 8-bit red component from a 24-bit RGB integer.
     *
     * @param color the 24-bit RGB integer
     * @return red component (0-255)
     */
    public int getR(int color) {
        return (color >> 16) & 0xFF;
    }

    /**
     * Extracts the 8-bit green component from a 24-bit RGB integer.
     *
     * @param color the 24-bit RGB integer
     * @return green component (0-255)
     */
    public int getG(int color) {
        return (color >> 8) & 0xFF; 
    }

    /**
     * Extracts the 8-bit blue component from a 24-bit RGB integer.
     *
     * @param color the 24-bit RGB integer
     * @return blue component (0-255)
     */
    public int getB(int color) {
        return color & 0xFF; 
    }
}
