import java.awt.image.BufferedImage;
import java.util.List;

public class VideoProcessor {

    private final VideoFrameExtractor extractor;
    private final ImageGroupFinder groupFinder;
    private final CsvWriter writer;

    public VideoProcessor(VideoFrameExtractor extractor,
                          ImageGroupFinder groupFinder,
                          CsvWriter writer) {
        this.extractor = extractor;
        this.groupFinder = groupFinder;
        this.writer = writer;
    }

    // Returns the largest centroid, or (-1, -1) if nothing is found
    public Coordinate findLargestCentroid(BufferedImage image) {
        List<Group> groups = groupFinder.findConnectedGroups(image);

        if (groups.isEmpty()) {
            return new Coordinate(-1, -1);
        }

        // Groups are sorted largest first
        return groups.get(0).centroid();
    }

    public void process() throws Exception {
        VideoFrame frame = extractor.nextFrame();

        while (frame != null) {
            Coordinate centroid = findLargestCentroid(frame.image());
            writer.writeRow(frame.seconds(), centroid);
            frame = extractor.nextFrame();
        }
    }
}