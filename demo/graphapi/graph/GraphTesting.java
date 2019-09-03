package graph;

import org.junit.Test;
import static org.junit.Assert.*;

/** Unit tests for the Graph class.
 *  @author E. Khumalo
 */
public class GraphTesting {


    @Test
    public void emptyGraph() {
        DirectedGraph g = new DirectedGraph();
        assertEquals("Initial graph has vertices", 0, g.vertexSize());
        assertEquals("Initial graph has edges", 0, g.edgeSize());
    }

    @Test
    public void addVertex() {
        DirectedGraph g = new DirectedGraph();
        g.add();
        g.add();
        g.add();
        g.add();
        g.add();
        g.remove(2);
        g.remove(3);
        assertEquals("Removed 2 nodes", 3, g.vertexSize());
        assertEquals("Max vertex remains the same", 5, g.maxVertex());
        g.add();
        g.add();
        g.add();
        g.remove(1);
        assertEquals("Initial graph has vertices", 6, g.maxVertex());
        assertEquals("Initial graph has _maxSize", 5, g.vertexSize());
    }

    @Test
    public void addEdge() {
        DirectedGraph g = new DirectedGraph();
        g.add();
        g.add();
        g.add();
        g.add();
        g.add();
        g.add(1, 3);
        g.add(3, 1);
        g.add(1, 2);
        g.add(2, 1);
        g.remove(1);
        assertEquals("Edge size is 0", 0, g.edgeSize());
    }
}
