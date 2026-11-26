import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * End-to-end tests for {@link VideoSummaryApp}: they run the whole pipeline (FFmpeg decode,
 * sampling, binarizing, group finding, CSV writing) on the real fixture video and check the
 * CSV file that comes out.
 *
 * The fixture {@code processor/sampleInput/ball.mp4} is a synthetic 2 second, 10 fps,
 * 160x120 video: a black background with a solid red (FF0000) 20x20 square at y = 50..69
 * whose left edge moves right 4 pixels per frame, starting at x = 20. Because the motion is
 * known exactly, so are the centroids. VideoSummaryApp samples every 0.5 seconds, so we expect
 * 4 rows, at t = 0.00, 0.50, 1.00 and 1.50, with the square's left edge at x = 20, 40, 60 and
 * 80. The centroid of a 20 pixel wide span starting at x0 is x0 + 9 (integer division), and
 * likewise y = 50 + 9 = 59.
 */
public class VideoSummaryAppEndToEndTest {

    /** The color of the moving square in the fixture video. */
    private static final String RED = "FF0000";

    /**
     * Wide enough to absorb the small color shifts from H.264 compression, but far narrower
     * than the distance from red to the black background (255), so only the square matches.
     */
    private static final String THRESHOLD = "100";

    /**
     * The frozen CSV row format: "timestamp,x,y", timestamp with exactly 2 decimals, and
     * integer coordinates (which may be -1 when nothing matched).
     */
    private static final Pattern CSV_ROW = Pattern.compile("^\\d+\\.\\d{2},-?\\d+,-?\\d+$");

    /**
     * Runs VideoSummaryApp on the fixture video and returns the lines of the CSV it wrote.
     *
     * @param tempDir a temporary directory to write the CSV into, so tests never leave files behind
     * @param hexColor the target color argument
     * @param threshold the threshold argument
     * @return every line of the output CSV
     * @throws IOException if the output CSV cannot be read (for example, it was never written)
     */
    private static List<String> runOnFixture(Path tempDir, String hexColor, String threshold) throws IOException {
        Path outputCsv = tempDir.resolve("output.csv");
        VideoSummaryApp.main(new String[] {
                FfmpegVideoFrameGrabberTest.fixtureVideoPath(), outputCsv.toString(), hexColor, threshold});
        assertTrue(Files.exists(outputCsv), "VideoSummaryApp should have written " + outputCsv);
        return Files.readAllLines(outputCsv);
    }

    // The fixture is 2 seconds long and sampled every 0.5 seconds, so it produces 4 rows
    @Test
    void fixtureVideo_writesOneRowPerSampledFrame(@TempDir Path tempDir) throws IOException {
        List<String> rows = runOnFixture(tempDir, RED, THRESHOLD);

        assertEquals(4, rows.size());
    }

    // Every row follows the frozen "timestamp,x,y" CSV format the server and frontend rely on
    @Test
    void fixtureVideo_everyRowMatchesCsvFormat(@TempDir Path tempDir) throws IOException {
        List<String> rows = runOnFixture(tempDir, RED, THRESHOLD);

        for (String row : rows) {
            assertTrue(CSV_ROW.matcher(row).matches(), "Row does not match timestamp,x,y format: " + row);
        }
    }

    // The red square's centroid is found at its known position in every sampled frame
    @Test
    void fixtureVideo_findsKnownCentroids(@TempDir Path tempDir) throws IOException {
        List<String> rows = runOnFixture(tempDir, RED, THRESHOLD);

        assertEquals(List.of(
                "0.00,29,59",
                "0.50,49,59",
                "1.00,69,59",
                "1.50,89,59"), rows);
    }

    // A color that appears nowhere in the video gives -1,-1 for every sampled frame
    @Test
    void fixtureVideo_colorNotInVideo_writesMinusOneRows(@TempDir Path tempDir) throws IOException {
        List<String> rows = runOnFixture(tempDir, "00FF00", "50");

        assertEquals(List.of(
                "0.00,-1,-1",
                "0.50,-1,-1",
                "1.00,-1,-1",
                "1.50,-1,-1"), rows);
    }
}
