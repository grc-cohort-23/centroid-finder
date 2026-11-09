public interface FrameSource extends AutoCloseable {

  VideoFrame nextFrame() throws Exception;

  @Override
  void close() throws Exception;
}