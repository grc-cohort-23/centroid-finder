import java.awt.image.BufferedImage;

/**
 * Represents a single decoded video frame along with when it occurs in the video.
 *
 * Holds the frame's image data and its timestamp, measured in seconds since the
 * start of the video. Returned by VideoFrameGrabber.nextFrame() so callers know
 * both what the frame looks like and when it happened.
 */
public record FrameData(BufferedImage frame, double timestamp) {}
