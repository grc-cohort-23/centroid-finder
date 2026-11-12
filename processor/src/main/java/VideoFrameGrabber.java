/**
 * An interface for reading frames out of a video one at a time, in order.
 *
 * Implementations are responsible for decoding whatever underlying video format
 * is used and exposing each decoded frame as a FrameData, paired with the time
 * (in seconds) at which that frame occurs in the video.
 */
public interface VideoFrameGrabber {

    /**
     * Returns whether there is another frame available to read from the video.
     *
     * @return true if nextFrame() can be called again, false if the video is exhausted
     */
    public boolean hasNext();

    /**
     * Returns the next frame of the video along with its timestamp in seconds.
     *
     * @return a FrameData containing the next decoded frame and its timestamp
     */
    public FrameData nextFrame();
}
