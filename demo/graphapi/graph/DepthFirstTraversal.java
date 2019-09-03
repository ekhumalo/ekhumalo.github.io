package graph;

/* See restrictions in Graph.java. */

import java.util.LinkedList;

/** Implements a depth-first traversal of a graph.  Generally, the
 *  client will extend this class, overriding the visit and
 *  postVisit methods, as desired (by default, they do nothing).
 *  @author E. Khumalo
 */
public class DepthFirstTraversal extends Traversal {

    /** A depth-first Traversal of G. */
    protected DepthFirstTraversal(Graph G) {
        super(G, new LinkedListDeque());
        _G = G;
    }

    @Override
    protected boolean visit(int v) {
        return super.visit(v);
    }

    @Override
    protected boolean postVisit(int v) {
        return super.postVisit(v);
    }

    @Override
    protected boolean shouldPostVisit(int v) {
        return true;
    }

    @Override
    protected boolean reverseSuccessors(int v) {
        return true;
    }


    /**
     * Class to act as a Queue for DFS.
     */
    static class LinkedListDeque extends LinkedList<Integer> {
        /**
         * My fringe, initialization.
         */
        LinkedListDeque() {
            super(new LinkedList<>());
        }
        @Override
        public boolean add(Integer a) {
            this.addLast(a);
            return true;
        }

        /**
         * Remove Last Integer.
         * @return Last int.
         */
        public Integer remove() {
            return this.removeLast();
        }
    }

    /**
     * My Graph.
     */
    private Graph _G;

}
