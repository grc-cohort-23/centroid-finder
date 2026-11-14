
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class VideoExperiment {

    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("Please provide an MP4 video path.");
            return;
        }

        String videoPath = args[0];

        try (FFmpegFrameGrabber grabber =
                new FFmpegFrameGrabber(videoPath);
             Java2DFrameConverter converter =
                new Java2DFrameConverter()) {

            grabber.start();

            // Get video information
            System.out.println("Video width: " + grabber.getImageWidth());
            System.out.println("Video height: " + grabber.getImageHeight());
            System.out.println("Frame rate: " + grabber.getFrameRate());
            System.out.println("Duration: " +
                    grabber.getLengthInTime() / 1000000.0 + " seconds");

            // Extract the first available video frame
            Frame frame = grabber.grabImage();

            if (frame != null) {
                BufferedImage image = converter.convert(frame);

                File output = new File("video-frame.png");
                ImageIO.write(image, "png", output);

                System.out.println("Frame saved as video-frame.png");
            } else {
                System.out.println("No video frame found.");
            }

        } catch (Exception e) {
            System.out.println("Error processing video: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
