import static org.junit.Assert.*;

import java.util.*;

import org.junit.Test;

public class DfsBinaryGroupFinderTest {
    @Test
    public void test_DfsBinaryGroupFinder_simple() {
        int[][] testImgData = 
        {
               // 0,1,2,3,4,5
        /*0*/    {1,1,1,0,1,1},
        /*1*/    {0,0,0,0,1,1},
        /*2*/    {1,1,0,0,0,0},
        /*3*/    {1,1,0,1,1,1},
        /*4*/    {1,1,0,1,1,1},
        };
        DfsBinaryGroupFinder DFSFinder = new DfsBinaryGroupFinder(); 
        List<Group> actual = DFSFinder.findConnectedGroups(testImgData);
        List<Group> expected = new ArrayList<>();

        expected.add(new Group(3, new Coordinate(1, 0)));
        expected.add(new Group(4, new Coordinate(4, 0)));
        expected.add(new Group(6, new Coordinate(0, 3)));
        expected.add(new Group(6, new Coordinate(4, 3)));

        Collections.sort(expected);
        Collections.sort(actual);
        assertEquals(expected, actual);
    }

    
    @Test
    public void test_DfsBinaryGroupFinder_emptyImage() {
        int[][] image = {
            {0,0,0},
            {0,0,0},
            {0,0,0}
        };

        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        List<Group> actual = finder.findConnectedGroups(image);

        assertEquals(List.of(), actual);
    }

    @Test
    public void test_DfsBinaryGroupFinder_singlePixel() {
        int[][] image = {
            {0,0,0},
            {0,0,1},
            {0,0,0}
        };

        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        List<Group> expected = List.of(
            new Group(1, new Coordinate(2, 1))
        );

        assertEquals(expected, finder.findConnectedGroups(image));
    }

    @Test
    public void test_DfsBinaryGroupFinder_allConnected() {
        int[][] image = {
            {1,1,1},
            {1,1,1}
        };

        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        List<Group> expected = List.of(
            new Group(6, new Coordinate(1, 0))
        );

        assertEquals(expected, finder.findConnectedGroups(image));
    }

    @Test
    public void test_DfsBinaryGroupFinder_horizontalGroups() {
        int[][] image = {
            {1,1,0,1,0,1,1}
        };

        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        List<Group> expected = List.of(
            new Group(1, new Coordinate(3, 0)),
            new Group(2, new Coordinate(0, 0)),
            new Group(2, new Coordinate(5, 0))
        );

        assertEquals(expected, finder.findConnectedGroups(image));
    }

    @Test
    public void test_DfsBinaryGroupFinder_nonsquareImage() {
        int[][] image = {
            {0,0,1,1},
            {0,0,1,0}
        };

        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        List<Group> expected = List.of(
            new Group(3, new Coordinate(2, 0))
        );

        assertEquals(expected, finder.findConnectedGroups(image));
    }

    @Test
    public void test_DfsBinaryGroupFinder_sortedOutput() {
        int[][] image = {
            {1,1,1,0,1},
            {0,0,0,0,0}
        };

        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        List<Group> expected = List.of(
            new Group(1, new Coordinate(4, 0)),
            new Group(3, new Coordinate(1, 0))
        );

        assertEquals(expected, finder.findConnectedGroups(image));
    }


}
