package graph;

/* See restrictions in Graph.java. */


import java.util.*;


/** The shortest paths through an edge-weighted graph.
 *  By overrriding methods getWeight, setWeight, getPredecessor, and
 *  setPredecessor, the client can determine how to represent the weighting
 *  and the search results.  By overriding estimatedDistance, clients
 *  can search for paths to specific destinations using A* search.
 *  @author E. Khumalo
 */
public abstract class ShortestPaths {

    /** The shortest paths in G from SOURCE. */
    public ShortestPaths(Graph G, int source) {
        this(G, source, 0);
    }

    /** A shortest path in G from SOURCE to DEST. */
    public ShortestPaths(Graph G, int source, int dest) {
        _G = G;
        _source = source;
        _dest = dest;
        _fringeT = new TreeDeque();

        _traversal = new Astar(_G, _fringeT);
    }

    /** Initialize the shortest paths.  Must be called before using
     *  getWeight, getPredecessor, and pathTo. */
    public void setPaths() {
        setWeight(_source, 0);
        _traversal.traverse(_source);
    }

    /** Returns the starting vertex. */
    public int getSource() {
        return _source;
    }

    /** Returns the target vertex, or 0 if there is none. */
    public int getDest() {
        return _dest;
    }

    /** Returns the current weight of vertex V in the graph.  If V is
     *  not in the graph, returns positive infinity. */
    public abstract double getWeight(int v);

    /** Set getWeight(V) to W. Assumes V is in the graph. */
    protected abstract void setWeight(int v, double w);

    /** Returns the current predecessor vertex of vertex V in the graph, or 0 if
     *  V is not in the graph or has no predecessor. */
    public abstract int getPredecessor(int v);

    /** Set getPredecessor(V) to U. */
    protected abstract void setPredecessor(int v, int u);

    /** Returns an estimated heuristic weight of the shortest path from vertex
     *  V to the destination vertex (if any).  This is assumed to be less
     *  than the actual weight, and is 0 by default. */
    protected double estimatedDistance(int v) {
        return 0.0;
    }

    /** Returns the current weight of edge (U, V) in the graph.  If (U, V) is
     *  not in the graph, returns positive infinity. */
    protected abstract double getWeight(int u, int v);

    /** Returns a list of vertices starting at _source and ending
     *  at V that represents a shortest path to V.  Invalid if there is a
     *  destination vertex other than V. */
    public List<Integer> pathTo(int v) {
        LinkedList<Integer> path = new LinkedList<Integer>();
        if (!(v != _dest && _dest != _source && v == _source)) {
            int pathItem = v;
            while (pathItem != _source && pathItem != 0) {
                path.addFirst(pathItem);
                pathItem = getPredecessor(pathItem);
            }
            path.addFirst(pathItem);
            return path;
        } else {
            throw new IllegalArgumentException("Cannot assign vertex");
        }
    }

    /** Returns a list of vertices starting at the source and ending at the
     *  destination vertex. Invalid if the destination is not specified. */
    public List<Integer> pathTo()     {
        return pathTo(getDest());
    }

    /** The graph being searched. */
    protected final Graph _G;
    /** The starting vertex. */
    private final int _source;
    /** The target vertex. */
    private final int _dest;

    /**
     * My Fringe.
     */
    private final TreeDeque _fringeT;

    /**
     * My AStar.
     */
    private final Astar _traversal;



    /**
     * Dijkstra's Algorithm.
     */
    class Astar extends Traversal {
        /**
         * A Traversal of G, using FRINGE as the fringe.
         *
         * @param G graph
         * @param fringe my TreeDeque Fringe
         */
        protected Astar(Graph G, Queue<Integer> fringe) {
            super(G, fringe);
        }

        @Override
        protected boolean processSuccessor(int u, int v) {
            if (!marked(v)) {
                if (getWeight(v) > getWeight(u) + getWeight(u, v)) {
                    _fringe.remove(v);
                    setWeight(v, getWeight(u, v) + getWeight(u));
                    setPredecessor(v, u);
                    return true;
                }
                return false;
            }
            return false;
        }

        @Override
        protected boolean visit(int v) {
            return _dest != v;
        }

    }

    /**
     * TrreDeque Class.
     */
    class TreeDeque extends AbstractQueue<Integer> {
        /**
         * My queue.
         */
        private final TreeSet<Integer> _queue;

        /**
         * Inittializer.
         */
        TreeDeque() {
            _queue = new TreeSet<Integer>(new Comparator<Integer>() {
                @Override
                public int compare(Integer v, Integer u) {
                    if (getWeight(v) + estimatedDistance(v)
                            > getWeight(u) + estimatedDistance(u)) {
                        return 1;
                    } else if (getWeight(v) + estimatedDistance(v)
                            < getWeight(u) + estimatedDistance(u)) {
                        return -1;
                    }
                    return v - u;
                }
            });
        }

        @Override
        public Iterator<Integer> iterator() {
            return _queue.iterator();
        }

        @Override
        public int size() {
            return _queue.size();
        }

        @Override
        public boolean offer(Integer integer) {
            _queue.add(integer);
            return true;
        }

        @Override
        public Integer poll() {
            return _queue.pollFirst();
        }

        /**
         * Remove method.
         * @param node int
         * @return boolean
         */
        public boolean remove(int node) {
            boolean a = _queue.remove(node);
            return a;
        }

        @Override
        public Integer peek() {
            return _queue.first();
        }
    }



}
