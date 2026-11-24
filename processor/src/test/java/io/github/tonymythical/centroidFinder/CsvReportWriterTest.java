package io.github.tonymythical.centroidFinder;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class CsvReportWriterTest {

    @Test
    void testWriteRecordsWritesCorrectCsvFormat(@TempDir File tempDir) throws IOException {
        File csvFile = new File(tempDir, "output.csv");
        CsvReportWriter writer = new CsvReportWriter();

        List<CentroidTimestampRecord> records = List.of(
            new CentroidTimestampRecord(0.0, 15, 22),
            new CentroidTimestampRecord(1.0, -1, -1),
            new CentroidTimestampRecord(2.5, 100, 200)
        );

        writer.writeRecords(csvFile.getAbsolutePath(), records);

        assertTrue(csvFile.exists());
        List<String> lines = Files.readAllLines(csvFile.toPath());
        assertEquals(3, lines.size());
        assertEquals("0.0,15,22", lines.get(0));
        assertEquals("1.0,-1,-1", lines.get(1));
        assertEquals("2.5,100,200", lines.get(2));
    }

    @Test
    void testWriteRecordsEmptyList(@TempDir File tempDir) throws IOException {
        File csvFile = new File(tempDir, "empty.csv");
        CsvReportWriter writer = new CsvReportWriter();

        writer.writeRecords(csvFile.getAbsolutePath(), List.of());

        assertTrue(csvFile.exists());
        List<String> lines = Files.readAllLines(csvFile.toPath());
        assertTrue(lines.isEmpty());
    }
}
