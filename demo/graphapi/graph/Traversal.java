package graph;

/* See restrictions in Graph.java. */


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.Arrays;


/** Implements a generalized traversal of a graph.  At any given time,
 *  there is a particular collection of untraversed vertices---the "fringe."
 *  Traversal consists of repeatedly removing an untraversed vertex
 *  from the fringe, visting it, and then adding its untraversed
 *  successors to the fringe.
 *
 *  Generally, the client will extend Traversal.  By overriding the visit
 *  method, the client can determine what happens when a node is visited.
 *  By supplying an appropriate type of Queue object to the constructor,
 *  the client can control the behavior of the fringe. By overriding the
 *  shouldPostVisit and postVisit methods, the client can arrange for
 *  post-visits of a node (as in depth-first search).  By overriding
 *  the reverseSuccessors and processSuccessor methods, the client can control
 *  the addition of neighbor vertices to the fringe when a vertex is visited.
 *
 *  Traversals may be interrupted or restarted, remembering the previously
 *  marked vertices.
 *  @author E. Khumalo
 */
public abstract class Traversal {

    /** A Traversal of G, using FRINGE as the fringe. */
    protected Traversal(Graph G, Queue<Integer> fringe) {
        _G = G;
        _fringe = fringe;
        markedArray = new boolean[_G.maxVertex() + 1];
    }

    /** Unmark all vertices in the graph. */
    public void clear() {
        markedArray = new boolean[_G.maxVertex() + 1];
    }

    /** Initialize the fringe to V0 and perform a traversal. */
    public void traverse(Collection<Integer> V0) {
        _fringe.clear();
        _fringe.addAll(V0);
        clear();
        int node = Integer.MIN_VALUE;
        traverseHelper(node);
    }

    /**
     * Traverse Helper Method.
     * @param node Node
     * @return int.
     */
    private int traverseHelper(int node) {
        while (!_fringe.isEmpty()) {
            node = _fringe.remove();
            if (node < 0) {
                postVisit(-node);
                return traverseHelper(-node);
            }
            if (!marked(node)) {
                mark(node);

                if (!visit(node)) {
                    return Integer.MAX_VALUE;
                }

                if (!doDFS(node)) {
                    Iterator allNeig = _G.successors(node).iterator();

                    for (Iterator it = allNeig; it.hasNext(); ) {
                        int neig = (int) it.next();
                        if (processSuccessor(node, neig)) {
                            _fringe.add(neig);
                        }
                    }
                    if (shouldPostVisit(node)) {
                        _fringe.add(-node);
                    }
                }
            }
        }
        return node;
    }

    /**
     * Perform DFS Helper method.
     * @param node vertex.
     * @return Boolean.
     */
    private boolean doDFS(int node) {
        if (reverseSuccessors(node)) {
            if (shouldPostVisit(node)) {
                _fringe.add(-node);
            }
            ArrayList<Integer> toSort = new ArrayList<Integer>();
            for (int i = _G.degree(node) - 1; i >= 0; i--) {
                int neig = _G.successor(node, i);
                if (processSuccessor(node, neig)) {
                    _fringe.add(neig);
                }
            }
            return true;
        }
        return false;
    }

    /** Initialize the fringe to { V0 } and perform a traversal. */
    public void traverse(int v0) {
        traverse(Arrays.<Integer>asList(v0));
    }

    /** Returns true iff V has been marked. */
    protected boolean marked(int v) {
        return markedArray[v];
    }

    /** Mark vertex V. */
    protected void mark(int v) {
        markedArray[v] = true;
    }

    /** Perform a visit on vertex V.  Returns false iff the traversal is to
     *  terminate immediately. */
    protected boolean visit(int v) {
        return true;
    }

    /** Return true if we should postVisit V after traversing its
     *  successors.  (Post-visiting generally is useful only for depth-first
     *  traversals, although we define it for all traversals.) */
    protected boolean shouldPostVisit(int v) {
        return false;
    }

    /** Revisit vertex V after traversing its successors.  Returns false iff
     *  the traversal is to terminate immediately. */
    protected boolean postVisit(int v) {
        return true;
    }

    /** Return true if we should schedule successors of V in reverse order. */
    protected boolean reverseSuccessors(int v) {
        return false;
    }

    /** Process successor V to U.  Returns true iff V is then to
     *  be added to the fringe.  By default, returns true iff V is unmarked. */
    protected boolean processSuccessor(int u, int v) {
        return !marked(v);
    }

    /** The graph being traversed. */
    private final Graph _G;
    /** The fringe. */
    protected final Queue<Integer> _fringe;

    /**
     * Marked ELEMENTS Array.
     */
    private boolean[] markedArray;
}
