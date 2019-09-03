package make;

import graph.DirectedGraph;
import graph.LabeledGraph;

/** A directed, labeled subtype of Graph that describes dependencies between
 *  targets in a Makefile.
 *  @author E. Khumalo
 */
class Depends extends LabeledGraph<Rule, Void> {
    /** An empty dependency graph. */
    Depends() {
        super(new DirectedGraph());
    }
}
