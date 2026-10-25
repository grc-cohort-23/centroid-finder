import static org.junit.jupiter.api.Assertions.*;
 import java.awt.image.BufferedImage;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class BinarizingImageGroupFinderTest {

    private FakeImageBinarizer fakeBinarizer;
    private FakeBinaryGroupFinder fakeGroupFinder;
    private BinarizingImageGroupFinder finder;

    private static class FakeImageBinarizer implements ImageBinarizer {


        int[][] binaryArray = {
            {1, 1, 0},
            {1, 1, 0},
            {1, 1, 0}
        };

        BufferedImage receivedImage;
        @Override public int[][] toBinaryArray(BufferedImage image) {
        return binaryArray;
    }
    @Override
     public BufferedImage toBufferedImage(int[][] image) {
        throw new UnsupportedOperationException("Not needed for this test");
    }
    }

    private static class FakeBinaryGroupFinder implements BinaryGroupFinder {
        int[][] receivedArray;
        List<Group> groupsToReturn = List.of();

        @Override 
        public List<Group> findConnectedGroups(int[][] image) {
            receivedArray = image;
            return groupsToReturn;
        }
    }

    
    


    @BeforeEach
   


    void setUp() {
         fakeBinarizer = new FakeImageBinarizer();
         fakeGroupFinder = new FakeBinaryGroupFinder();

         finder = new BinarizingImageGroupFinder(fakeBinarizer, fakeGroupFinder);

        
    }

    @Test
    void testBinaryArrayPassedToGroupFinder() {
        BufferedImage image = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);

        finder.findConnectedGroups(image);

        assertSame(fakeBinarizer.binaryArray, fakeGroupFinder.receivedArray);

    }

    @Test
    void testOriginalImagePassedToBinarizer() {
        BufferedImage image = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);

        finder.findConnectedGroups(image);

        assertSame(image, fakeBinarizer.receivedImage);
    }

    @Test
    void testCorrectedGroupsReturned() {
        BufferedImage image = new BufferedImage(3,3, BufferedImage.TYPE_INT_RGB);

        List<Group> expectedGroups = List.of(
            new Group(6, new Coordinate(0, 1)),
            new Group(1, new Coordinate(2, 2))
        );

        fakeGroupFinder.groupsToReturn = expectedGroups;

        List<Group> actualGroups = finder.findConnectedGroups(image);

        assertSame(expectedGroups, actualGroups);
        }

        @Test

        void testEmptyGroupsReturned() {
            BufferedImage image = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);

             fakeGroupFinder.groupsToReturn = List.of();

            List<Group> actualGroups = finder.findConnectedGroups(image);

        assertNotNull(actualGroups);
        assertTrue(actualGroups.isEmpty());

        }



    
}
