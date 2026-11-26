import java.util.List;

public class VideoProcessor {
  private final ImageGroupFinder finder;
  private final LargestGroupSelector selector;

  public VideoProcessor(
      ImageGroupFinder finder,
      LargestGroupSelector selector) {
    this.finder = finder;
    this.selector = selector;
  }

  public void process(FrameSource source, CsvWriter writer)
      throws Exception {
    VideoFrame frame = source.nextFrame();

    while (frame != null) {
      List<Group> groups = finder.findConnectedGroups(frame.image());

      Coordinate centroid = selector.select(groups);

      writer.writeRow(frame.seconds(), centroid);

      frame = source.nextFrame();
    }
  }
}