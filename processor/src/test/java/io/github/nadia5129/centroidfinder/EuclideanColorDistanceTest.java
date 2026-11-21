package io.github.nadia5129.centroidfinder;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class EuclideanColorDistanceTest {

     //Test same colors/both colors red
    @Test
    void distance_sameColor_returnsZero() {
        // Arrange
        int colorA = 0xFF0000;
        int colorB = 0xFF0000;

        EuclideanColorDistance finder = new EuclideanColorDistance();

        // Act
        double actual = finder.distance(colorA, colorB);

        // Assert
        assertEquals(0.0, actual, 0.000001);
    }

    //Test two different colors black and red
    @Test
    void distance_blackAndRed_returns255() {
        // Arrange
        int colorA = 0x000000;
        int colorB = 0xFF0000;

        EuclideanColorDistance finder = new EuclideanColorDistance();

        // Act
        double actual = finder.distance(colorA, colorB);

        // Assert
        assertEquals(255.0, actual, 0.000001);
    }

    //Test black and white
    @Test
    void distance_blackAndWhite_returnsCorrectDistance() {
        // Arrange
        int colorA = 0x000000;
        int colorB = 0xFFFFFF;
        EuclideanColorDistance finder = new EuclideanColorDistance();

        // Act
        double actual = finder.distance(colorA, colorB);

        // Assert
        assertEquals(Math.sqrt(3 * 255 * 255), actual, 0.000001);
    }

    //Test Red and Green 
    @Test
    void distance_redAndGreen_returnsCorrectDistance() {
        // Arrange
        int colorA = 0xFF0000;
        int colorB = 0x00FF00;
        EuclideanColorDistance finder = new EuclideanColorDistance();

        // Act
        double actual = finder.distance(colorA, colorB);

        // Assert
        assertEquals(Math.sqrt(2 * 255 * 255), actual, 0.000001);
    }

    //Test small RGB differences
    @Test
    void distance_smallDifference_returnsFive() {
        // Arrange
        int colorA = 0x000000;
        int colorB = 0x030400;
        EuclideanColorDistance finder = new EuclideanColorDistance();
        // Act
        double actual = finder.distance(colorA, colorB);
        // Assert
        assertEquals(5.0, actual, 0.000001);
    }

    // Test distance is symmetric (tests whether reversing the two colors produces the same distance.)
    @Test
    void distance_reversedColors_returnsSameDistance() {
        // Arrange
        int colorA = 0x123456;
        int colorB = 0xABCDEF;
        EuclideanColorDistance finder = new EuclideanColorDistance();

        // Act
        double distanceAB = finder.distance(colorA, colorB);
        double distanceBA = finder.distance(colorB, colorA);

        // Assert
        assertEquals(distanceAB, distanceBA, 0.000001);
    }
    
    //Test Green channel only
    @Test
    void distance_blackAndGreen_returns255() {
        // Arrange
        int colorA = 0x000000;
        int colorB = 0x00FF00;
        EuclideanColorDistance finder = new EuclideanColorDistance();

        // Act
        double actual = finder.distance(colorA, colorB);

        // Assert
        assertEquals(255.0, actual, 0.000001);
    }
        
    // Blue channel only
    @Test
    void distance_blackAndBlue_returns255() {
        // Arrange
        int colorA = 0x000000;
        int colorB = 0x0000FF;
        EuclideanColorDistance finder = new EuclideanColorDistance();

        // Act
        double actual = finder.distance(colorA, colorB);

        // Assert
        assertEquals(255.0, actual, 0.000001);
    }

    // Smallest possible nonzero distance
    @Test
    void distance_oneUnitDifference_returnsOne() {
        // Arrange
        int colorA = 0x000000;
        int colorB = 0x000001;
        EuclideanColorDistance finder = new EuclideanColorDistance();

        // Act
        double actual = finder.distance(colorA, colorB);

        // Assert
        assertEquals(1.0, actual, 0.000001);
    }

    // All three channels differ by different amounts: 3-4-12 gives exactly 13
    @Test
    void distance_allChannelsDifferent_returnsThirteen() {
        // Arrange
        int colorA = 0x000000;
        int colorB = 0x03040C;
        EuclideanColorDistance finder = new EuclideanColorDistance();

        // Act
        double actual = finder.distance(colorA, colorB);

        // Assert: 3^2 + 4^2 + 12^2 = 169
        assertEquals(13.0, actual, 0.000001);
    }

    // Some dials go up and some go down between the two colors (red goes up
    // by 2, green goes down by 3, blue goes down by 6). Distance should never
    // be negative, so going down has to count the same as going up.
    // (2*2 + 3*3 + 6*6 = 49, and the square root of 49 is 7.)
    @Test
    void distance_mixedSignDifferences_returnsSeven() {
        // Arrange
        int colorA = 0x0A141E; // red 10, green 20, blue 30
        int colorB = 0x0C1118; // red 12, green 17, blue 24
        EuclideanColorDistance finder = new EuclideanColorDistance();

        // Act
        double actual = finder.distance(colorA, colorB);

        // Assert
        assertEquals(7.0, actual, 0.000001);
    }
    
    // Some channels go up and some go down, so negative differences must work
    @Test
    void distance_bothColorsNonZero_returnsCorrectDistance() {
        // Arrange
        int colorA = 0x102030;
        int colorB = 0x405060;
        EuclideanColorDistance finder = new EuclideanColorDistance();

        // Act
        double actual = finder.distance(colorA, colorB);

        // Assert
        assertEquals(83.13843876330611, actual, 0.000001);
    }
}
    

