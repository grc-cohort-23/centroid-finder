package io.github.nadia5129.centroidfinder;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.WritableRaster;

/**
 * Helpers for TYPE_3BYTE_BGR images, the kind of frame JavaCV gives us.
 *
 * Their pixels are stored in one byte[]: blue, green, red, blue, green, red, ...
 * row after row. Reading or copying that array directly is much faster than
 * going through getRGB or copyData, which convert one pixel or sample at a time.
 */
public final class BgrImages {

    private BgrImages() {
        //only static helpers, so nobody needs to make a BgrImages object
    }

    /**
     * True if the image's pixels are exactly one byte[] of width * height * 3 bytes
     * in blue, green, red order, with nothing extra at the start or end of a row.
     * Only then is it safe to read or copy the array directly.
     */
    public static boolean isPlainBgrImage(BufferedImage image) {
        if (image.getType() != BufferedImage.TYPE_3BYTE_BGR) {
            return false;
        }
        WritableRaster raster = image.getRaster();
        if (!(raster.getDataBuffer() instanceof DataBufferByte buffer) || buffer.getNumBanks() != 1) {
            return false;
        }
        if (!(raster.getSampleModel() instanceof PixelInterleavedSampleModel model)) {
            return false;
        }
        int width = image.getWidth();
        return raster.getSampleModelTranslateX() == 0
            && raster.getSampleModelTranslateY() == 0
            && model.getPixelStride() == 3
            && model.getScanlineStride() == width * 3
            && buffer.getOffset() == 0
            && buffer.getData().length == width * image.getHeight() * 3;
    }

    /**
     * The pixel bytes of a plain BGR image (the real array, not a copy).
     * Only call this when isPlainBgrImage(image) is true.
     */
    public static byte[] bytes(BufferedImage image) {
        return ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
    }

    /**
     * The color of pixel number `pixel` (0 = top left, then left to right, row by row)
     * as 0xRRGGBB, the same value getRGB(x, y) & 0xFFFFFF gives.
     * "& 0xFF" turns Java's signed byte (-128 to 127) into 0 to 255.
     */
    public static int rgbAt(byte[] bytes, int pixel) {
        int i = pixel * 3;
        int blue = bytes[i] & 0xFF;
        int green = bytes[i + 1] & 0xFF;
        int red = bytes[i + 2] & 0xFF;
        return (red << 16) | (green << 8) | blue;
    }
}
