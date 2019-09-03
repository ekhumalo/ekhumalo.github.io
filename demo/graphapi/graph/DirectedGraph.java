package graph;

/* See restrictions in Graph.java. */

import java.util.ArrayList;

/** Represents a general unlabeled directed graph whose vertices are denoted by
 *  positive integers. Graphs may have self edges.
 *
 *  @author E. Khumalo
 */
public class DirectedGraph extends GraphObj {

    @Override
    public boolean isDirected() {
        return true;
    }

    @Override
    public int inDegree(int v) {
        if (contains(v)) {
            return getPredecessors().get(v).size();
        }
        return 0;
    }

    @Override
    public int predecessor(int v, int k) {
        if (contains(v)) {
            if (k < getPredecessors().get(v).size()) {
                return getPredecessors().get(v).get(k);
            }
            return 0;
        }
        return 0;
    }

    @Override
    public Iteration<Integer> predecessors(int v) {
        if (contains(v)) {
            return Iteration.iteration(getPredecessors().get(v));
        }
        return Iteration.iteration(new ArrayList<Integer>());
    }

}
