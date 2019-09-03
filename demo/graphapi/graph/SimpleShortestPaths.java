package graph;

/* See restrictions in Graph.java. */

/** A partial implementation of ShortestPaths that contains the weights of
 *  the vertices and the predecessor edges.   The client needs to
 *  supply only the two-argument getWeight method.
 *  @author E. Khumalo
 */
public abstract class SimpleShortestPaths extends ShortestPaths {

    /** The shortest paths in G from SOURCE. */
    public SimpleShortestPaths(Graph G, int source) {
        this(G, source, 0);
    }

    /** A shortest path in G from SOURCE to DEST. */
    public SimpleShortestPaths(Graph G, int source, int dest) {
        super(G, source, dest);
        _myGraph = new LabeledGraph<>(G);
    }

    /** Returns the current weight of edge (U, V) in the graph.  If (U, V) is
     *  not in the graph, returns positive infinity. */
    @Override
    protected abstract double getWeight(int u, int v);

    @Override
    public double getWeight(int v) {
        if (_myGraph.getLabel(v) != null) {
            return _myGraph.getLabel(v).weight;
        }
        return Double.POSITIVE_INFINITY;
    }

    @Override
    protected void setWeight(int v, double w) {
        if (_myGraph.getLabel(v) == null) {
            _myGraph.setLabel(v, new Node());
        }
        _myGraph.getLabel(v).weight = w;
    }

    @Override
    public int getPredecessor(int v) {
        if (_myGraph.getLabel(v) == null) {
            return 0;
        } else {
            return _myGraph.getLabel(v).parent;
        }
    }

    @Override
    protected void setPredecessor(int v, int u) {
        if (_myGraph.getLabel(v) == null) {
            _myGraph.setLabel(v, new Node());
        }
        _myGraph.getLabel(v).parent = u;
    }

    /**
     * My Labeled Graph.
     */
    private LabeledGraph<Node, Void> _myGraph;

    /**
     * Node Class, VL in LabeledGraph.
     */
    class Node {
        /**
         * Node constructor.
         */
        Node() {
            parent = 0;
            weight = Double.POSITIVE_INFINITY;
        }

        /**
         * The previous Node, aka Parent.
         */
        private int parent;

        /**
         * Weight of my graph.
         */
        private double weight;
    }

}
