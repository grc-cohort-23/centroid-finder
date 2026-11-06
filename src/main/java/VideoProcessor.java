import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;


/**
 * Processes video files by extracting individual frames and
 * identifying the largest connected group of matching pixels.
 *
 * Uses an ImageGroupFinder to analyze each decoded frame.
 * Results contain the frame timestamp and the centroid
 * coordinates of the largest detected group.
 */
public class VideoProcessor {
    
    ImageGroupFinder groupFinder;

    /**
     * Constructs a VideoProcessor using the provided image group finder.
     *
     * @param groupFinder the ImageGroupFinder used to identify
     *                    connected pixel groups in each frame
     */
    public VideoProcessor(ImageGroupFinder groupFinder){
        this.groupFinder = groupFinder;
    }


    /**
     * Reads a video file and processes each frame to find its
     * largest connected pixel group.
     *
     * Each result is formatted as "seconds,x,y".
     * If no connected groups are detected, the coordinates
     * are recorded as (-1,-1).
     *
     * @param videoPath the path to the input video file
     * @return a list of CSV-formatted strings containing frame
     *         timestamps and centroid coordinates
     * @throws Exception if the video cannot be opened or processed
     */
    public List<String> process(String url) throws Exception {
        List<String> toCSV = new ArrayList<>();

        //create a frame grabber to isolate frames as images
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(url)) {

            // converter is used to conver the grabber into a BufferedImage
            Java2DFrameConverter converter = new Java2DFrameConverter();
            grabber.start();

            Frame frame;
            int count = 0;

            while ((frame = grabber.grabImage()) != null) {
                count++;

                BufferedImage image = converter.convert(frame);
                List<Group> groups = groupFinder.findConnectedGroups(image);
                String frameString = "Connected groups found: " + groups.size() + " | Processing frame: " + count ;
                double seconds = grabber.getTimestamp() / 1_000_000.0;
                if (!groups.isEmpty()) {
                    Group largest = Collections.max(groups);
                    toCSV.add(String.valueOf(seconds) + "," + largest.centroid().x() + "," + largest.centroid().y() );

                    frameString += " | Largest group: " + largest + " at " + seconds + " seconds";
                } else{
                    toCSV.add(String.valueOf(seconds) + ",-1,-1" );
                }

                System.out.println(frameString);
            }

            System.out.println("Total frames processed: " + count);
            grabber.stop();

        } catch (Exception e) {
            System.err.println("Could not read the video: " + e.getMessage());
            e.printStackTrace();
        }
        return toCSV;
    }
}
