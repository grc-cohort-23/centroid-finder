import java.io.Closeable;
import java.io.IOException;

/**
 * Saves tracking results somewhere, one at a time.
 * Must be closed when finished, so everything is saved.
 */
public interface ResultWriter extends Closeable {

    /**
     * Saves one result.
     *
     * @param result the time and centroid for one frame
     * @throws IOException if the result can't be written
     */
    void write(TrackingResult result) throws IOException;
}