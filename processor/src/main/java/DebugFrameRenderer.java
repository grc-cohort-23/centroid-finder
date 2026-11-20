import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Draws what the tracker saw in one frame: the original frame on the left and
 * the binarized mask on the right, with a crosshair on the tracked centroid in
 * both panels and a caption listing the sizes of the largest blobs.
 */
public class DebugFrameRenderer {
  static final Color MARKER = new Color(255, 0, 255);

  private static final int BLOB_SIZES_SHOWN = 3;

  public BufferedImage render(
      BufferedImage frame,
      BufferedImage mask,
      List<Group> groups,
      Coordinate centroid,
      double seconds) {
    int width = frame.getWidth();
    int height = frame.getHeight();

    BufferedImage panels = new BufferedImage(
        width * 2, height, BufferedImage.TYPE_3BYTE_BGR);

    Graphics2D graphics = panels.createGraphics();
    graphics.drawImage(frame, 0, 0, null);
    graphics.drawImage(mask, width, 0, null);

    if (centroid.x() >= 0) {
      drawCrosshair(graphics, centroid.x(), centroid.y(), height);
      drawCrosshair(graphics, width + centroid.x(), centroid.y(), height);
    }

    drawCaption(graphics, caption(groups, centroid, seconds), height);
    graphics.dispose();

    return panels;
  }

  /**
   * Shrinks an image to fit maxWidth and makes both dimensions even, which
   * the H.264 video encoder requires.
   */
  public static BufferedImage fitForVideo(BufferedImage image, int maxWidth) {
    double scale = Math.min(1.0, (double) maxWidth / image.getWidth());

    int width = evenAtLeastTwo(image.getWidth() * scale);
    int height = evenAtLeastTwo(image.getHeight() * scale);

    if (width == image.getWidth() && height == image.getHeight()) {
      return image;
    }

    BufferedImage fitted = new BufferedImage(
        width, height, BufferedImage.TYPE_3BYTE_BGR);

    Graphics2D graphics = fitted.createGraphics();
    graphics.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    graphics.drawImage(image, 0, 0, width, height, null);
    graphics.dispose();

    return fitted;
  }

  private static int evenAtLeastTwo(double size) {
    return Math.max(2, (int) size & ~1);
  }

  private static String caption(
      List<Group> groups, Coordinate centroid, double seconds) {
    String time = String.format("t=%.3fs", seconds);

    if (groups.isEmpty()) {
      return time + "  NO DETECTION (-1,-1)";
    }

    String largestSizes = groups.stream()
        .limit(BLOB_SIZES_SHOWN)
        .map(group -> String.valueOf(group.size()))
        .collect(Collectors.joining(", "));

    return String.format(
        "%s  centroid=(%d,%d)  blobs=%d  largest=%s",
        time, centroid.x(), centroid.y(), groups.size(), largestSizes);
  }

  private static void drawCrosshair(
      Graphics2D graphics, int x, int y, int imageHeight) {
    int arm = Math.max(12, imageHeight / 25);
    int thickness = Math.max(2, imageHeight / 360);

    // A dark outline keeps the marker visible on both black and white pixels.
    graphics.setColor(Color.BLACK);
    graphics.setStroke(new BasicStroke(thickness + 2));
    graphics.drawLine(x - arm, y, x + arm, y);
    graphics.drawLine(x, y - arm, x, y + arm);

    graphics.setColor(MARKER);
    graphics.setStroke(new BasicStroke(thickness));
    graphics.drawLine(x - arm, y, x + arm, y);
    graphics.drawLine(x, y - arm, x, y + arm);
  }

  private static void drawCaption(
      Graphics2D graphics, String text, int imageHeight) {
    graphics.setFont(new Font(
        Font.SANS_SERIF, Font.BOLD, Math.max(12, imageHeight / 30)));
    graphics.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    FontMetrics metrics = graphics.getFontMetrics();
    int padding = metrics.getHeight() / 4;

    graphics.setColor(Color.BLACK);
    graphics.fillRect(
        0, 0,
        metrics.stringWidth(text) + padding * 2,
        metrics.getHeight() + padding * 2);

    graphics.setColor(Color.WHITE);
    graphics.drawString(text, padding, padding + metrics.getAscent());
  }
}
