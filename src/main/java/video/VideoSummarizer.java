package video;

import java.util.List;
import java.util.ArrayList;

public class VideoSummarizer {
    private final LargestCentroidLocator locator;

    public VideoSummarizer(LargestCentroidLocator locator) {
        this.locator = locator;
    }

    public List<TimedGroup> process(FrameSource source) throws Exception {
        List<TimedGroup> results = new ArrayList<>();
        TimedFrame frame;
        while ((frame = source.nextFrame()) != null) {
            results.add(locator.locate(frame));
            System.out.println("Processed frame at " + frame.seconds() + " seconds.");
        }
        return results;
    }
}
