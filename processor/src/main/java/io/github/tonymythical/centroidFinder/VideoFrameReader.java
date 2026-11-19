package io.github.tonymythical.centroidFinder;

import java.awt.image.BufferedImage;
import java.io.IOException;

public interface VideoFrameReader extends AutoCloseable {

    BufferedImage nextFrame() throws IOException;

    @Override
    void close() throws IOException;


}

    

