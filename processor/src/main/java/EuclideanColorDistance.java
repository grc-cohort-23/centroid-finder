import java.awt.color.*;
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

        int [] color1 = hexToRgb(colorA);
        int [] color2 = hexToRgb(colorB);

        int r1 = color1[0];
        int g1 = color1[1];
        int b1 = color1[2];


        int r2 = color2[0];
        int g2 = color2[1];
        int b2 = color2[2];

        double deltaR = r2 - r1;
        double deltaG = g2 - g1;
        double deltaB = b2 - b1;

        //Distance formula: sqrt((r1 - r2)^2 + (g1 - g2)^2 + (b1 - b2)^2)
        double euclideanColorDistance = Math.sqrt((deltaR * deltaR) + (deltaG * deltaG) + (deltaB * deltaB));
        
        return euclideanColorDistance;
    }

    private int[] hexToRgb(int color) {
        java.awt.Color rgb = new java.awt.Color(color);

        return new int[] {
            rgb.getRed(),
            rgb.getGreen(),
            rgb.getBlue()
        };
    }
}
