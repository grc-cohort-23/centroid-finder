import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

/** Reads frames from a video file with JavaCV, one every stepSeconds. */
public class JavaCvFrameSource implements FrameSource {
    private final FFmpegFrameGrabber grabber;
    private final Java2DFrameConverter converter = new Java2DFrameConverter();
    private final long durationUs;
    private final long stepUs;
    private long nextUs = 0;

    public JavaCvFrameSource(String path, double stepSeconds) throws IOException {
        if (stepSeconds <= 0) {
            throw new IllegalArgumentException("stepSeconds must be positive");
        }
        this.stepUs = (long) (stepSeconds * 1_000_000);
        this.grabber = new FFmpegFrameGrabber(path);
        try {
            grabber.start();
        } catch (IOException e) {
            grabber.release();
            throw e;
        }
        this.durationUs = grabber.getLengthInTime();
    }

    @Override
    public Optional<TimedFrame> nextFrame() throws IOException {
        if (nextUs >= durationUs) {
            return Optional.empty();
        }
        grabber.setTimestamp(nextUs);
        Frame frame = grabber.grabImage();
        if (frame == null) {
            return Optional.empty();
        }
        BufferedImage image = converter.convert(frame);
        if (image == null) {
            return Optional.empty();
        }
        TimedFrame result = new TimedFrame(nextUs / 1_000_000.0, image);
        nextUs += stepUs;
        return Optional.of(result);
    }

    @Override
    public void close() throws IOException {
        converter.close();
        grabber.close();
    }
}