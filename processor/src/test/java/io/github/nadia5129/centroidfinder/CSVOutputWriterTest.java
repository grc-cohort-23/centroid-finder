package io.github.nadia5129.centroidfinder;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class CSVOutputWriterTest {

    // One row with a found centroid -> "seconds,x,y" with 3 decimals
    @Test
    void writeRow_foundCentroid_writesSecondsXY() {
        // Arrange
        StringWriter output = new StringWriter();
        CSVOutputWriter writer = new CSVOutputWriter(output);

        // Act
        writer.writeRow(1.5, new Coordinate(320, 180));
        writer.close();

        // Assert
        assertEquals("1.500,320,180\n", output.toString());
    }

    // No centroid found -> the -1,-1 coordinate is written like any other
    @Test
    void writeRow_noCentroid_writesNegativeOnes() {
        // Arrange
        StringWriter output = new StringWriter();
        CSVOutputWriter writer = new CSVOutputWriter(output);

        // Act
        writer.writeRow(2.0, new Coordinate(-1, -1));
        writer.close();

        // Assert
        assertEquals("2.000,-1,-1\n", output.toString());
    }

    // First frame at 0 seconds
    @Test
    void writeRow_zeroSeconds_writesZero() {
        // Arrange
        StringWriter output = new StringWriter();
        CSVOutputWriter writer = new CSVOutputWriter(output);

        // Act
        writer.writeRow(0.0, new Coordinate(5, 7));
        writer.close();

        // Assert
        assertEquals("0.000,5,7\n", output.toString());
    }

    // Long decimals get rounded to 3 places (1/3 of a second -> 0.333)
    @Test
    void writeRow_longDecimal_roundsToThreePlaces() {
        // Arrange
        StringWriter output = new StringWriter();
        CSVOutputWriter writer = new CSVOutputWriter(output);

        // Act
        writer.writeRow(1.0 / 3.0, new Coordinate(10, 20));
        writer.close();

        // Assert
        assertEquals("0.333,10,20\n", output.toString());
    }

    // Floating point noise like 0.039999999 should still come out tidy
    @Test
    void writeRow_floatingPointNoise_roundsCleanly() {
        // Arrange
        StringWriter output = new StringWriter();
        CSVOutputWriter writer = new CSVOutputWriter(output);

        // Act
        writer.writeRow(0.039999999, new Coordinate(1, 1));
        writer.close();

        // Assert
        assertEquals("0.040,1,1\n", output.toString());
    }

    // Several rows -> one line each, in the order they were written
    @Test
    void writeRow_multipleRows_writesOneLineEach() {
        // Arrange
        StringWriter output = new StringWriter();
        CSVOutputWriter writer = new CSVOutputWriter(output);

        // Act
        writer.writeRow(0.0, new Coordinate(100, 50));
        writer.writeRow(0.04, new Coordinate(-1, -1));
        writer.writeRow(0.08, new Coordinate(102, 51));
        writer.close();

        // Assert
        assertEquals("0.000,100,50\n0.040,-1,-1\n0.080,102,51\n", output.toString());
    }

    // Nothing written -> empty output (no header row)
    @Test
    void close_noRows_writesNothing() {
        // Arrange
        StringWriter output = new StringWriter();
        CSVOutputWriter writer = new CSVOutputWriter(output);

        // Act
        writer.close();

        // Assert
        assertEquals("", output.toString());
    }

    // Using a real file path -> the file is created with the rows inside
    // @TempDir gives us a temporary folder that JUnit deletes after the test
    @Test
    void constructor_filePath_writesRowsToFile(@TempDir Path tempDir) throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("output.csv");
        CSVOutputWriter writer = new CSVOutputWriter(csvFile.toString());

        // Act
        writer.writeRow(1.0, new Coordinate(3, 4));
        writer.writeRow(2.0, new Coordinate(-1, -1));
        writer.close();

        // Assert
        assertEquals("1.000,3,4\n2.000,-1,-1\n", Files.readString(csvFile));
    }

    // A path to a folder that doesn't exist -> IOException
    @Test
    void constructor_badPath_throwsIOException(@TempDir Path tempDir) {
        String badPath = tempDir.resolve("no_such_folder/output.csv").toString();

        assertThrows(IOException.class, () -> new CSVOutputWriter(badPath));
    }
}
