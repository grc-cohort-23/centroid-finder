import java.awt.image.BufferedImage;
import java.util.List;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

public class VideoExperiment {
    public static void main(String[] args) throws Exception {
        int target = Integer.parseInt(args[1], 16);
        int threshold = Integer.parseInt(args[2]);
        ImageGroupFinder finder = new BinarizingImageGroupFinder(
            new DistanceImageBinarizer(new EuclideanColorDistance(), target, threshold),
            new DfsBinaryGroupFinder());

        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(args[0]);
             Java2DFrameConverter converter = new Java2DFrameConverter()) {
            grabber.start();
            System.out.println("FPS: " + grabber.getFrameRate());
            System.out.println("Duration (s): " + grabber.getLengthInTime() / 1_000_000.0);

            long durationUs = grabber.getLengthInTime();
            for (long t = 0; t < durationUs; t += 1_000_000) {
                grabber.setTimestamp(t);              // seek
                Frame frame = grabber.grabImage();
                if (frame == null) break;
                BufferedImage img = converter.convert(frame);
                List<Group> groups = finder.findConnectedGroups(img);
                String out = groups.isEmpty()
                    ? "-1,-1"
                    : groups.get(0).centroid().x() + "," + groups.get(0).centroid().y();
                System.out.println((t / 1_000_000) + "," + out);
            }
            grabber.stop();
        }
    }
}