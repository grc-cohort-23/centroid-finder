package video;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;


public class JavaCvFrameSource implements FrameSource {

    // Reads frames from the video file; final because it's set once in the constructor
    private final FFmpegFrameGrabber grabber;

    // Turns each Frame into a BufferedImage that the image/centroid code understands
    private final Java2DFrameConverter converter = new Java2DFrameConverter();

    public JavaCvFrameSource(String path) throws Exception{
        grabber = new FFmpegFrameGrabber(path);
        grabber.start();
    }
    
    @Override
    public TimedFrame nextFrame() throws Exception {
        // Grab the next video frame only (grab() would also return audio frames)
        Frame frame = grabber.grabImage();

        // No more frames means the video is finished
        if (frame == null) return null;

        // getTimestamp() is in microseconds, so divide by 1,000,000 to get seconds
        double seconds = grabber.getTimestamp() / 1_000_000.0;

        // Convert the frame to a BufferedImage and pair it with its time
        return new TimedFrame(seconds, converter.convert(frame));
    }
    
    // Required by AutoCloseable: runs automatically at the end of a try-with-resources block
    @Override
    public void close() throws Exception {
        // Stop decoding the video
        grabber.stop();
        // Free the native FFmpeg memory and the file handle
        grabber.release();
        // Free the converter's internal buffers
        converter.close();
    }
}