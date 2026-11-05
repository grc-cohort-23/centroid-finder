package io.github.akurfurst.centroidFinder;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EuclideanColorDistanceTest {
    private final EuclideanColorDistance distanceFinder = new EuclideanColorDistance();

    @Test
    void returnsZeroForIdenticalColors() {
        assertEquals(0.0, distanceFinder.distance(0x12ABEF, 0x12ABEF), 0.0);
    }

    @Test
    void returnsZeroForBlackComparedWithItself() {
        assertEquals(0.0, distanceFinder.distance(0x000000, 0x000000), 0.0);
    }

    @Test
    void calculatesDistanceWhenOnlyRedDiffers() {
        assertEquals(3.0, distanceFinder.distance(0x030000, 0x000000), 0.0);
    }

    @Test
    void calculatesDistanceWhenOnlyGreenDiffers() {
        assertEquals(4.0, distanceFinder.distance(0x000400, 0x000000), 0.0);
    }

    @Test
    void calculatesDistanceWhenOnlyBlueDiffers() {
        assertEquals(5.0, distanceFinder.distance(0x000005, 0x000000), 0.0);
    }

    @Test
    void calculatesEuclideanDistanceAcrossAllChannels() {
        assertEquals(Math.sqrt(50.0), distanceFinder.distance(0x010203, 0x040608), 0.0);
    }

    @Test
    void calculatesDistanceBetweenComplexColors() {
        assertEquals(Math.sqrt(3584.0), distanceFinder.distance(0x2A7FC1, 0x5A6FA1), 0.0);
    }

    @Test
    void calculatesDistanceAcrossDifferentRgbChannels() {
        assertEquals(Math.sqrt(35649.0), distanceFinder.distance(0xE34B7A, 0x4A9FC2), 0.0);
    }

    @Test
    void calculatesDistanceWhenOnlyTwoChannelsDifferBetweenComplexColors() {
        assertEquals(Math.sqrt(12500.0), distanceFinder.distance(0xC86432, 0x649632), 0.0);
    }

    // colorA-colorB = colorB-colorA
    @Test
    void distanceIsSymmetric() {
        assertEquals(distanceFinder.distance(0x123456, 0xABCDEF),
                distanceFinder.distance(0xABCDEF, 0x123456), 0.0);
    }

    @Test
    void calculatesMaximumDistanceBetweenBlackAndWhite() {
        assertEquals(Math.sqrt(3.0 * 255.0 * 255.0),
                distanceFinder.distance(0x000000, 0xFFFFFF), 0.0);
    }
}
