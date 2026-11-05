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
     * This gives a measure of how visually different the two colors are.
     * 
     * @param colorA the first color as a 24-bit hex RGB integer
     * @param colorB the second color as a 24-bit hex RGB integer
     * @return the Euclidean distance between the two colors
     */
    @Override
    public double distance(int colorA, int colorB) {
        
        // split both colors into three num: {red, green, blue} 
        int[] rgbA = toRgb(colorA);
        int[] rgbB = toRgb(colorB);

        int dRed = rgbA[0] - rgbB[0];
        int dGreen = rgbA[1] - rgbB[1];
        int dBlue = rgbA[2] - rgbB[2];

        // square each difference, add them then take square root
        int sum = dRed * dRed + dGreen * dGreen + dBlue * dBlue;
        return Math.sqrt(sum);

    }
    // helper method to split into red gree blue from 0 - 255
    private int[] toRgb(int color) {
        int red = (color >> 16) & 0xFF;   
        int green = (color >> 8) & 0xFF; 
        int blue = color & 0xFF;
        return new int[] {red, green, blue};
    }
}
