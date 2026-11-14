import java.awt.image.BufferedImage;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

public class JavaCvFrameSource implements FrameSource {
  private final FFmpegFrameGrabber grabber;
  private final Java2DFrameConverter converter;

  public JavaCvFrameSource(String inputPath) throws Exception {
    grabber = new FFmpegFrameGrabber(inputPath);
    converter = new Java2DFrameConverter();

    try {
      grabber.start();
    } catch (Exception failure) {
      try {
        close();
      } catch (Exception cleanupFailure) {
        failure.addSuppressed(cleanupFailure);
      }
      throw failure;
    }
  }

  @Override
  public VideoFrame nextFrame() throws Exception {
    Frame frame = grabber.grabImage();

    if (frame == null) {
      return null;
    }

    BufferedImage image = converter.convert(frame);
    double seconds = frame.timestamp / 1_000_000.0;

    return new VideoFrame(image, seconds);
  }

  public double frameRate() {
    return grabber.getVideoFrameRate();
  }

  @Override
  public void close() throws Exception {
    try {
      grabber.close();
    } finally {
      converter.close();
    }
  }
}