package io.github.tonymythical.centroidFinder;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Serializes tracked centroid timestamp records to standard CSV files.
 */
public class CsvReportWriter {

    /**
     * Writes centroid records into a CSV file with the format "timestamp,x,y".
     *
     * @param outputPath the file system destination path
     * @param records the list of centroid records to serialize
     * @throws IOException if an error occurs while writing to the output path
     */
    public void writeRecords(String outputPath, List<CentroidTimestampRecord> records) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            for (CentroidTimestampRecord record : records) {
                writer.write(String.format("%.1f,%d,%d%n", record.timestampSeconds(), record.x(), record.y()));
            }
        }
    }
}
