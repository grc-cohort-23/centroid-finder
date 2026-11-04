import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

public class VideoSummaryAppTest {

    // A valid RRGGBB hex string parses into the matching 24-bit integer
    @Test
    void parseTargetColor_validHex_returnsExpectedInt() {
        assertEquals(0xFF0000, VideoSummaryApp.parseTargetColor("FF0000"));
    }

    // A non-hex string is not a valid target color
    @Test
    void parseTargetColor_invalidHex_throwsNumberFormatException() {
        assertThrows(NumberFormatException.class, () -> VideoSummaryApp.parseTargetColor("ZZZZZZ"));
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

    // Running with the wrong number of arguments prints usage instead of crashing
    @Test
    void main_wrongArgumentCount_printsUsageAndReturns() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream capturedOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOut));
        try {
            VideoSummaryApp.main(new String[] {"onlyOneArg"});
        } finally {
            System.setOut(originalOut);
        }

        assertEquals(true, capturedOut.toString().startsWith("Usage:"));
    }
}
