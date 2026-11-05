import java.awt.image.BufferedImage;

/**
 * One frame of video and when it appears.
 *
 * @param seconds time since the start of the video
 * @param image   the frame's pixels
 */
public record TimedFrame(double seconds, BufferedImage image) {}
