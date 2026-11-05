package io.github.nadia5129.centroidfinder;

import java.awt.image.BufferedImage;
import java.io.IOException;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

/**
 * Reads video frames using JavaCV.
 *
 * This is the ONLY class that knows about JavaCV. Everything else uses the
 * FrameReader interface, so switching libraries means only replacing this class.
 */
public class JavaCVFrameReader implements FrameReader {

    //tool that reads the video
    private final FFmpegFrameGrabber grabber;
    //turns a JavaCV Frame into a BufferedImage
    private final Java2DFrameConverter converter;

    //Opens the video at the given path
    public JavaCVFrameReader(String videoPath) throws IOException {
        this.grabber = new FFmpegFrameGrabber(videoPath);
        this.converter = new Java2DFrameConverter();
        //open the video (throws an IOException if the file is missing or not a video)
        grabber.start();
    }

    @Override
    public BufferedImage nextFrame() throws IOException {
        //grabImage() returns the next video frame, or null when the video ends
        Frame frame = grabber.grabImage();
        if (frame == null) {
            return null;
        }
        return converter.convert(frame);
    }

    @Override
    public double getTimestamp() {
        //getTimestamp() is in microseconds, so divide by 1,000,000.0 to get seconds
        return grabber.getTimestamp() / 1_000_000.0;
    }

    @Override
    public void close() throws IOException {
        //stops the video and frees the native (C) memory FFmpeg was using
        grabber.close();
        converter.close();
    }
}
