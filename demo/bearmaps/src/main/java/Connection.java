/**
 * Created by ekhumalo on 4/12/16.
 */

public class Connection {
    Node n1, n2;

    public Connection(Node n1, Node n2) {
        this.n1 = n1;
        this.n2 = n2;
    }

    public Node getNeighbor(Node o) {
        if (o.equals(this.n1)) {
            return this.n2;
        }
        return n1;
    }
}
