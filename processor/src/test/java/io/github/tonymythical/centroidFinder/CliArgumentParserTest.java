package io.github.tonymythical.centroidFinder;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CliArgumentParserTest {

    @Test
    void testParseProcessorArgsValid() {
        String[] args = {"video.mp4", "output.csv", "#FFA200", "30"};
        CliArgumentParser config = CliArgumentParser.parseProcessorArgs(args);

        assertEquals("video.mp4", config.inputPath());
        assertEquals("output.csv", config.outputPath());
        assertEquals(0xFFA200, config.targetColor());
        assertEquals(30, config.threshold());
        assertNull(config.originalImagePath());
    }

    @Test
    void testParseProcessorArgsWithoutHashPrefix() {
        String[] args = {"video.mp4", "output.csv", "00FF00", "15"};
        CliArgumentParser config = CliArgumentParser.parseProcessorArgs(args);

        assertEquals(0x00FF00, config.targetColor());
        assertEquals(15, config.threshold());
    }

    @Test
    void testParsePreviewArgsWithOriginalImage() {
        String[] args = {"video.mp4", "binary.png", "FF0000", "50", "original.png"};
        CliArgumentParser config = CliArgumentParser.parsePreviewArgs(args);

        assertEquals("video.mp4", config.inputPath());
        assertEquals("binary.png", config.outputPath());
        assertEquals(0xFF0000, config.targetColor());
        assertEquals(50, config.threshold());
        assertEquals("original.png", config.originalImagePath());
    }

    @Test
    void testParseProcessorArgsTooFewArgs() {
        String[] args = {"video.mp4", "output.csv"};
        assertThrows(IllegalArgumentException.class, () -> CliArgumentParser.parseProcessorArgs(args));
    }

    @Test
    void testParseInvalidThreshold() {
        String[] args = {"video.mp4", "output.csv", "FFA200", "not-a-number"};
        assertThrows(IllegalArgumentException.class, () -> CliArgumentParser.parseProcessorArgs(args));
    }

    @Test
    void testParseInvalidHexColor() {
        String[] args = {"video.mp4", "output.csv", "ZZZZZZ", "30"};
        assertThrows(IllegalArgumentException.class, () -> CliArgumentParser.parseProcessorArgs(args));
    }
}
