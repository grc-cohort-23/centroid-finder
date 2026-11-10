package io.github.nadia5129.centroidfinder;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.image.BufferedImage;
import java.util.List;

public class ImageCentroidTrackerTest {

    @Test
    void returnsLargestGroupCentroid() {

        // Create two groups, largest first
        Group largeGroup = new Group(10, new Coordinate(5, 8));
        Group smallGroup = new Group(3, new Coordinate(2, 4));

        ImageGroupFinder fakeFinder = new ImageGroupFinder() {

        @Override
        public List<Group> findConnectedGroups(BufferedImage image) {
            return List.of(largeGroup, smallGroup);
        }
        } ;

        // Create tracker using our fake group finder
        ImageCentroidTracker tracker =
            new ImageCentroidTracker(fakeFinder);

        // Create a dummy image
        BufferedImage image =
            new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

        // Call the method we're testing
        Coordinate result = tracker.findCentroid(image);

        // Check the expected centroid
        assertEquals(new Coordinate(5, 8), result);
    }

   @Test
    void returnsNegativeCoordinatesWhenNoGroupsFound() {

        // Create a fake group finder
        ImageGroupFinder fakeFinder = new ImageGroupFinder() {

        @Override
        public List<Group> findConnectedGroups(BufferedImage image) {
            return List.of();
        }
        };

        // Create tracker using fake finder
        ImageCentroidTracker tracker =
            new ImageCentroidTracker(fakeFinder);

        // Create a dummy image
        BufferedImage image =
            new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

        // Call the method
        Coordinate result = tracker.findCentroid(image);

        // Check that no groups returns (-1, -1)
        assertEquals(new Coordinate(-1, -1), result);
}

   
}