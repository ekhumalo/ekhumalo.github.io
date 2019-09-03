import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ekhumalo on 4/12/16.
 */
public class Node implements Comparable<Node> {
    long id;
    double lat, lon;
    double distSofar = Double.POSITIVE_INFINITY;
    double priority = Double.POSITIVE_INFINITY;
    Node prev = null;
    boolean marked = false;
    String name;
    HashMap<String, String> tagData = new HashMap<String, String>();

    Set<Connection> connectionSet = new HashSet<>();

    public Node(long id, double lat, double lon) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Node node = (Node) o;

        if (id != node.id) {
            return false;
        }
        if (Double.compare(node.lat, lat) != 0) {
            return false;
        }
        return Double.compare(node.lon, lon) == 0;

    }

    @Override
    public int hashCode() {
        int result = (int) this.id;
        return result;
    }

    @Override
    public int compareTo(Node o) {
        int compareP =  Double.compare(this.priority, o.priority);
        if (compareP == 0) {
            compareP = Double.compare(this.priority - this.distSofar, o.priority - o.distSofar);
        }
        return compareP;

    }

    public void setPriority(Node end) {
        this.priority =  MapServer.euclideanDistance(this.lat, this.lon, end.lat, end.lon) + this.distSofar;
    }

    public void setDistSofar(double d) {
        this.distSofar = d;
    }
}
