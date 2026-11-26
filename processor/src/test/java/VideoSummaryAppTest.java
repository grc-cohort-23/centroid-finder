import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link VideoSummaryApp}: argument parsing, and the exit code returned by
 * {@link VideoSummaryApp#run(String[])} for each kind of failure.
 *
 * These tests call run() rather than main(), because main() calls System.exit on failure,
 * which would stop the whole JUnit run.
 */
public class VideoSummaryAppTest {

    /**
     * Runs VideoSummaryApp.run with stderr captured, so the tests can check the error
     * message without cluttering the build output.
     */
    private record RunResult(int exitCode, String stderr) {}

    private static RunResult runCapturingStderr(String... args) {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream capturedErr = new ByteArrayOutputStream();
        System.setErr(new PrintStream(capturedErr));
        try {
            int exitCode = VideoSummaryApp.run(args);
            return new RunResult(exitCode, capturedErr.toString());
        } finally {
            System.setErr(originalErr);
        }
    }

    // A valid RRGGBB hex string parses into the matching 24-bit integer
    @Test
    void parseTargetColor_validHex_returnsExpectedInt() {
        assertEquals(0xFF0000, VideoSummaryApp.parseTargetColor("FF0000"));
    }

    // Lowercase hex digits are accepted too
    @Test
    void parseTargetColor_lowercaseHex_returnsExpectedInt() {
        assertEquals(0x00ff7f, VideoSummaryApp.parseTargetColor("00ff7f"));
    }

    // A non-hex string is not a valid target color
    @Test
    void parseTargetColor_invalidHex_throwsNumberFormatException() {
        assertThrows(NumberFormatException.class, () -> VideoSummaryApp.parseTargetColor("ZZZZZZ"));
    }

    // Strings that Integer.parseInt would accept but that aren't RRGGBB colors are rejected
    @Test
    void parseTargetColor_wrongLengthOrSign_throwsNumberFormatException() {
        assertThrows(NumberFormatException.class, () -> VideoSummaryApp.parseTargetColor("FFF"));
        assertThrows(NumberFormatException.class, () -> VideoSummaryApp.parseTargetColor("FF00000"));
        assertThrows(NumberFormatException.class, () -> VideoSummaryApp.parseTargetColor("-1"));
        assertThrows(NumberFormatException.class, () -> VideoSummaryApp.parseTargetColor("+FFFFF"));
    }

    // A valid integer string parses into the matching threshold
    @Test
    void parseThreshold_validInteger_returnsExpectedInt() {
        assertEquals(164, VideoSummaryApp.parseThreshold("164"));
    }

    // A non-integer string is not a valid threshold
    @Test
    void parseThreshold_nonInteger_throwsNumberFormatException() {
        assertThrows(NumberFormatException.class, () -> VideoSummaryApp.parseThreshold("not-a-number"));
    }

    // Running with the wrong number of arguments prints usage to stderr and returns the usage code
    @Test
    void run_wrongArgumentCount_printsUsageAndReturnsUsageCode() {
        RunResult result = runCapturingStderr("onlyOneArg");

        assertEquals(VideoSummaryApp.EXIT_USAGE, result.exitCode());
        assertTrue(result.stderr().startsWith("Usage:"));
    }

    // No arguments at all is also a usage error
    @Test
    void run_noArguments_returnsUsageCode() {
        assertEquals(VideoSummaryApp.EXIT_USAGE, runCapturingStderr().exitCode());
    }

    // A threshold that isn't an integer returns the bad-threshold code
    @Test
    void run_badThreshold_returnsBadThresholdCode(@TempDir Path tempDir) {
        RunResult result = runCapturingStderr(
                FfmpegVideoFrameGrabberTest.fixtureVideoPath(), tempDir.resolve("out.csv").toString(), "FF0000", "abc");

        assertEquals(VideoSummaryApp.EXIT_BAD_THRESHOLD, result.exitCode());
        assertTrue(result.stderr().contains("Threshold"));
    }

    // A color that isn't RRGGBB returns the bad-color code and writes no CSV
    @Test
    void run_badColor_returnsBadColorCode(@TempDir Path tempDir) {
        Path outputCsv = tempDir.resolve("out.csv");
        RunResult result = runCapturingStderr(
                FfmpegVideoFrameGrabberTest.fixtureVideoPath(), outputCsv.toString(), "ZZZZZZ", "100");

        assertEquals(VideoSummaryApp.EXIT_BAD_COLOR, result.exitCode());
        assertTrue(result.stderr().contains("RRGGBB"));
        assertFalse(Files.exists(outputCsv));
    }

    // A video path that doesn't exist returns the video-open code and writes no CSV
    @Test
    void run_missingVideo_returnsVideoOpenFailedCode(@TempDir Path tempDir) {
        Path outputCsv = tempDir.resolve("out.csv");
        RunResult result = runCapturingStderr(
                tempDir.resolve("does-not-exist.mp4").toString(), outputCsv.toString(), "FF0000", "100");

        assertEquals(VideoSummaryApp.EXIT_VIDEO_OPEN_FAILED, result.exitCode());
        assertTrue(result.stderr().contains("Error opening video"));
        assertFalse(Files.exists(outputCsv));
    }

    // A file that exists but isn't a video also returns the video-open code
    @Test
    void run_fileIsNotAVideo_returnsVideoOpenFailedCode(@TempDir Path tempDir) throws IOException {
        Path notAVideo = tempDir.resolve("notes.mp4");
        Files.writeString(notAVideo, "this is plain text, not an mp4");

        RunResult result = runCapturingStderr(
                notAVideo.toString(), tempDir.resolve("out.csv").toString(), "FF0000", "100");

        assertEquals(VideoSummaryApp.EXIT_VIDEO_OPEN_FAILED, result.exitCode());
    }

    // An output path inside a folder that doesn't exist returns the CSV-write code
    @Test
    void run_outputFolderMissing_returnsCsvWriteFailedCode(@TempDir Path tempDir) {
        Path outputCsv = tempDir.resolve("no-such-folder").resolve("out.csv");
        RunResult result = runCapturingStderr(
                FfmpegVideoFrameGrabberTest.fixtureVideoPath(), outputCsv.toString(), "FF0000", "100");

        assertEquals(VideoSummaryApp.EXIT_CSV_WRITE_FAILED, result.exitCode());
        assertTrue(result.stderr().contains("Error writing output CSV"));
    }

    // The fixture video with valid arguments succeeds with exit code 0 and writes the CSV
    @Test
    void run_fixtureVideo_returnsOkAndWritesCsv(@TempDir Path tempDir) {
        Path outputCsv = tempDir.resolve("out.csv");
        RunResult result = runCapturingStderr(
                FfmpegVideoFrameGrabberTest.fixtureVideoPath(), outputCsv.toString(), "FF0000", "100");

        assertEquals(VideoSummaryApp.EXIT_OK, result.exitCode());
        assertTrue(Files.exists(outputCsv));
    }

    // Every failure kind has its own distinct, non-zero exit code
    @Test
    void exitCodes_areDistinctAndNonZeroForFailures() {
        int[] failureCodes = {
                VideoSummaryApp.EXIT_USAGE,
                VideoSummaryApp.EXIT_BAD_THRESHOLD,
                VideoSummaryApp.EXIT_BAD_COLOR,
                VideoSummaryApp.EXIT_VIDEO_OPEN_FAILED,
                VideoSummaryApp.EXIT_VIDEO_READ_FAILED,
                VideoSummaryApp.EXIT_CSV_WRITE_FAILED};

        assertEquals(0, VideoSummaryApp.EXIT_OK);
        for (int i = 0; i < failureCodes.length; i++) {
            assertTrue(failureCodes[i] != 0);
            for (int j = i + 1; j < failureCodes.length; j++) {
                assertTrue(failureCodes[i] != failureCodes[j]);
            }
        }
    }
}
