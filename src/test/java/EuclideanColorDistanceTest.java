import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class EuclideanColorDistanceTest {
    
     private final EuclideanColorDistance finder = new EuclideanColorDistance();

    // Doubles come out of a square root, so compare within a small tolerance
    private static final double DELTA = 1e-9;

    // Same color twice should be 0, even with a different value in every channel
    @Test
    void identicalColors_returnsZero() {
        assertEquals(0.0, finder.distance(0x123456, 0x123456), DELTA);
    }

    // Channel diffs (3, 4, 0) -> sqrt(9 + 16 + 0) = sqrt(25) = 5
    @Test
    void threeFourZeroDifferences_returnsFive() {
        assertEquals(5.0, finder.distance(0x000000, 0x030400), DELTA);
    }

    // 0x0A141E is (10, 20, 30), 0x1E2814 is (30, 40, 20)
    // Diffs (20, 20, 10) -> sqrt(400 + 400 + 100) = sqrt(900) = 30
    // Blue goes down while red and green go up, so the sign of a diff shouldn't matter
    @Test
    void mixedChannelDifferences_returnsThirty() {
        assertEquals(30.0, finder.distance(0x0A141E, 0x1E2814), DELTA);
    }

    // Argument order shouldn't matter: distance(a, b) == distance(b, a)
    @Test
    void swappedArguments_returnsSameDistance() {
        double forward = finder.distance(0x0A141E, 0x1E2814);
        double backward = finder.distance(0x1E2814, 0x0A141E);

        assertEquals(forward, backward, DELTA);
    }

    // 0x00FF00 vs 0x010000 -> diffs (1, 255, 0) -> sqrt(1 + 65025) = sqrt(65026)
    // Subtracting the whole ints would borrow across bytes and corrupt the neighboring channel
    @Test
    void greenBorrowCase_doesNotBleedIntoNeighboringChannel() {
        assertEquals(Math.sqrt(65026), finder.distance(0x00FF00, 0x010000), DELTA);
    }
}
