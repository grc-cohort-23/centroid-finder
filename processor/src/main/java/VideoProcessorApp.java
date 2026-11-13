import java.util.ArrayList;
import java.util.List;

/**
 * The main entry point for the video centroid processing application.
 *
 * Parses command-line arguments, configures the image-processing
 * dependencies, and coordinates video processing and CSV output.
 *
 * Usage:
 * java -jar videoprocessor.jar <input> <outputCsv> <hexColor> <threshold>
 */
public class VideoProcessorApp {
    
    /**
     * Starts the application using the supplied command-line arguments.
     *
     * @param args the input video path, output CSV path,
     *             target RGB hex color, and color-distance threshold
     */
    public static void main(String[] args) {
     
        if (args.length != 4) {
            System.err.println( "Usage: java -jar videoprocessor.jar " + "<input> <outputCsv> <hexColor> <threshold>" );
            return;
        }

        String videoPath = args[0];
        String outputPath = args[1];
        String hexColor = args[2];
        String thresholdText = args[3];

        // Parse the target color from a hex string (format RRGGBB) into a 24-bit integer (0xRRGGBB)
        String hexTargetColor = hexColor;
        int targetColor = 0;
        try {
            targetColor = Integer.parseInt(hexTargetColor, 16);
        } catch (NumberFormatException e) {
            System.err.println("Invalid hex target color. Please provide a color in RRGGBB format.");
            return;
        }

        // Parse the threshold from the command-line argument.
        int threshold = Integer.parseInt(thresholdText);
        
        // Create the DistanceImageBinarizer with a EuclideanColorDistance instance.
        ColorDistanceFinder distanceFinder = new EuclideanColorDistance();
        ImageBinarizer binarizer = new DistanceImageBinarizer(distanceFinder, targetColor, threshold);
        ImageGroupFinder groupFinder = new BinarizingImageGroupFinder(binarizer, new DfsBinaryGroupFinder());
        


        List<String> toCSV = new ArrayList<>();
        VideoCsvWriter writer = new VideoCsvWriter(outputPath);

        try{
            VideoProcessor vp = new VideoProcessor(groupFinder);
            toCSV = vp.process(videoPath);
            writer.writeGroups(toCSV);

        }catch(Exception e){
            System.err.println("Error processing video: " + e.getMessage());
        }
    }
}