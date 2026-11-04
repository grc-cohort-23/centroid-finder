package io.github.tonymythical.centroidFinder;
import java.io.File;
import java.io.IOException;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.api.JCodecException;
import java.awt.image.BufferedImage;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import org.jcodec.api.FrameGrab;
import org.jcodec.common.io.SeekableByteChannel;

public class JCodecVideoFrameReader implements VideoFrameReader {

    private final SeekableByteChannel channel;
    private final FrameGrab grabber;

    //Constructor goes here
    public JCodecVideoFrameReader(File videoFile) throws IOException, JCodecException {
        this.channel = NIOUtils.readableChannel(videoFile);

        try {
            this.grabber = FrameGrab.createFrameGrab(channel);

        } catch (IOException | JCodecException e) {
            channel.close();
            throw e;
        }

    }

    @Override
    public BufferedImage nextFrame() throws IOException {
        //get hte next frame from the video
        Picture picture = grabber.getNativeFrame();
        //check if there are no more frames 
        if (picture == null) {
            return null;
        }
        //convert image to BufferedImage
        BufferedImage image = AWTUtil.toBufferedImage(picture);
        //return converted image
        return image;
    }

    @Override
    public void close() throws IOException {
        channel.close();

    }


    
}
