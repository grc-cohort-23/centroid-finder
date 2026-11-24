package io.github.tonymythical.centroidFinder;

import org.jcodec.api.FrameGrab;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import java.awt.image.BufferedImage;
import java.io.File;

public class VideoExperiment {
    public static void main(String[] args) {
        System.out.println("=== Starting JCodec Video Experiment ===");

        // Point this to a sample video file in your project
        File videoFile = new File("sampleInput/test.mp4");

        if (!videoFile.exists()) {
            System.out.println("⚠️ Warning: No video file found at " + videoFile.getPath());
            System.out.println("Please place a small .mp4 file in your sampleInput folder to test frame extraction.");
            return;
        }

        try {
            // Test 1: Extracting a frame at 1.0 seconds
            double targetSecond = 1.0;
            System.out.println("Attempting to grab frame at " + targetSecond + " second(s)...");
            
            Picture picture = FrameGrab.getFrameAtSec(videoFile, targetSecond);
            
            if (picture != null) {
                // Convert JCodec Picture to Java BufferedImage
                BufferedImage bufferedImage = AWTUtil.toBufferedImage(picture);
                
                System.out.println("Successfully extracted frame");
                System.out.println("   - Frame Width: " + bufferedImage.getWidth());
                System.out.println("   - Frame Height: " + bufferedImage.getHeight());
                System.out.println("   - Color Type: " + bufferedImage.getType());
                
            } else {
                System.out.println("Could not retrieve frame at specified timestamp");
            }

        } catch (Exception e) {
            System.err.println("Error processing video file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}