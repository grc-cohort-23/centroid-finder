import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class VideoProcessorTest {

  @TempDir
  Path tempDir;

  private static class FakeFrameSource implements FrameSource {
    private final List<VideoFrame> frames;
    private int index = 0;

    FakeFrameSource(List<VideoFrame> frames) {
      this.frames = frames;
    }

    @Override
    public VideoFrame nextFrame() {
      if (index >= frames.size()) {
        return null;
      }

      return frames.get(index++);
    }

    @Override
    public void close() {
      // No video resources to release.
    }
  }

  @Test
  public void testProcessesFramesAndWritesCentroids() throws Exception {
    BufferedImage firstImage = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
    BufferedImage secondImage = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);

    ImageGroupFinder finder = image -> {
      if (image == firstImage) {
        return List.of(
            new Group(2, new Coordinate(0, 0)),
            new Group(5, new Coordinate(1, 1)));
      }

      if (image == secondImage) {
        return List.of();
      }

      throw new AssertionError("Unexpected image");
    };

    VideoProcessor processor = new VideoProcessor(
        finder, new LargestGroupSelector());

    Path output = tempDir.resolve("output.csv");

    try (
        FrameSource source = new FakeFrameSource(List.of(
            new VideoFrame(firstImage, 0.0),
            new VideoFrame(secondImage, 0.5)));
        CsvWriter writer = new CsvWriter(output.toString())) {
      processor.process(source, writer);
    }

    assertEquals(
        List.of("0.0,1,1", "0.5,-1,-1"),
        Files.readAllLines(output));
  }
}