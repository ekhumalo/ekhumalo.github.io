/**
 * Created by ekhumalo on 4/12/16.
 */
public class QuadTree {
    private static QuadNode root;

    public static final double ROOT_ULLAT = 37.892195547244356, ROOT_ULLON = -122.2998046875,
            ROOT_LRLAT = 37.82280243352756, ROOT_LRLON = -122.2119140625;

    public QuadTree() {
        root = new QuadNode(null, "root", 0);
        root.lrlat = MapServer.ROOT_LRLAT;
        root.lrlon = MapServer.ROOT_LRLON;
        root.ullat = MapServer.ROOT_ULLAT;
        root.ullon = MapServer.ROOT_ULLON;
        root.dpp = (root.lrlon - root.ullon) / MapServer.TILE_SIZE;
        root.node1 = new QuadNode(root, "1", 1);
        root.node2 = new QuadNode(root, "2", 1);
        root.node3 = new QuadNode(root, "3", 1);
        root.node4 = new QuadNode(root, "4", 1);
        root.node1.setCoordinates();
        root.node2.setCoordinates();
        root.node3.setCoordinates();
        root.node4.setCoordinates();
        createChildren(root.node1);
        createChildren(root.node2);
        createChildren(root.node3);
        createChildren(root.node4);
    }

    public static void createChildren(QuadNode qnode) {
        if (qnode.depth != 7) {
            qnode.node1 = new QuadNode(qnode, qnode.fileName + "1", qnode.depth + 1);
            qnode.node2 = new QuadNode(qnode, qnode.fileName + "2", qnode.depth + 1);
            qnode.node3 = new QuadNode(qnode, qnode.fileName + "3", qnode.depth + 1);
            qnode.node4 = new QuadNode(qnode, qnode.fileName + "4", qnode.depth + 1);
            qnode.node1.setCoordinates();
            qnode.node2.setCoordinates();
            qnode.node3.setCoordinates();
            qnode.node4.setCoordinates();
            createChildren(qnode.node1);
            createChildren(qnode.node2);
            createChildren(qnode.node3);
            createChildren(qnode.node4);
        }

    }

    public static QuadNode getRoot() {
        return root;
    }


    public static void main(String[] args) {
        new QuadTree();
    }
}
