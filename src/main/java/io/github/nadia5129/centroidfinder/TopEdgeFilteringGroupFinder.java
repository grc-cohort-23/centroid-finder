package io.github.nadia5129.centroidfinder;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * An ImageGroupFinder that ignores groups near the top edge of the image.
 *
 * In the salamander video there is a dark strip along the top edge that is almost
 * the same color as the salamander. Sometimes that strip is a bigger group than the
 * salamander, so the tracker would follow the strip instead. This class removes any
 * group whose centroid is in the top `topMargin` rows.
 *
 * It wraps another ImageGroupFinder (like BinarizingImageGroupFinder) and only filters
 * its results, so none of the existing classes had to change.
 */
public class TopEdgeFilteringGroupFinder implements ImageGroupFinder {

    private final ImageGroupFinder groupFinder;
    private final int topMargin;

    public TopEdgeFilteringGroupFinder(ImageGroupFinder groupFinder, int topMargin) {
        if (topMargin < 0) {
            throw new IllegalArgumentException("topMargin cannot be negative, but got: " + topMargin);
        }
        this.groupFinder = groupFinder;
        this.topMargin = topMargin;
    }

    @Override
    public List<Group> findConnectedGroups(BufferedImage image) {
        List<Group> groups = groupFinder.findConnectedGroups(image);

        //keep only the groups whose centroid is at or below the margin line
        //(the list is already sorted biggest first, and we keep that order)
        List<Group> kept = new ArrayList<>();
        for (Group group : groups) {
            if (group.centroid().y() >= topMargin) {
                kept.add(group);
            }
        }
        return kept;
    }
}
