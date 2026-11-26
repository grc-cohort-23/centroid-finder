import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

public class DebugFrameRendererTest {
  private static final int WIDTH = 200;
  private static final int HEIGHT = 200;
  private static final int MARKER_RGB = DebugFrameRenderer.MARKER.getRGB();

  private static BufferedImage filled(int width, int height, Color color) {
    BufferedImage image =
        new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        image.setRGB(x, y, color.getRGB());
      }
    }
    return image;
  }

  private static BufferedImage render(List<Group> groups, Coordinate centroid) {
    return new DebugFrameRenderer().render(
        filled(WIDTH, HEIGHT, Color.GREEN),
        filled(WIDTH, HEIGHT, Color.WHITE),
        groups,
        centroid,
        1.5);
  }

  @Test
  public void testPlacesFrameAndMaskSideBySide() {
    BufferedImage panels = render(List.of(), new Coordinate(-1, -1));

    assertEquals(WIDTH * 2, panels.getWidth());
    assertEquals(HEIGHT, panels.getHeight());
    assertEquals(Color.GREEN.getRGB(), panels.getRGB(WIDTH - 1, HEIGHT - 1));
    assertEquals(Color.WHITE.getRGB(), panels.getRGB(WIDTH, HEIGHT - 1));
  }

  @Test
  public void testDrawsCrosshairOnCentroidInBothPanels() {
    Coordinate centroid = new Coordinate(120, 150);

    BufferedImage panels =
        render(List.of(new Group(50, centroid)), centroid);

    assertEquals(MARKER_RGB, panels.getRGB(120, 150));
    assertEquals(MARKER_RGB, panels.getRGB(WIDTH + 120, 150));
  }

  @Test
  public void testDrawsNoCrosshairWithoutDetection() {
    BufferedImage panels = render(List.of(), new Coordinate(-1, -1));

    for (int y = 0; y < panels.getHeight(); y++) {
      for (int x = 0; x < panels.getWidth(); x++) {
        assertEquals(false, panels.getRGB(x, y) == MARKER_RGB);
      }
    }
  }

  @Test
  public void testFitForVideoRoundsOddDimensionsDownToEven() {
    BufferedImage fitted = DebugFrameRenderer.fitForVideo(
        filled(101, 51, Color.GREEN), 1920);

    assertEquals(100, fitted.getWidth());
    assertEquals(50, fitted.getHeight());
  }

  @Test
  public void testFitForVideoShrinksWideImagesKeepingProportions() {
    BufferedImage fitted = DebugFrameRenderer.fitForVideo(
        filled(3840, 1080, Color.GREEN), 1920);

    assertEquals(1920, fitted.getWidth());
    assertEquals(540, fitted.getHeight());
  }

  @Test
  public void testFitForVideoReturnsImagesThatAlreadyFitUnchanged() {
    BufferedImage image = filled(100, 50, Color.GREEN);

    assertSame(image, DebugFrameRenderer.fitForVideo(image, 1920));
  }
}
