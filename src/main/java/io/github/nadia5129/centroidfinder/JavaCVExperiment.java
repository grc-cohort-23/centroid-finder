package io.github.nadia5129.centroidfinder;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;


public class JavaCVExperiment {

    public static void main(String [] args) throws Exception{

        //sample of MP4 video - 
        String videoPath = "sampleInput/ensantina.mp4";

        //The try statement automatically closes the grabber.
        // tool to read the video
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath)){
            
            //open video
            grabber.start();

            // Print video information
            //getImageWidth() and getImageHeight() retrieve its dimensions.
            System.out.println("Width: " + grabber.getImageWidth());
            System.out.println("Height: " + grabber.getImageHeight());
            //getFrameRate() gets the reported frame rate.
            System.out.println("Frame rate: " + grabber.getFrameRate());

            // Convert microseconds into seconds
            //Dividing by 1_000_000.0 converts the duration into seconds.
            double duration =
                grabber.getLengthInTime() / 1_000_000.0;

            System.out.println("Duration: " + duration + " seconds");

            // Extract frames
            //Java2DFrameConverter turns a JavaCV Frame into a BufferedImage,
            //which is what our binarizer already uses.
            Java2DFrameConverter converter = new Java2DFrameConverter();

            //grabImage() returns the next video frame, or null when the video ends.
            Frame frame;
            int frameCount = 0;
            double seconds = 0;

            while ((frame = grabber.grabImage()) != null) {
                BufferedImage image = converter.convert(frame);

                //getTimestamp() is also in microseconds.
                seconds = grabber.getTimestamp() / 1_000_000.0;

                //Only print once per second so the output isn't hundreds of lines.
                if (frameCount % (int) Math.round(grabber.getFrameRate()) == 0) {
                    System.out.println("Frame " + frameCount + " at " + seconds + " seconds: "
                        + image.getWidth() + "x" + image.getHeight());
                }

                frameCount++;
            }

            System.out.println("Total frames: " + frameCount);
            System.out.println("Last timestamp: " + seconds + " seconds");

        }
    }
    
}
