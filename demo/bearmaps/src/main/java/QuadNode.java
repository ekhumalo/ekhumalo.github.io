/**
 * Created by ekhumalo on 4/14/16.
 */
import java.util.Map;


public class QuadNode implements Comparable<QuadNode> {
    QuadNode parent;
    double ullon;
    double lrlon;
    double ullat;
    double lrlat;
    String fileName;
    int depth;
    double dpp;
    QuadNode node1, node2, node3, node4;

    public QuadNode(QuadNode parent, String fileName, int depth) {
        this.parent = parent;
        this.fileName = fileName;
        this.depth = depth;
    }

    public void setCoordinates() {
        char str = this.fileName.charAt(this.fileName.length() - 1);
        double x1 = this.parent.ullon;
        double y1 = this.parent.ullat;
        double x2 = this.parent.lrlon;
        double y2 = this.parent.lrlat;

        if (str == "1".charAt(0)) {
            this.ullon = x1;
            this.ullat = y1;
            this.lrlon = (x1 + x2) / 2;
            this.lrlat = (y1 + y2) / 2;
            this.dpp = (this.lrlon - this.ullon) / MapServer.TILE_SIZE;
        }

        if (str == "2".charAt(0)) {
            this.ullon = (x1 + x2) / 2;
            this.ullat = y1;
            this.lrlon = x2;
            this.lrlat = (y1 + y2) / 2;
            this.dpp = (this.lrlon - this.ullon) / MapServer.TILE_SIZE;
        }

        if (str == "3".charAt(0)) {
            this.ullon = x1;
            this.ullat = (y1 + y2) / 2;
            this.lrlon = (x1 + x2) / 2;
            this.lrlat = y2;
            this.dpp = (this.lrlon - this.ullon) / MapServer.TILE_SIZE;
        }

        if (str == "4".charAt(0)) {
            this.ullon = (x1 + x2) / 2;
            this.ullat = (y1 + y2) / 2;
            this.lrlon = x2;
            this.lrlat = y2;
            this.dpp = (this.lrlon - this.ullon) / MapServer.TILE_SIZE;
        }
    }

    public boolean intersectsTile(Map<String, Double> params) {
        return !(lrlat > params.get("ullat")
                || ullat < params.get("lrlat")
                || ullon > params.get("lrlon")
                || lrlon < params.get("ullon"));

    }

    public boolean satisfiesDepthorisLeaf(double queryDpp) {
        return this.dpp <= queryDpp || this.depth == 7;
    }

    @Override
    public int compareTo(QuadNode o) {
        int compareLat = Double.compare(-this.ullat, -o.ullat);
        if (compareLat == 0) {
            return Double.compare(this.ullon, o.ullon);
        }
        return compareLat;
    }
}
