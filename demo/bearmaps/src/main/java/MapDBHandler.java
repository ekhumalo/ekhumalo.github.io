import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Arrays;


/**
 *  Parses OSM XML files using an XML SAX parser. Used to construct the graph of roads for
 *  pathfinding, under some constraints.
 *  See OSM documentation on
 *  <a href="http://wiki.openstreetmap.org/wiki/Key:highway">the highway tag</a>,
 *  <a href="http://wiki.openstreetmap.org/wiki/Way">the way XML element</a>,
 *  <a href="http://wiki.openstreetmap.org/wiki/Node">the node XML element</a>,
 *  and the java
 *  <a href="https://docs.oracle.com/javase/tutorial/jaxp/sax/parsing.html">SAX parser tutorial</a>.
 *  @author Alan Yao
 */
public class MapDBHandler extends DefaultHandler {
    /**
     * Only allow for non-service roads; this prevents going on pedestrian streets as much as
     * possible. Note that in Berkeley, many of the campus roads are tagged as motor vehicle
     * roads, but in practice we walk all over them with such impunity that we forget cars can
     * actually drive on them.
     */
    private static final Set<String> ALLOWED_HIGHWAY_TYPES = new HashSet<>(Arrays.asList
            ("motorway", "trunk", "primary", "secondary", "tertiary", "unclassified",
                    "residential", "living_street", "motorway_link", "trunk_link", "primary_link",
                    "secondary_link", "tertiary_link"));
    private String activeState = "";
    private int currentNodeid = 0;
    private int currentNodeway = 0;
    private boolean goodRoad = false;
    private final GraphDB g;

    public MapDBHandler(GraphDB g) {
        this.g = g;
    }

    /**
     * Called at the beginning of an element. Typically, you will want to handle each element in
     * here, and you may want to track the parent element.
     * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or
     *            if Namespace processing is not being performed.
     * @param localName The local name (without prefix), or the empty string if Namespace
     *                  processing is not being performed.
     * @param qName The qualified name (with prefix), or the empty string if qualified names are
     *              not available. This tells us which element we're looking at.
     * @param attributes The attributes attached to the element. If there are no attributes, it
     *                   shall be an empty Attributes object.
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     * @see Attributes
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        /* Some example code on how you might begin to parse XML files. */
        if (qName.equals("node")) {
            activeState = "node";
            long id = Long.parseLong(attributes.getValue("id"));
            double lat = Double.parseDouble(attributes.getValue("lat"));
            double lon = Double.parseDouble(attributes.getValue("lon"));
            Node parseNode = new Node(id, lat, lon);
            currentNodeid = parseNode.hashCode();
            nodeCollection.put(currentNodeid, parseNode);
        } else if (qName.equals("way")) {
            activeState = "way";
            long id = Long.parseLong(attributes.getValue("id"));
            currentNodeway = (int) id;
//            System.out.println("Beginning a way...");
        } else if (activeState.equals("way") && qName.equals("nd")) {
            long ref = Long.parseLong(attributes.getValue("ref"));
            int refConnect = (int) ref;
            refList.addLast(refConnect);
        } else if (activeState.equals("way") && qName.equals("tag")) {
            String k = attributes.getValue("k");
            String v = attributes.getValue("v");
            if (k.equals("highway")) {
                if (ALLOWED_HIGHWAY_TYPES.contains(v)) {
                    goodRoad = true;
                }
            }
//            System.out.println("Tag with k=" + k + ", v=" + v + ".");
        } else if (activeState.equals("node") && qName.equals("tag") && attributes.getValue("k")
                .equals("name")) {
            String k = attributes.getValue("k");
            String v = attributes.getValue("v");

            nodeCollection.get(currentNodeid).tagData.put(k, v);
            if (locationMap.containsKey(v)) {
                locationMap.get(v).add(nodeCollectionbackup.get(currentNodeid));
            } else {
                LinkedList temp = new LinkedList();
                temp.add(nodeCollectionbackup.get(currentNodeid));
                locationMap.put(v, temp);
            }
//            System.out.println("Node with name: " + attributes.getValue("v"));
        }
    }

    HashMap<Integer, Node> nodeCollection = new HashMap<Integer, Node>();
    HashMap<Integer, Node> nodeCollectionbackup = new HashMap<Integer, Node>();
    HashMap<String, LinkedList> locationMap = new HashMap<String, LinkedList>();
    LinkedList<Integer> refList= new LinkedList<>();


    /**
     * Receive notification of the end of an element. You may want to take specific terminating
     * actions here, like finalizing vertices or edges found.
     * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or
     *            if Namespace processing is not being performed.
     * @param localName The local name (without prefix), or the empty string if Namespace
     *                  processing is not being performed.
     * @param qName The qualified name (with prefix), or the empty string if qualified names are
     *              not available.
     * @throws SAXException  Any SAX exception, possibly wrapping another exception.
     */

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("way")) {
            if (goodRoad) {
                int first = refList.removeFirst();
                while (!refList.isEmpty()) {
                    int second = refList.get(0);
                    Node node1 = nodeCollection.get(first);
                    Node node2 = nodeCollection.get(second);
                    Connection newConnection = new Connection(node1, node2);
                    node1.connectionSet.add(newConnection);
                    node2.connectionSet.add(newConnection);
                    first = refList.removeFirst();
                }
            } else {
                refList = new LinkedList<>();
            }
            goodRoad = false;
        }
    }

}
