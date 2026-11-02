import java.io.IOException;
import java.util.Objects;

/**
 * The main loop: take each frame from a FrameSource, track it, write the result.
 * It only depends on interfaces (FrameSource, ResultWriter), so tests can give it
 * fake frames instead of a real video.
 */
public class VideoProcessor {
    private final CentroidTracker tracker;

    public VideoProcessor(CentroidTracker tracker) {
        this.tracker = Objects.requireNonNull(tracker, "tracker must not be null");
    }

    /**
     * Processes every frame from the source.
     *
     * @return how many frames were processed
     */
    public int process(FrameSource source, ResultWriter writer) throws IOException {
        int frameCount = 0;

        // TODO KC: Loop until source.nextFrame() returns an empty Optional.
        //         For each frame: track it, write the result, add 1 to frameCount.
        //   while (next.isPresent()) { ... next = source.nextFrame(); }
        // Checked by: VideoProcessorTest
        if (true) throw new UnsupportedOperationException("TODO 8"); // delete this line when done

return ? 
    }
}