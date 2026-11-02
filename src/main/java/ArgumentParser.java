import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Turns the raw String[] from main into a checked VideoProcessorArgs
 * Any bad input causes an IllegalArgumentException
 */
public class ArgumentParser {

    public static final String USAGE =
        "Usage: java -jar videoprocessor.jar <inputPath> <outputCsv> <targetColor RRGGBB> <threshold>";

    public VideoProcessorArgs parse(String[] args) {
        if (args == null || args.length != 4) {
            throw new IllegalArgumentException(USAGE);
        }

        String inputPath = args[0];
        String outputCsv = args[1];
        int targetColor = parseColor(args[2]);
        int threshold = parseThreshold(args[3]);

        if (!Files.isRegularFile(Path.of(inputPath))) {
            throw new IllegalArgumentException("Input video not found: " + inputPath);
        }

    //     return new VideoProcessorArgs(inputPath, outputCsv, targetColor, threshold);
    // }
    }

    private int parseColor(String text) {
        String hex = text.startsWith("#") ? text.substring(1) : text;

        if (!hex.matches("[0-9A-Fa-f]{6}")) {
            throw new IllegalArgumentException("Invalid target color: " + text);
        }
        return Integer.parseInt(hex, 16); 
    }


    }
