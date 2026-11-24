package video;

public interface FrameSource extends AutoCloseable {
    TimedFrame nextFrame() throws Exception; // null when the video ends
}
