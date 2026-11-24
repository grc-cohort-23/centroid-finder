package io.github.nadia5129.centroidfinder;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Reads a video one frame at a time.
 *
 * The rest of the program only uses this interface, so it never needs to know
 * which video library is being used. JavaCVFrameReader is the real version,
 * and tests can use a fake version that returns images made by hand.
 *
 * Extends AutoCloseable so it can be used in a try-with-resources block.
 */
public interface FrameReader extends AutoCloseable {

    /**
     * Returns the next frame of the video, or null when the video is over.
     *
     * The returned image may be reused by the next call, so process it
     * before asking for another frame.
     */
    BufferedImage nextFrame() throws IOException;

    /**
     * Returns the time, in seconds since the start of the video,
     * of the frame most recently returned by nextFrame().
     */
    double getTimestamp();

    /**
     * Closes the video.
     */
    @Override
    void close() throws IOException;
}
