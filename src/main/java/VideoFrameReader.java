import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.api.PictureWithMetadata;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.scale.AWTUtil;

public class VideoFrameReader implements AutoCloseable {
    public record VideoFrame(double seconds, BufferedImage image) {}

    private final SeekableByteChannel channel;
    private final FrameGrab grabber;

    public VideoFrameReader(Path videoPath) throws IOException, JCodecException {
        channel = NIOUtils.readableChannel(videoPath.toFile());

        try {
            grabber = FrameGrab.createFrameGrab(channel);
        } catch (IOException | JCodecException | RuntimeException error) {
            try {
                channel.close();
            } catch (IOException closeError) {
                error.addSuppressed(closeError);
            }
            throw error;
        }
    }

    public VideoFrame readNext() throws IOException {
        PictureWithMetadata frame = grabber.getNativeFrameWithMetadata();
        if (frame == null) {
            return null;
        }

        BufferedImage image = AWTUtil.toBufferedImage(frame.getPicture());
        return new VideoFrame(frame.getTimestamp(), image);
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }
}