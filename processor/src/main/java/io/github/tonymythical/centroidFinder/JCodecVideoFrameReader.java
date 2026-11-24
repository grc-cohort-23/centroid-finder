package io.github.tonymythical.centroidFinder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

/**
 * Video frame reader implementation powered by JCodec for decoding MP4 and MOV files.
 */
public class JCodecVideoFrameReader implements VideoFrameReader {

    private final SeekableByteChannel channel;
    private final FrameGrab grabber;

    /**
     * Constructs a JCodecVideoFrameReader for the specified video file.
     *
     * @param videoFile the video file to open and decode
     * @throws IOException if an I/O error occurs opening the channel
     * @throws JCodecException if the video cannot be parsed or demuxed
     */
    public JCodecVideoFrameReader(File videoFile) throws IOException, JCodecException {
        this.channel = NIOUtils.readableChannel(videoFile);
        try {
            this.grabber = FrameGrab.createFrameGrab(channel);
        } catch (IOException | JCodecException e) {
            channel.close();
            throw e;
        }
    }

    /**
     * Decodes and returns the next video frame converted to a BufferedImage.
     *
     * @return the next decoded frame, or null if end of stream is reached
     * @throws IOException if an error occurs during decoding
     */
    @Override
    public BufferedImage nextFrame() throws IOException {
        Picture picture = grabber.getNativeFrame();
        if (picture == null) {
            return null;
        }
        return AWTUtil.toBufferedImage(picture);
    }

    /**
     * Closes the underlying seekable byte channel.
     *
     * @throws IOException if an error occurs while closing the channel
     */
    @Override
    public void close() throws IOException {
        channel.close();
    }
}
