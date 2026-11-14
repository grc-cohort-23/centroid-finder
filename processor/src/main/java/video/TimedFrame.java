package video;

import java.awt.image.BufferedImage;

public record TimedFrame(double seconds, BufferedImage image) {}
