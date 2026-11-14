import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * An implementation of the VideoFrameGrabber interface that decodes a video file using
 * the JavaCV FFmpeg bindings.
 *
 * This class opens a video with FFmpegFrameGrabber and converts each decoded frame into a
 * BufferedImage using a Java2DFrameConverter. Each frame's timestamp (reported by FFmpeg in
 * microseconds) is converted to seconds since the start of the video.
 *
 * One frame is read ahead of time so that hasNext() can report whether a frame is available
 * without consuming it.
 */
public class FfmpegVideoFrameGrabber implements VideoFrameGrabber, AutoCloseable {
    private final FFmpegFrameGrabber grabber;
    private final Java2DFrameConverter converter;
    private Frame lookaheadFrame;

    /**
     * Constructs a FfmpegVideoFrameGrabber that reads frames from the video at the given path.
     *
     * @param videoPath the path to the video file to decode
     * @throws IOException if the video file cannot be opened or started
     */
    public FfmpegVideoFrameGrabber(String videoPath) throws IOException {
        this.grabber = new FFmpegFrameGrabber(videoPath);
        this.converter = new Java2DFrameConverter();
        try {
            this.grabber.start();
            this.lookaheadFrame = grabber.grabImage();
        } catch (Exception e) {
            throw new IOException("Failed to open video: " + videoPath, e);
        }
    }

    /**
     * Returns whether another video frame is available to read.
     *
     * @return true if nextFrame() can be called again, false if the video is exhausted
     */
    @Override
    public boolean hasNext() {
        return lookaheadFrame != null;
    }

    /**
     * Returns the next decoded frame of the video along with its timestamp in seconds.
     *
     * @return a FrameData containing the next frame's image and its timestamp
     * @throws NoSuchElementException if there is no next frame available
     * @throws RuntimeException if the underlying grabber fails to read the following frame
     */
    @Override
    public FrameData nextFrame() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more frames in video");
        }

        BufferedImage image = converter.convert(lookaheadFrame);
        // FFmpeg reports timestamps in microseconds, so convert to seconds.
        double timestamp = lookaheadFrame.timestamp / 1_000_000.0;

        try {
            lookaheadFrame = grabber.grabImage();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read next frame", e);
        }

        return new FrameData(image, timestamp);
    }

    /**
     * Stops the underlying FFmpeg grabber and releases its native resources.
     *
     * @throws Exception if the grabber fails to stop cleanly
     */
    @Override
    public void close() throws Exception {
        grabber.stop();
    }
}
