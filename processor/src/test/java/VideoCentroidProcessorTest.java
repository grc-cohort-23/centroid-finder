import java.awt.image.BufferedImage;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VideoCentroidProcessorTest {
    @Test
    void returnsCenterOfLargestGroup() {
        Group small = new Group(2, new Coordinate(0, 0));
        Group large = new Group(5, new Coordinate(3, 2));
        var processor = new VideoCentroidProcessor(image -> List.of(small, large));
        var image = new BufferedImage(6, 6, BufferedImage.TYPE_INT_RGB);
        var frame = new VideoFrameReader.VideoFrame(1.25, image);

        assertEquals(new VideoCentroidProcessor.Result(1.25, 3, 2),
                processor.process(frame));
    }

    @Test
    void returnsNegativeCoordinatesWhenNoGroupMatches() {
        var processor = new VideoCentroidProcessor(image -> List.of());
        var image = new BufferedImage(6, 6, BufferedImage.TYPE_INT_RGB);
        var frame = new VideoFrameReader.VideoFrame(2.5, image);

        assertEquals(new VideoCentroidProcessor.Result(2.5, -1, -1),
                processor.process(frame));
    }
}