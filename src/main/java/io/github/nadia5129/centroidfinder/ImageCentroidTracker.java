package io.github.nadia5129.centroidfinder;

import java.awt.image.BufferedImage;
import java.util.List;

public class ImageCentroidTracker {

    private final ImageGroupFinder groupFinder;

    public ImageCentroidTracker(ImageGroupFinder groupFinder) {
        this.groupFinder = groupFinder;
    }

    public Coordinate findCentroid(BufferedImage image) {

        List<Group> groups = groupFinder.findConnectedGroups(image);

        if (groups.isEmpty()) {
            return new Coordinate(-1, -1);
        }

        return groups.get(0).centroid();
    }
}