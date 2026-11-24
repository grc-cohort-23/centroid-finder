package io.github.nadia5129.centroidfinder;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Finds where the tracked object is in one image (one video frame).
 *
 * The tracked object is the largest group of matching pixels. The tracker asks an
 * {@link ImageGroupFinder} for every group in the image and returns the centroid of the
 * largest one. Coordinates start at (x:0, y:0) in the top-left corner; x increases to the
 * right and y increases downward.
 */
public class ImageCentroidTracker {

    private final ImageGroupFinder groupFinder;

    /**
     * Creates a tracker that uses the given group finder.
     *
     * @param groupFinder finds the groups of matching pixels in an image. It must return
     *                    the groups sorted in descending order (largest first), as
     *                    {@link ImageGroupFinder#findConnectedGroups(BufferedImage)} promises,
     *                    because this tracker always picks the first group.
     */
    public ImageCentroidTracker(ImageGroupFinder groupFinder) {
        this.groupFinder = groupFinder;
    }

    /**
     * Finds the centroid of the largest group of matching pixels in the image.
     *
     * The first group in the list is the largest only because the group finder sorts the
     * groups in descending order using {@link Group#compareTo(Group)}: by size, then by
     * centroid x, then by centroid y. So if two groups have the same size, the one with the
     * larger centroid x wins (and if x is also the same, the one with the larger y).
     *
     * @param image the image (video frame) to search
     * @return the centroid of the largest group, or (-1, -1) if no pixels match.
     *         (-1, -1) is never a real pixel position, so it means "not found" and is
     *         written to the CSV as-is for that frame.
     */
    public Coordinate findCentroid(BufferedImage image) {

        List<Group> groups = groupFinder.findConnectedGroups(image);

        if (groups.isEmpty()) {
            return new Coordinate(-1, -1);
        }

        return groups.get(0).centroid();
    }
}