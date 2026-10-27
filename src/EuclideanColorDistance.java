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
     * 
     * 
     * 
     * pseudo code
     * Component	Hex value	Decimal value
        Red	            12	        18
        Green	        34	        52
        Blue	        AB	        171


        Component	Hex value
        Red	            AA
        Green       	BB
        Blue        	CC

        // Each RGB color is stored as a hexadecimal number (0xRRGGBB).
        // Each color component takes up 8 bits.
        // >> 16 shifts past green and blue to extract red.
        // >> 8 shifts past blue to extract green.
        // Blue doesn't need shifting because it's already in the last 8 bits
        // & 0xFF keeps only the last 8 bits of the number (0–255)
        
        extract red:
        int redA = (colorA >> 16) & 0xFF;
        int redB = (colorB >> 16) & 0xFF;

        extract green: 
        int greenA = (colorA >> 8) & 0xFF;
        int greenB = (colorB >> 8) & 0xFF;

        extract blue: 
        int blueA = colorA & 0xFF;
        int blueB = colorB & 0xFF;

        int redDiff = redA - redB;
        int greenDiff = greenA - greenB;
        int blueDiff = blueA - blueB;

        find square by multiplying by itself
        int redSquared = redDiff * redDiff;
        int greenSquared = greenDiff * greenDiff;
        int blueSquared = blueDiff * blueDiff;

        int sum = redSquared + greenSquared + blueSquared;

        java has built in method called Math.sqrt() that calculates sqaure root
        double distance = Math.sqrt(sum);



     * 
     * @param colorA the first color as a 24-bit hex RGB integer
     * @param colorB the second color as a 24-bit hex RGB integer
     * @return the Euclidean distance between the two colors
     */
    @Override
    public double distance(int colorA, int colorB) {

        //extract red:
        int redA = (colorA >> 16) & 0xFF;
        int redB = (colorB >> 16) & 0xFF;

        //extract green: 
        int greenA = (colorA >> 8) & 0xFF;
        int greenB = (colorB >> 8) & 0xFF;

       // extract blue: 
        int blueA = colorA & 0xFF;
        int blueB = colorB & 0xFF;

        //calculate the difference and store in a new variable 
        int redDiff = redA - redB;
        int greenDiff = greenA - greenB;
        int blueDiff = blueA - blueB;

        //Calculate the square of each difference
        //(find square by multiplying by itself)
        int redSquared = redDiff * redDiff;
        int greenSquared = greenDiff * greenDiff;
        int blueSquared = blueDiff * blueDiff;

        //add the sqaured differences 
        int sum = redSquared + greenSquared + blueSquared;

        //calcular squared root with the help of math.sqrt
        double distance = Math.sqrt(sum);

        return distance;
        
    }
}
