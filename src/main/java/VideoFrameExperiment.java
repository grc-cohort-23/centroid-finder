import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.jcodec.api.JCodecException;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.PictureWithMetadata;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.containers.mp4.MP4Util;
import org.jcodec.containers.mp4.MP4Util.Movie;
import org.jcodec.scale.AWTUtil;

public class VideoFrameExperiment {
    public static void main(String[] args) throws IOException, JCodecException {
        File videoFile = new File("sampleInput/sample.mp4");
        Movie movie = MP4Util.parseFullMovie(videoFile);
        double duration = (double) movie.getMoov().getDuration() /
                movie.getMoov().getTimescale();
        System.out.println("Video duration: " + duration + " seconds");

        Path outputFolder = Files.createDirectories(Path.of("target", "video-frames"));
        int lastSavedSecond = -1;

        try (SeekableByteChannel channel = NIOUtils.readableChannel(videoFile)) {
            FrameGrab grabber = FrameGrab.createFrameGrab(channel);
            PictureWithMetadata frame;

            while ((frame = grabber.getNativeFrameWithMetadata()) != null) {
                double timestamp = frame.getTimestamp();
                int second = (int) timestamp;

                if (second > lastSavedSecond) {
                    Path outputFile = outputFolder.resolve("frame-" + second + ".png");
                    ImageIO.write(AWTUtil.toBufferedImage(frame.getPicture()),
                            "png", outputFile.toFile());

                    System.out.printf("Saved %s at %.3f seconds%n",
                            outputFile.getFileName(), timestamp);
                    lastSavedSecond = second;
                }
            }
        }

    }
}