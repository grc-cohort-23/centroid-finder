import java.util.List;

public class LargestGroupSelector {

    public Coordinate select(List<Group> groups) {
        if (groups.isEmpty()) {
            return new Coordinate(-1, -1);
        }

        Group largest = groups.get(0);

        for (Group group : groups) {
            if (group.size() > largest.size()) {
                largest = group;
            }
        }

        return largest.centroid();
    }
}