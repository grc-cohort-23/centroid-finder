package io.github.tonymythical.centroidFinder;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvReportWriter {
    public void writeRecords(String outputPath, List<CentroidTimestampRecord> records) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            for (CentroidTimestampRecord record : records) {
                writer.write(String.format("%.1f,%d,%d%n", record.timestampSeconds(), record.x(), record.y()));
            }
        }
    }
}