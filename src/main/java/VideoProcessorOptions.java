
public class VideoProcessorOptions {

    private final String inputPath;
    private final String outputPath;
    private final int targetColor;
    private final int threshold;

    public VideoProcessorOptions(String[] args) {
        if (args.length != 4) {
            throw new IllegalArgumentException(
                "Usage: inputPath outputCsv targetColor threshold"
            );
        }

        inputPath = args[0];
        outputPath = args[1];

        if (!args[2].matches("[0-9a-fA-F]{6}")) {
            throw new IllegalArgumentException(
                "Target color must be 6 hex digits."
            );
        }

        targetColor = Integer.parseInt(args[2], 16);

        try {
            threshold = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "Threshold must be an integer."
            );
        }

        if (threshold < 0) {
            throw new IllegalArgumentException(
                "Threshold cannot be negative."
            );
        }
    }

    public String getInputPath() {
        return inputPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public int getTargetColor() {
        return targetColor;
    }

    public int getThreshold() {
        return threshold;
    }
}
