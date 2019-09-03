package graph;

/* See restrictions in Graph.java. */

import java.util.ArrayList;
import java.util.LinkedList;

/** A partial implementation of Graph containing elements common to
 *  directed and undirected graphs.
 *
 *  @author E. Khumalo
 */
abstract class GraphObj extends Graph {

    /** A new, empty Graph. */
    GraphObj() {
        _nodes = new ArrayList<Integer>();
        _successors = new ArrayList<ArrayList<Integer>>();
        _predecessors = new ArrayList<ArrayList<Integer>>();
        _edges = new ArrayList<int[]>();
        _edgeIDS = new ArrayList<Integer>();
        _nodes.add(0);
        _successors.add(null);
        _predecessors.add(null);
        _vertexSize = 0;
        _maxVertex = 0;
        _edgeSize = 0;
    }

    @Override
    public int vertexSize() {
        return _vertexSize;
    }

    @Override
    public int maxVertex() {
        return _maxVertex;
    }

    @Override
    public int edgeSize() {
        return _edgeSize;
    }

    @Override
    public abstract boolean isDirected();

    @Override
    public int outDegree(int v) {
        if (contains(v)) {
            return _successors.get(v).size();
        }
        return 0;
    }

    @Override
    public abstract int inDegree(int v);

    @Override
    public boolean contains(int u) {
        try {
            return _nodes.get(u) != 0;
        } catch (IndexOutOfBoundsException excp) {
            return false;
        }
    }

    @Override
    public boolean contains(int u, int v) {
        return contains(u) && contains(v) && _edgeIDS.contains(edgeId(u, v));
    }

    @Override
    public int add() {
        int newNode;
        if (vertexSize() < maxVertex()) {
            int i = 1;
            for (; i < _nodes.size(); i++) {
                if (_nodes.get(i) == 0) {
                    _nodes.set(i, i);
                    _successors.set(i, new ArrayList<Integer>());
                    _predecessors.set(i, new ArrayList<Integer>());
                    break;
                }
            }
            _vertexSize++;
            return i;
        } else {
            newNode = _vertexSize + 1;
            _nodes.add(newNode);
            _successors.add(new ArrayList<Integer>());
            _predecessors.add(new ArrayList<Integer>());
            _vertexSize++;
            _maxVertex = _vertexSize;
            return newNode;
        }
    }

    @Override
    public int add(int u, int v) {
        int id = 0;
        if (contains(u) && contains(v)) {
            if (!contains(u, v)) {
                if (isDirected()) {
                    _successors.get(u).add(v);
                    _predecessors.get(v).add(u);
                } else {
                    _successors.get(u).add(v);
                    if (u != v) {
                        _successors.get(v).add(u);
                    }
                }
                id = edgeId(u, v);
                _edges.add(new int[] {u, v});
                _edgeIDS.add(id);
                _edgeSize++;
            }
            return id;
        } else {
            throw new IllegalArgumentException("bad edge");
        }
    }

    @Override
    public void remove(int v) {
        if (contains(v)) {
            ArrayList<Integer> toRemove = new ArrayList<Integer>();
            ArrayList<Integer> toRemove2 = new ArrayList<Integer>();
            for (int val : _successors.get(v)) {
                toRemove.add(val);
            }
            if (isDirected()) {
                for (int val2 : _predecessors.get(v)) {
                    toRemove2.add(val2);
                }
            }
            for (int val3 : toRemove) {
                remove(v, val3);
            }
            if (isDirected()) {
                for (int val4 : toRemove2) {
                    remove(val4, v);
                }
            }
            if (v == _maxVertex) {
                _nodes.remove(v);
                _successors.remove(v);
                _predecessors.remove(v);
                _maxVertex--;
            } else {
                _nodes.set(v, 0);
                _successors.set(v, new ArrayList<Integer>());
                _predecessors.set(v, new ArrayList<Integer>());
            }
            _vertexSize--;
        }
    }

    @Override
    public void remove(int u, int v) {
        if (contains(u) && contains(v)) {
            if (isDirected()) {
                _successors.get(u).remove((Integer) v);
                _predecessors.get(v).remove((Integer) u);
            } else {
                _successors.get(u).remove((Integer) v);
                _successors.get(v).remove((Integer) u);
            }
            int index = _edgeIDS.indexOf(edgeId(u, v));
            if (index == -1) {
                return;
            }
            _edges.remove(index);
            _edgeIDS.remove(index);
            _edgeSize--;
        }
    }

    @Override
    public Iteration<Integer> vertices() {
        LinkedList<Integer> verIter = new LinkedList<Integer>();
        for (int i = 1; i < _nodes.size(); i++) {
            if (_nodes.get(i) != 0) {
                verIter.add(i);
            }
        }
        return Iteration.iteration(verIter);
    }

    @Override
    public int successor(int v, int k) {
        if (contains(v)) {
            if (k < _successors.get(v).size()) {
                return _successors.get(v).get(k);
            }
            return 0;
        }
        return 0;
    }

    @Override
    public abstract int predecessor(int v, int k);

    @Override
    public Iteration<Integer> successors(int v) {
        if (contains(v)) {
            return Iteration.iteration(_successors.get(v));
        }
        return Iteration.iteration(new ArrayList<Integer>());
    }

    @Override
    public abstract Iteration<Integer> predecessors(int v);

    @Override
    public Iteration<int[]> edges() {
        return Iteration.iteration(_edges);
    }

    @Override
    protected void checkMyVertex(int v) {
        super.checkMyVertex(v);
    }

    @Override
    protected int edgeId(int u, int v) {
        if (contains(u) && contains(v)) {
            if (!this.isDirected()) {
                int a = u;
                int b = v;
                u = Math.min(a, b);
                v = Math.max(a, b);
            }
            return  pair(u, v);
        }
        return 0;
    }

    /**
     * Pairing function.
     * <a href="https://en.wikipedia.org/wiki/Pairing_function">
     *     Pairing Function
     * </a>
     * @param m int
     * @param n int
     * @return int
     */
    private int pair(int m, int n) {
        int val = (int) (0.5 * (m + n) * (m + n + 1) + n);
        if (depair(val)[0] != m && depair(val)[1] != n) {
            throw new IllegalArgumentException("Something is wrong here");
        }
        return val;
    }

    /**
     * Depair.
     * @param q int
     * @return int array.
     */
    private int[] depair(int q) {
        double w = Math.floor((Math.sqrt(8 * q + 1) - 1) / 2);
        double t = ((Math.pow(w, 2) + w) / 2);
        int y = (int) (q - t);
        int x = (int) (w - y);
        return new int[]{x, y};

    }

    /**
     * Get Predecessors.
     * @return 2D ArrayList.
     */
    ArrayList<ArrayList<Integer>> getPredecessors() {
        return _predecessors;
    }

    /**
     * My Vertices.
     */
    private ArrayList<Integer> _nodes;

    /**
     * Successors.
     */
    private ArrayList<ArrayList<Integer>> _successors;

    /**
     * Predecessors.
     */
    private ArrayList<ArrayList<Integer>> _predecessors;

    /**
     * My edges.
     */
    private ArrayList<int[]> _edges;

    /**
     * List of Edge IDs Mapping.
     */
    private ArrayList<Integer> _edgeIDS;

    /**
     * Vertex Size.
     */
    private int _vertexSize;

    /**
     * Edge Size.
     */
    private int _edgeSize;

    /**
     * Max Vertex.
     */
    private int _maxVertex;

}
