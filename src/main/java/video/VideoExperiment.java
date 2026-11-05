package video;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

public class VideoExperiment {
    public static void main(String[] args) {
        boolean ranSuccessfully = false;
        String sampleVideoDir = "sampleInput/kitten.mp4";
        FFmpegFrameGrabber grabber = null;

        try {
            grabber = new FFmpegFrameGrabber(sampleVideoDir);
            grabber.start();

        // observe as Herbert becomes green!!!!!

        Frame frame = null;
        String outputPath = null;
        
            frame = grabber.grabImage();
            if (frame == null) throw new Exception("Frame is null.");
        
            
            Java2DFrameConverter frameConverter = new Java2DFrameConverter();
            BufferedImage image = frameConverter.convert(frame);

            // now for the greenification

            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int pix = image.getRGB(x, y);

                    // separates hex into channels
                    int r = (pix >> 16) & 0xFF;
                    int g = (pix >> 8) & 0xFF;
                    int b = pix & 0xFF;

                     // increase green channel, cap at max
                    g = Math.min(255, g + 50);

                    int newPix = (r << 16) | (g << 8) | b;
                    image.setRGB(x, y, newPix);
                }
            }

            ImageIO.write(image, "png", new File("greenKitten.png"));

            frameConverter.close();
            grabber.stop();
            ranSuccessfully = true;
        } catch (Exception e) {
            System.out.println("Failed to process frame. Aborting...");
            e.printStackTrace();
        }

        System.out.println(ranSuccessfully ? "Image processed successfully!" : "Image failed to process.");
    }
}
