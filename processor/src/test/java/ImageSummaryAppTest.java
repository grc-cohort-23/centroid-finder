import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link ImageSummaryApp#loadImage(String)}, which turns ImageIO's silent
 * "returns null" failure into a clear IOException.
 */
public class ImageSummaryAppTest {

    /**
     * Returns the absolute path of processor/sampleInput/squares.jpg, found from the test
     * classes directory (processor/target/test-classes) so it works from any working directory.
     */
    private static String sampleImagePath() throws URISyntaxException {
        Path testClassesDir = Path.of(ImageSummaryAppTest.class
                .getProtectionDomain().getCodeSource().getLocation().toURI());
        return testClassesDir.getParent().getParent().resolve("sampleInput").resolve("squares.jpg").toString();
    }

    // A real image file loads into a non-null BufferedImage
    @Test
    void loadImage_validImage_returnsImage() throws Exception {
        BufferedImage image = ImageSummaryApp.loadImage(sampleImagePath());

        assertNotNull(image);
        assertTrue(image.getWidth() > 0 && image.getHeight() > 0);
    }

    // A file that exists but isn't an image gives a clear IOException instead of a null image
    @Test
    void loadImage_notAnImage_throwsIOExceptionWithClearMessage(@TempDir Path tempDir) throws IOException {
        Path notAnImage = tempDir.resolve("fake.png");
        Files.writeString(notAnImage, "definitely not a png");

        IOException e = assertThrows(IOException.class, () -> ImageSummaryApp.loadImage(notAnImage.toString()));
        assertEquals("Unsupported or corrupt image file: " + notAnImage, e.getMessage());
    }

    // A path that doesn't exist throws an IOException
    @Test
    void loadImage_missingFile_throwsIOException(@TempDir Path tempDir) {
        String missing = tempDir.resolve("missing.png").toString();

        assertThrows(IOException.class, () -> ImageSummaryApp.loadImage(missing));
    }
}
