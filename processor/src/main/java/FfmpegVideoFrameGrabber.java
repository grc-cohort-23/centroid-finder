import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
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
 *
 * Error handling: JavaCV reports every decoding problem as a
 * {@link FrameGrabber.Exception}, which is a subclass of {@link IOException}. Failures while
 * opening the video surface from the constructor as an IOException. Failures while reading
 * later frames surface from {@link #nextFrame()} as an {@link UncheckedIOException}, because
 * the VideoFrameGrabber interface does not allow checked exceptions there. Callers can catch
 * those two types to tell "could not open" apart from "broke while reading".
 */
public class FfmpegVideoFrameGrabber implements VideoFrameGrabber, AutoCloseable {
    private final FFmpegFrameGrabber grabber;
    private final Java2DFrameConverter converter;
    private Frame lookaheadFrame;

    /**
     * Constructs a FfmpegVideoFrameGrabber that reads frames from the video at the given path.
     *
     * @param videoPath the path to the video file to decode
     * @throws IOException if the video file cannot be opened or started, or its first frame
     *         cannot be decoded (for example, the file is missing or is not a video)
     */
    public FfmpegVideoFrameGrabber(String videoPath) throws IOException {
        this.grabber = new FFmpegFrameGrabber(videoPath);
        this.converter = new Java2DFrameConverter();
        try {
            this.grabber.start();
            this.lookaheadFrame = grabber.grabImage();
        } catch (FrameGrabber.Exception e) {
            // The constructor failed, so the caller never gets an object to close().
            // Release the native FFmpeg resources here so they don't leak.
            try {
                grabber.release();
            } catch (FrameGrabber.Exception releaseFailure) {
                e.addSuppressed(releaseFailure);
            }
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
     * @throws UncheckedIOException if the underlying grabber fails to read the following frame
     *         (the cause is the original {@link FrameGrabber.Exception})
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
        } catch (FrameGrabber.Exception e) {
            // FrameGrabber.Exception is a checked IOException, but nextFrame() is defined by
            // the VideoFrameGrabber interface without a throws clause, so wrap it in the
            // standard unchecked wrapper for I/O failures.
            throw new UncheckedIOException("Failed to read next frame", e);
        }

        return new FrameData(image, timestamp);
    }

    /**
     * Stops the underlying FFmpeg grabber and releases its native resources.
     *
     * @throws FrameGrabber.Exception if the grabber fails to stop cleanly
     */
    @Override
    public void close() throws FrameGrabber.Exception {
        grabber.stop();
    }
}
