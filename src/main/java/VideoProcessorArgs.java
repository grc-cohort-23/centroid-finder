/**
 * The four command-line arguments after they've been checked and converted.
 *
 * @param inputPath   path to the input video
 * @param outputCsv   path of the CSV file to write
 * @param targetColor target color as 0xRRGGBB
 * @param threshold   color distance threshold
 */
public record VideoProcessorArgs(String inputPath, String outputCsv, int targetColor, int threshold) {}
