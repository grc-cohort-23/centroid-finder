package io.github.nadia5129.centroidfinder;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ArgumentParserTest {

    // Valid arguments -> each getter returns the right value
    @Test
    void constructor_validArgs_parsesAllFour() {
        // Arrange
        String[] args = {"video.mp4", "output.csv", "FF4500", "100"};

        // Act
        ArgumentParser parser = new ArgumentParser(args);

        // Assert
        assertEquals("video.mp4", parser.getInputPath());
        assertEquals("output.csv", parser.getOutputCsv());
        assertEquals(0xFF4500, parser.getTargetColor());
        assertEquals(100, parser.getThreshold());
    }

    // Lowercase hex letters should work the same as uppercase
    @Test
    void constructor_lowercaseHex_parsesColor() {
        // Arrange
        String[] args = {"video.mp4", "output.csv", "ff4500", "100"};

        // Act
        ArgumentParser parser = new ArgumentParser(args);

        // Assert
        assertEquals(0xFF4500, parser.getTargetColor());
    }

    // Black (000000) is a real color and should not be rejected
    @Test
    void constructor_blackColor_parsesToZero() {
        // Arrange
        String[] args = {"video.mp4", "output.csv", "000000", "50"};

        // Act
        ArgumentParser parser = new ArgumentParser(args);

        // Assert
        assertEquals(0x000000, parser.getTargetColor());
    }

    // Threshold of 0 is allowed
    @Test
    void constructor_zeroThreshold_parsesToZero() {
        // Arrange
        String[] args = {"video.mp4", "output.csv", "FF4500", "0"};

        // Act
        ArgumentParser parser = new ArgumentParser(args);

        // Assert
        assertEquals(0, parser.getThreshold());
    }

    // Too few arguments -> IllegalArgumentException
    @Test
    void constructor_tooFewArgs_throwsException() {
        String[] args = {"video.mp4", "output.csv", "FF4500"};

        assertThrows(IllegalArgumentException.class, () -> new ArgumentParser(args));
    }

    // Too many arguments -> IllegalArgumentException
    @Test
    void constructor_tooManyArgs_throwsException() {
        String[] args = {"video.mp4", "output.csv", "FF4500", "100", "extra"};

        assertThrows(IllegalArgumentException.class, () -> new ArgumentParser(args));
    }

    // No arguments at all -> IllegalArgumentException
    @Test
    void constructor_noArgs_throwsException() {
        String[] args = {};

        assertThrows(IllegalArgumentException.class, () -> new ArgumentParser(args));
    }

    // Null args -> IllegalArgumentException
    @Test
    void constructor_nullArgs_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new ArgumentParser(null));
    }

    // Threshold is a word, not a number -> IllegalArgumentException
    @Test
    void constructor_thresholdNotANumber_throwsException() {
        String[] args = {"video.mp4", "output.csv", "FF4500", "hello"};

        assertThrows(IllegalArgumentException.class, () -> new ArgumentParser(args));
    }

    // Threshold is a decimal -> IllegalArgumentException (must be a whole number)
    @Test
    void constructor_thresholdDecimal_throwsException() {
        String[] args = {"video.mp4", "output.csv", "FF4500", "10.5"};

        assertThrows(IllegalArgumentException.class, () -> new ArgumentParser(args));
    }

    // Negative threshold -> IllegalArgumentException (a distance can never be negative)
    @Test
    void constructor_negativeThreshold_throwsException() {
        String[] args = {"video.mp4", "output.csv", "FF4500", "-5"};

        assertThrows(IllegalArgumentException.class, () -> new ArgumentParser(args));
    }

    // Color has letters that are not hex (only 0-9 and A-F allowed) -> IllegalArgumentException
    @Test
    void constructor_colorNotHex_throwsException() {
        String[] args = {"video.mp4", "output.csv", "ZZZZZZ", "100"};

        assertThrows(IllegalArgumentException.class, () -> new ArgumentParser(args));
    }

    // Color is too short (must be exactly 6 hex digits, RRGGBB) -> IllegalArgumentException
    @Test
    void constructor_colorTooShort_throwsException() {
        String[] args = {"video.mp4", "output.csv", "FFF", "100"};

        assertThrows(IllegalArgumentException.class, () -> new ArgumentParser(args));
    }

    // Color is too long -> IllegalArgumentException
    @Test
    void constructor_colorTooLong_throwsException() {
        String[] args = {"video.mp4", "output.csv", "FF4500AA", "100"};

        assertThrows(IllegalArgumentException.class, () -> new ArgumentParser(args));
    }

    // Color with a minus sign is 6 characters but not a real color -> IllegalArgumentException
    @Test
    void constructor_colorWithMinusSign_throwsException() {
        String[] args = {"video.mp4", "output.csv", "-FF450", "100"};

        assertThrows(IllegalArgumentException.class, () -> new ArgumentParser(args));
    }

    // The error message should explain what went wrong
    @Test
    void constructor_badThreshold_messageMentionsThreshold() {
        String[] args = {"video.mp4", "output.csv", "FF4500", "hello"};

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
            () -> new ArgumentParser(args));

        assertTrue(e.getMessage().toLowerCase().contains("threshold"));
    }
}
