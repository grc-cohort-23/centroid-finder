package io.github.nadia5129.centroidfinder;

/**
 * Reads and checks the 4 command line arguments for the video processor:
 * inputPath outputCsv targetColor threshold
 *
 * Everything is checked in the constructor. If any argument is invalid,
 * an IllegalArgumentException is thrown with a message explaining what went wrong.
 */
public class ArgumentParser {

    private final String inputPath;
    private final String outputCsv;
    private final int targetColor;
    private final int threshold;

    public ArgumentParser(String[] args) {
        //must have exactly 4 arguments
        if (args == null || args.length != 4) {
            throw new IllegalArgumentException(
                "Usage: java -jar videoprocessor.jar <inputPath> <outputCsv> <targetColor> <threshold>");
        }

        this.inputPath = args[0];
        this.outputCsv = args[1];
        this.targetColor = parseColor(args[2]);
        this.threshold = parseThreshold(args[3]);
    }

    //Turns a hex string like "FF4500" into an int like 0xFF4500
    private int parseColor(String hex) {
        //exactly 6 characters, each 0-9 or A-F (upper or lowercase)
        if (!hex.matches("[0-9A-Fa-f]{6}")) {
            throw new IllegalArgumentException(
                "Target color must be 6 hex digits in RRGGBB format (example: FF4500), but got: " + hex);
        }
        //16 means read it as hexadecimal
        return Integer.parseInt(hex, 16);
    }

    //Turns a string like "100" into an int, and makes sure it is not negative
    private int parseThreshold(String text) {
        int value;
        try {
            value = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Threshold must be a whole number, but got: " + text);
        }

        //a color distance can never be negative, so a negative threshold makes no sense
        if (value < 0) {
            throw new IllegalArgumentException("Threshold cannot be negative, but got: " + text);
        }
        return value;
    }

    public String getInputPath() {
        return inputPath;
    }

    public String getOutputCsv() {
        return outputCsv;
    }

    public int getTargetColor() {
        return targetColor;
    }

    public int getThreshold() {
        return threshold;
    }
}
