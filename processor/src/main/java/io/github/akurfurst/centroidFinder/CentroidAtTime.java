package io.github.akurfurst.centroidFinder;
/**
 * Represents the largest centroid at a particular timestamp in a video
 * Time is represented in microseconds.
 * 
 */
public record CentroidAtTime(Coordinate centroid, long time ) {

    /**
     * Returns a string representing this centroid in comma-separated values (CSV) format.
     * The format is "time,x,y" time is seconds since beginning of the video, x and y are the coordinates of the centroid
     * 
     * @return a CSV row string representing the time and centroid coordinates
     */
    public String toCsvRow() {
        return String.format("%.2f,%d,%d", this.timeSeconds(), this.centroid().x(), this.centroid().y());
    }

    /**
     * Returns the time in seconds.
     * 
     * @return time in second.
     */
    public double timeSeconds() {
        return (double) this.time() / 1_000_000L;
    }
}
