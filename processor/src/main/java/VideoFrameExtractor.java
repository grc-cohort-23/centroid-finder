import java.awt.image.BufferedImage;
import java.io.IOException;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

public class VideoFrameExtractor implements AutoCloseable {
    private final FFmpegFrameGrabber grabber;
    private final Java2DFrameConverter converter = new Java2DFrameConverter();

    public VideoFrameExtractor(String videoPath) throws IOException {
        grabber = new FFmpegFrameGrabber(videoPath);
        grabber.start();
    }

    // Returns the next frame with its time in seconds, or nothing at the end of the videon.
    public VideoFrame nextFrame() throws IOException {
        Frame frame = grabber.grabImage();
        if (frame == null) {
            return null;
        }
        BufferedImage image = converter.convert(frame);
        double seconds = grabber.getTimestamp() / 1_000_000.0; 
        return new VideoFrame(image, seconds);
    }

    @Override
    public void close() throws IOException {
        grabber.stop();
        grabber.release();
        converter.close();
    }
} 