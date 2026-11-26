package io.github.akurfurst.centroidFinder;

import static org.junit.jupiter.api.Assertions.*;

import javax.imageio.ImageIO;
import java.io.File;
import java.awt.image.BufferedImage;
import java.util.List;

import org.junit.jupiter.api.Test;

class BinarizingImageGroupFinderTest {
    private static final int TARGET_COLOR = 0xFFA200;

    private final List<Group> groupsList = List.of(
        new Group(6987, new Coordinate(533, 510)),
        new Group(5564, new Coordinate(185, 453)),
        new Group(5187, new Coordinate(586, 195)),
        new Group(5115, new Coordinate(371, 244)),
        new Group(7, new Coordinate(617, 229)),
        new Group(7, new Coordinate(377, 280)),
        new Group(6, new Coordinate(592, 233)),
        new Group(5, new Coordinate(611, 231)),
        new Group(5, new Coordinate(573, 235)),
        new Group(5, new Coordinate(373, 286)),
        new Group(4, new Coordinate(599, 232)),
        new Group(4, new Coordinate(582, 234)),
        new Group(2, new Coordinate(564, 236)),
        new Group(2, new Coordinate(507, 552)),
        new Group(2, new Coordinate(501, 556)),
        new Group(2, new Coordinate(497, 558)),
        new Group(2, new Coordinate(196, 487)),
        new Group(1, new Coordinate(551, 550)),
        new Group(1, new Coordinate(546, 551)),
        new Group(1, new Coordinate(371, 281)),
        new Group(1, new Coordinate(368, 282)),
        new Group(1, new Coordinate(228, 470)),
        new Group(1, new Coordinate(167, 494)),
        new Group(1, new Coordinate(152, 498))
    );
    
    // javac -cp lib/junit-platform-console-standalone-6.0.3.jar src/*.java && java
    // -cp src ImageSummaryApp sampleInput/squares.jpg FFA200 164
    @Test
    void TestResultMatchesSample() {
        ColorDistanceFinder distanceFinder = new EuclideanColorDistance();
        ImageBinarizer binarizer = new DistanceImageBinarizer(distanceFinder, 0xFFA200, 164);
        BinaryGroupFinder groupFinder = new DfsBinaryGroupFinder();
        ImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);
        
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File("sampleInput/squares.jpg"));
        } catch (Exception error) {
            System.out.println(error);
            System.exit(-1);
        }
        
        List<Group> actual = finder.findConnectedGroups(image);
        
        List<Group> expected = groupsList;
        
        assertEquals(expected, actual);
    }
    

    // This test confirms that the binarizer and groupFinder are properly used by creating explicit helper implementations
    // Prevents replacing the image and/or binaryArray
    @Test
    void passesTheImageToTheBinarizerAndItsOutputToTheGroupFinder() {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        int[][] binaryImage = { { 1, 0 } };
        List<Group> expected = List.of(new Group(2, new Coordinate(0, 0)));
        int[] binarizerCalls = { 0 };
        int[] groupFinderCalls = { 0 };

        // An ImageBinarizer that will always return the same binaryImage.
        ImageBinarizer binarizer = new ImageBinarizer() {
            @Override
            public int[][] toBinaryArray(BufferedImage actualImage) {
                binarizerCalls[0]++;
                // Assert the image passed to the finder is the same
                assertSame(image, actualImage); 
                return binaryImage;
            }

            @Override
            public BufferedImage toBufferedImage(int[][] ignored) {
                throw new UnsupportedOperationException("Not used by this test");
            }
        };

        // A GroupFinder that will always return the expected.
        BinaryGroupFinder groupFinder = actualBinaryImage -> {
            groupFinderCalls[0]++;
            assertSame(binaryImage, actualBinaryImage);
            return expected;
        };
        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        // Ensures ImageBinarizer & GroupFinder were only called once, 
        // and that the BinarizingImageGroupFinder returned the expected value
        assertSame(expected, finder.findConnectedGroups(image));
        assertEquals(1, binarizerCalls[0]);
        assertEquals(1, groupFinderCalls[0]);
    }


    // A small test that uses existing classes
    @Test
    void findsGroupsInASmallImageUsingTheRealHelpers() {
        BufferedImage image = new BufferedImage(4, 3, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, TARGET_COLOR);
        image.setRGB(1, 0, TARGET_COLOR);
        image.setRGB(1, 1, TARGET_COLOR);
        image.setRGB(3, 2, TARGET_COLOR);

        ImageGroupFinder finder = new BinarizingImageGroupFinder(
                new DistanceImageBinarizer(new EuclideanColorDistance(), TARGET_COLOR, 1),
                new DfsBinaryGroupFinder());

        assertEquals(List.of(
                        new Group(3, new Coordinate(0, 0)),
                        new Group(1, new Coordinate(3, 2))),
                finder.findConnectedGroups(image));
    }

    // No matching pixels test (all are an invalid color)
    @Test
    void returnsNoGroupsWhenTheImageContainsNoMatchingPixels() {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ImageGroupFinder finder = new BinarizingImageGroupFinder(
                new DistanceImageBinarizer(new EuclideanColorDistance(), TARGET_COLOR, 0),
                new DfsBinaryGroupFinder());

        assertEquals(List.of(), finder.findConnectedGroups(image));
    }
}