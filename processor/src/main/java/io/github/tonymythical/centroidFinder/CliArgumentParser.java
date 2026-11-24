package io.github.tonymythical.centroidFinder;

/**
 * Handles validation and parsing of command-line arguments for video processor applications.
 */
public record CliArgumentParser(
    String inputPath,
    String outputPath,
    int targetColor,
    int threshold,
    String originalImagePath
) {
    /**
     * Parses command-line arguments for the full VideoProcessorApp CLI.
     *
     * @param args the command-line arguments array
     * @return the parsed configuration record
     * @throws IllegalArgumentException if argument count or formats are invalid
     */
    public static CliArgumentParser parseProcessorArgs(String[] args) {
        if (args == null || args.length < 4) {
            throw new IllegalArgumentException("Usage: java -jar videoprocessor.jar <inputPath> <outputCsv> <targetColorHex> <threshold>");
        }
        return parse(args[0], args[1], args[2], args[3], null);
    }

    /**
     * Parses command-line arguments for the VideoPreviewApp CLI.
     *
     * @param args the command-line arguments array
     * @return the parsed configuration record
     * @throws IllegalArgumentException if argument count or formats are invalid
     */
    public static CliArgumentParser parsePreviewArgs(String[] args) {
        if (args == null || args.length < 4) {
            throw new IllegalArgumentException("Usage: VideoPreviewApp <inputVideo> <outputImage> <targetColor> <threshold> [originalImage]");
        }
        String originalPath = args.length >= 5 ? args[4] : null;
        return parse(args[0], args[1], args[2], args[3], originalPath);
    }

    private static CliArgumentParser parse(String input, String output, String hexColor, String thresholdStr, String originalPath) {
        int threshold;
        try {
            threshold = Integer.parseInt(thresholdStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Threshold must be an integer.");
        }

        int targetColor;
        try {
            targetColor = Integer.parseInt(hexColor.replace("#", ""), 16);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex target color. Please provide a color in RRGGBB format.");
        }

        return new CliArgumentParser(input, output, targetColor, threshold, originalPath);
    }
}
