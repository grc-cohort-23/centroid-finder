package io.github.tonymythical.centroidFinder;

/**
 * An immutable record representing the centroid coordinates of a tracked group at a timestamp.
 *
 * @param timestampSeconds the elapsed time in seconds for the video frame
 * @param x the x-coordinate of the group centroid, or -1 if no centroid was found
 * @param y the y-coordinate of the group centroid, or -1 if no centroid was found
 */
public record CentroidTimestampRecord(double timestampSeconds, int x, int y) {}
