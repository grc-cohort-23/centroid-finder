import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;

/**
 * Hands out video frames one at a time in order.
 * CLose the video when it done.
 */

public interface FrameSource extends Closeable {
    // CLoseable basiclly mark the class as "something must close whenever you're done with it"
    // It give the class 1 method (close()) -> I'm done, put it away or clean it up now.

    /**
     * Returns the next frame, or an empty Optional when there are no more frames.
     *
     * @return the next frame with its timestamp, or Optional.empty() at the end
     * @throws IOException if the video can't be read
     */
    Optional<TimedFrame> nextFrame() throws IOException;
}