import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

public class Experiment {
    public static void main(String[] args) throws Exception {
        String path = args[0];
        int target = Integer.parseInt(args[1], 16);
        int threshold = Integer.parseInt(args[2]);
        // Optional 4th argument: where to write the CSV (defaults to output.csv)
        String outPath = args.length > 3 ? args[3] : "output.csv";

        ImageBinarizer binarizer = new DistanceImageBinarizer(new EuclideanColorDistance(), target, threshold);
        ImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, new DfsBinaryGroupFinder());

        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(path);
             Java2DFrameConverter converter = new Java2DFrameConverter();
             PrintWriter out = new PrintWriter(outPath)) {

            grabber.start();
            System.out.println("FPS " + grabber.getFrameRate() + ", frames " + grabber.getLengthInVideoFrames());

            Frame frame;
            int i = 0;
            while ((frame = grabber.grabImage()) != null) {
                if (i % 15 == 0) { // every 15th frame
                    BufferedImage img = converter.convert(frame);
                    List<Group> groups = finder.findConnectedGroups(img);

                    // Seconds since the start of the video (timestamp is in microseconds)
                    double seconds = frame.timestamp / 1_000_000.0;

                    // Groups are sorted largest first, so get(0) is the largest one.
                    // If nothing matched the color, write -1,-1 as the assignment says.
                    int x = -1;
                    int y = -1;
                    if (!groups.isEmpty()) {
                        x = groups.get(0).centroid().x();
                        y = groups.get(0).centroid().y();
                    }

                    // Locale.US makes sure the decimal point is a "." on any computer
                    out.println(String.format(Locale.US, "%.2f,%d,%d", seconds, x, y));
                }
                i++;
            }
            grabber.stop();
        }
        System.out.println("Wrote " + outPath);
    }
}