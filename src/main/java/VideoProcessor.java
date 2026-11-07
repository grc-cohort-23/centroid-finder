import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

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

         // ask for the first frame
        Optional<TimedFrame> next = source.nextFrame();

        // keep going until the source gives back an empty Optional (end of video)
        while (next.isPresent()) {
            TrackingResult result = tracker.track(next.get());
            writer.write(result);
            frameCount++;

            next = source.nextFrame();
        }

        return frameCount;
    }
}