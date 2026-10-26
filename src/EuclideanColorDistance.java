
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
        /* 
            hex values are base 16 not base 10

            EXAMPLE: Green portion 34  0x123456

            Base 10: (3 X 10)+(4 X 1) = 34  
            Base 16: (3 X 16)+(4 X 1) = 52  ---> >> 16 ---> 00110100  

            0xFF is 8 1's in binary --> 1111111 so an "&" flips all bits to zero that arent the first 8 bits
            this isolates the value we want in the greens place in this example because shifting the green still 
            leaves the remaining red/greater bits

            THIS IS A MASK
         */


        int R1 = colorA >> 16 & 0xFF;
        int G1 = colorA >> 8  & 0xFF;
        int B1 = colorA  & 0xFF;

        int R2 = colorB >> 16 & 0xFF;
        int G2 = colorB >> 8  & 0xFF;
        int B2 = colorB & 0xFF;

        double eDist = Math.sqrt(
            Math.pow(R1 - R2, 2) +
            Math.pow(G1 - G2, 2) +
            Math.pow(B1 - B2, 2)  
        );

        return eDist;
    }

    public static void main(String[] args) {
        EuclideanColorDistance d = new EuclideanColorDistance();

        int colorA = 0xFFFFFF;
        int colorB = 0x000000;

        double val =  d.distance(colorA,colorB);


        // int R1 = colorA >> 16 & 0xFF;
        // int G1 = colorA >> 8  & 0xFF;
        // int B1 = colorA  & 0xFF;

        // int R2 = colorB >> 16 & 0xFF;
        // int G2 = colorB >> 8  & 0xFF;
        // int B2 = colorB & 0xFF;

        // double eDist = Math.sqrt(
        //     Math.pow(R1 - R2, 2) +
        //     Math.pow(G1 - G2, 2) +
        //     Math.pow(B1 - B2, 2)  
        // );
        // List<Integer> A = List.of(R1 , G1, B1);
        // List<Integer> B = List.of(R2 , G2, B2);

        // System.out.println("A:  "+A);
        // System.out.println("B:  "+B);


        // System.out.println(val);

        //System.out.println(0x255 >> 4);
        // System.out.println(120 << 16);

    }
}
