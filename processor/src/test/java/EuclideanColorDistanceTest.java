import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EuclideanColorDistanceTest {

  @Test
  public void testSameColorDistance() {
    EuclideanColorDistance finder = new EuclideanColorDistance();

    double result = finder.distance(0xFF0000, 0xFF0000);

    assertEquals(0.0, result, 0.000001);
  }

  @Test
  public void testBlackToWhiteDistance() {
    EuclideanColorDistance finder = new EuclideanColorDistance();

    double result = finder.distance(0x000000, 0xFFFFFF);

    assertEquals(Math.sqrt(195075), result, 0.000001);
  }

  @Test
  public void testRedToGreenDistance() {
    EuclideanColorDistance finder = new EuclideanColorDistance();

    double result = finder.distance(0xFF0000, 0x00FF00);

    assertEquals(Math.sqrt(130050), result, 0.000001);
  }

  @Test
  public void testColorDistanceSymmetry() {
    EuclideanColorDistance finder = new EuclideanColorDistance();

    double distanceAB = finder.distance(0xFF8040, 0x123456);
    double distanceBA = finder.distance(0x123456, 0xFF8040);

    assertEquals(distanceAB, distanceBA, 0.000001);
  }
  @Test
public void testSingleChannelDifference() {
    EuclideanColorDistance finder = new EuclideanColorDistance();

    double result = finder.distance(0x123456, 0x123466);

    assertEquals(16.0, result, 0.000001);
}
}