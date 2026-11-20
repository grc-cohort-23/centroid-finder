
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class VideoProcessorTest {

    // This test is commented out because it requires a specific video file to be present.
    // @Test
    // void testExistingVideo() throws Exception {
    //     Path video = Path.of("videos\\FlowField_Example.mp4");

    //     assertTrue(Files.exists(video), "Test video must exist");

    //     ImageGroupFinder finder = image -> List.of();
    //     VideoProcessor processor = new VideoProcessor(finder);

    //     List<String> results = processor.process(video.toString());

    //     assertFalse(results.isEmpty());
    // }
}
