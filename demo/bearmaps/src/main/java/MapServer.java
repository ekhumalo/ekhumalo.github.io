import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;
import java.util.ArrayList;
import java.util.Set;
import java.util.Collections;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.PriorityQueue;
import java.awt.Stroke;
import java.awt.BasicStroke;

/* Maven is used to pull in these dependencies. */
import com.google.gson.Gson;

import static spark.Spark.*;

/**
 * This MapServer class is the entry point for running the JavaSpark web server for the BearMaps
 * application project, receiving API calls, handling the API call processing, and generating
 * requested images and routes.
 * @author Alan Yao
 */
public class MapServer {
    /**
     * The root upper left/lower right longitudes and latitudes represent the bounding box of
     * the root tile, as the images in the img/ folder are scraped.
     * Longitude == x-axis; latitude == y-axis.
     */
    public static final double ROOT_ULLAT = 37.892195547244356, ROOT_ULLON = -122.2998046875,
            ROOT_LRLAT = 37.82280243352756, ROOT_LRLON = -122.2119140625;
    /** Each tile is 256x256 pixels. */
    public static final int TILE_SIZE = 256;
    /** HTTP failed response. */
    private static final int HALT_RESPONSE = 403;
    /** Route stroke information: typically roads are not more than 5px wide. */
    public static final float ROUTE_STROKE_WIDTH_PX = 5.0f;
    /** Route stroke information: Cyan with half transparency. */
    public static final Color ROUTE_STROKE_COLOR = new Color(108, 181, 230, 200);
    /** The tile images are in the IMG_ROOT folder. */
    private static final String IMG_ROOT = "img/";
    /**
     * The OSM XML file path. Downloaded from <a href="http://download.bbbike.org/osm/">here</a>
     * using custom region selection.
     **/
    private static final String OSM_DB_PATH = "berkeley.osm";
    /**
     * Each raster request to the server will have the following parameters
     * as keys in the params map accessible by,
     * i.e., params.get("ullat") inside getMapRaster(). <br>
     * ullat -> upper left corner latitude,<br> ullon -> upper left corner longitude, <br>
     * lrlat -> lower right corner latitude,<br> lrlon -> lower right corner longitude <br>
     * w -> user viewport window width in pixels,<br> h -> user viewport height in pixels.
     **/
    private static final String[] REQUIRED_RASTER_REQUEST_PARAMS = {"ullat", "ullon", "lrlat",
        "lrlon", "w", "h"};
    /**
     * Each route request to the server will have the following parameters
     * as keys in the params map.<br>
     * start_lat -> start point latitude,<br> start_lon -> start point longitude,<br>
     * end_lat -> end point latitude, <br>end_lon -> end point longitude.
     **/
    private static final String[] REQUIRED_ROUTE_REQUEST_PARAMS = {"start_lat", "start_lon",
        "end_lat", "end_lon"};
    /* Define any static variables here. Do not define any instance variables of MapServer. */
    private static GraphDB g;
    private static QuadTree dataQuad;

    /**
     * Place any initialization statements that will be run before the server main loop here.
     * Do not place it in the main function. Do not place initialization code anywhere else.
     * This is for testing purposes, and you may fail tests otherwise.
     **/
    public static void initialize() {
        dataQuad = new QuadTree();
        g = new GraphDB(OSM_DB_PATH);
    }

    public static void main(String[] args) {
        initialize();
        staticFileLocation("/page");
        /* Allow for all origin requests (since this is not an authenticated server, we do not
         * care about CSRF).  */
        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Request-Method", "*");
            response.header("Access-Control-Allow-Headers", "*");
        });

        /* Define the raster endpoint for HTTP GET requests. I use anonymous functions to define
         * the request handlers. */
        get("/raster", (req, res) -> {
            HashMap<String, Double> params =
                    getRequestParams(req, REQUIRED_RASTER_REQUEST_PARAMS);
            /* The png image is written to the ByteArrayOutputStream */
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            /* getMapRaster() does almost all the work for this API call */
            Map<String, Object> rasteredImgParams = getMapRaster(params, os);
            /* On an image query success, add the image data to the response */
            if (rasteredImgParams.containsKey("query_success")
                    && (Boolean) rasteredImgParams.get("query_success")) {
                String encodedImage = Base64.getEncoder().encodeToString(os.toByteArray());
                rasteredImgParams.put("b64_encoded_image_data", encodedImage);
            }
            /* Encode response to Json */
            Gson gson = new Gson();
            return gson.toJson(rasteredImgParams);
        });

        /* Define the routing endpoint for HTTP GET requests. */
        get("/route", (req, res) -> {
            HashMap<String, Double> params =
                    getRequestParams(req, REQUIRED_ROUTE_REQUEST_PARAMS);
            LinkedList<Long> route = findAndSetRoute(params);
            return !route.isEmpty();
        });

        /* Define the API endpoint for clearing the current route. */
        get("/clear_route", (req, res) -> {
            clearRoute();
            return true;
        });

        /* Define the API endpoint for search */
        get("/search", (req, res) -> {
            Set<String> reqParams = req.queryParams();
            String term = req.queryParams("term");
            Gson gson = new Gson();
            /* Search for actual location data. */
            if (reqParams.contains("full")) {
                List<Map<String, Object>> data = getLocations(term);
                return gson.toJson(data);
            } else {
                /* Search for prefix matching strings. */
                List<String> matches = getLocationsByPrefix(term);
                return gson.toJson(matches);
            }
        });

        /* Define map application redirect */
        get("/", (request, response) -> {
            response.redirect("/map.html", 301);
            return true;
        });
    }

    /**
     * Validate & return a parameter map of the required request parameters.
     * Requires that all input parameters are doubles.
     * @param req HTTP Request
     * @param requiredParams TestParams to validate
     * @return A populated map of input parameter to it's numerical value.
     */
    private static HashMap<String, Double> getRequestParams(
            spark.Request req, String[] requiredParams) {
        Set<String> reqParams = req.queryParams();
        HashMap<String, Double> params = new HashMap<>();
        for (String param : requiredParams) {
            if (!reqParams.contains(param)) {
                halt(HALT_RESPONSE, "Request failed - parameters missing.");
            } else {
                try {
                    params.put(param, Double.parseDouble(req.queryParams(param)));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    halt(HALT_RESPONSE, "Incorrect parameters - provide numbers.");
                }
            }
        }
        return params;
    }


    /**
     * Handles raster API calls, queries for tiles and rasters the full image. <br>
     * <p>
     *     The rastered photo must have the following properties:
     *     <ul>
     *         <li>Has dimensions of at least w by h, where w and h are the user viewport width
     *         and height.</li>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *         <li>If a current route exists, lines of width ROUTE_STROKE_WIDTH_PX and of color
     *         ROUTE_STROKE_COLOR are drawn between all nodes on the route in the rastered photo.
     *         </li>
     *     </ul>
     *     Additional image about the raster is returned and is to be included in the Json response.
     * </p>
     * @param params Map of the HTTP GET request's query parameters - the query bounding box and
     *               the user viewport width and height.
     * @param os     An OutputStream that the resulting png image should be written to.
     * @return A map of parameters for the Json response as specified:
     * "raster_ul_lon" -> Double, the bounding upper left longitude of the rastered image <br>
     * "raster_ul_lat" -> Double, the bounding upper left latitude of the rastered image <br>
     * "raster_lr_lon" -> Double, the bounding lower right longitude of the rastered image <br>
     * "raster_lr_lat" -> Double, the bounding lower right latitude of the rastered image <br>
     * "raster_width"  -> Double, the width of the rastered image <br>
     * "raster_height" -> Double, the height of the rastered image <br>
     * "depth"         -> Double, the 1-indexed quadtree depth of the nodes of the rastered image.
     * Can also be interpreted as the length of the numbers in the image string. <br>
     * "query_success" -> Boolean, whether an image was successfully rastered. <br>
     * @see #REQUIRED_RASTER_REQUEST_PARAMS
     */
    public static Map<String, Object> getMapRaster(Map<String, Double> params, OutputStream os) throws IOException {
        HashMap<String, Object> rasteredImageParams = new HashMap<>();
        double query_dpp = (params.get("lrlon") - params.get("ullon"))/params.get("w");
        collectTiles(params, dataQuad.getRoot(), query_dpp);
        ArrayList<QuadNode> intersectingTiles = temp;
        Collections.sort(intersectingTiles);
        temp = new ArrayList<>();

        QuadNode first = intersectingTiles.get(0);
        QuadNode last = intersectingTiles.get(intersectingTiles.size() - 1);

        double rasterUlLon = first.ullon;
        double rasterUlLat = first.ullat;
        double rasterLrLon = last.lrlon;
        double rasterLrLat = last.lrlat;

        int numCol = 0;

        for (QuadNode item : intersectingTiles) {
            if (item.ullat != first.ullat) {
                break;}
            ++numCol;
        }

        int rasterWidth = TILE_SIZE * numCol;
        int rasterHeight = TILE_SIZE * intersectingTiles.size() / numCol;
        int depth = first.depth;

        rasteredImageParams.put("raster_ul_lon", rasterUlLon);
        rasteredImageParams.put("raster_ul_lat", rasterUlLat);
        rasteredImageParams.put("raster_lr_lon", rasterLrLon);
        rasteredImageParams.put("raster_lr_lat", rasterLrLat);
        rasteredImageParams.put("raster_width", rasterWidth);
        rasteredImageParams.put("raster_height", rasterHeight);
        rasteredImageParams.put("depth", depth);
        rasteredImageParams.put("query_success", true);

        BufferedImage result = new BufferedImage(
                rasterWidth, rasterHeight ,
                BufferedImage.TYPE_3BYTE_BGR);

        Graphics g = result.getGraphics();
        int x = 0, y = 0;
        for(QuadNode image : intersectingTiles){
            try {
                BufferedImage bi = ImageIO.read(new File(IMG_ROOT + image.fileName + ".png"));
                g.drawImage(bi,  x,  y, null);
                x += bi.getWidth();
                if(x >= result.getWidth()){
                    x = 0;
                    y += bi.getHeight();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Stroke stroke = new BasicStroke(
                MapServer.ROUTE_STROKE_WIDTH_PX, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        Graphics2D graphics2D = result.createGraphics();
        graphics2D.setStroke(stroke);
        graphics2D.setColor(ROUTE_STROKE_COLOR);

        if (!routeNodes.isEmpty()) {
            int i = routeNodes.size() - 2;
            while(i >= 0) {
                Node a = routeNodes.get(i);
                Node b = routeNodes.get(i + 1);
                double wSpan =  (rasterLrLon - rasterUlLon);
                double hSpan = (rasterUlLat - rasterLrLat);
                int startX = (int) (((a.lon - rasterUlLon) / wSpan) * result.getWidth());
                int startY = (int) (((rasterUlLat - a.lat) / hSpan) * result.getHeight());
                int endX = (int) (((b.lon - rasterUlLon) / wSpan) * result.getWidth());
                int endY = (int) (((rasterUlLat - b.lat) / hSpan) * result.getHeight());


                graphics2D.drawLine(startX, startY, endX, endY);
                System.out.println("Start: " + startX + "   " + startY + "   " +  endX + "   " + endY);
                System.out.println("Reached this end");
                i--;
            }
        }
        ImageIO.write(result, "png", os);
        return rasteredImageParams;
    }



    static ArrayList<QuadNode> temp = new ArrayList<>();
    private static void collectTiles(Map<String, Double> params, QuadNode qnode, double qdpp) {
        if (qnode.intersectsTile(params)) {
            if (qnode.satisfiesDepthorisLeaf(qdpp)) {
                temp.add(qnode);
            } else {
                collectTiles(params, qnode.node1, qdpp);
                collectTiles(params, qnode.node2, qdpp);
                collectTiles(params, qnode.node3, qdpp);
                collectTiles(params, qnode.node4, qdpp);
            }
        }
    }

    /**
     * Searches for the shortest route satisfying the input request parameters, sets it to be the
     * current route, and returns a <code>LinkedList</code> of the route's node ids for testing
     * purposes. <br>
     * The route should start from the closest node to the start point and end at the closest node
     * to the endpoint. Distance is defined as the euclidean between two points (lon1, lat1) and
     * (lon2, lat2).
     * @param params from the API call described in REQUIRED_ROUTE_REQUEST_PARAMS
     * @return A LinkedList of node ids from the start of the route to the end.
     */

    private static ArrayList<Node> routeNodes = new ArrayList<Node>();

    public static LinkedList<Long> findAndSetRoute(Map<String, Double> params) {
        routeNodes = new ArrayList<Node>();
        PriorityQueue<Node> fringe = new PriorityQueue<>();
        LinkedList<Long> closed = new LinkedList<Long>();
        HashMap<Node, Double> dist = new HashMap<Node, Double>();

        double slat = params.get("start_lat");
        double slon = params.get("start_lon");
        double elat = params.get("end_lat");
        double elon = params.get("end_lon");
        double startDistance = Double.POSITIVE_INFINITY;
        double endDistance = Double.POSITIVE_INFINITY;
        Node startNode = null;
        Node endNode = null;

        for (HashMap.Entry<Integer, Node> node : g.maphandler.nodeCollectionbackup.entrySet()) {
            Node value = node.getValue();
            double closestStartdistance = euclideanDistance(slat, slon, value.lat, value.lon);
            double closestEnddistance = euclideanDistance(elat, elon, value.lat, value.lon);

            if (closestStartdistance < startDistance) {
                startDistance = closestStartdistance;
                startNode = value;
            }
            if (closestEnddistance < endDistance) {
                endDistance = closestEnddistance;
                endNode = value;
            }
        }

        resetAll();
        dist.put(startNode, 0.0);
        startNode.priority = 0;
        fringe.add(startNode);


        while (!fringe.isEmpty()) {
            Node cur = fringe.remove();
            if (cur.equals(endNode)) {
                break;
            } else {
                for (Connection bond : cur.connectionSet) {
                    Node neighbor = bond.getNeighbor(cur);
                    double distance = dist.get(cur) + euclideanDistance(cur.lat, cur.lon, neighbor.lat, neighbor.lon);
                        if (!dist.containsKey(neighbor) || dist.get(neighbor) > distance) {
                           dist.put(neighbor, distance);
                            neighbor.setDistSofar(distance);
                            neighbor.setPriority(endNode);
                            fringe.add(neighbor);
                            neighbor.prev = cur;
                        }
                }
            }
        }
        Node recurseNode = endNode;
        while (recurseNode.prev != null) {
            closed.addFirst(recurseNode.id);
            routeNodes.add(recurseNode);
            recurseNode = recurseNode.prev;
        }
        closed.addFirst(recurseNode.id);
        routeNodes.add(recurseNode);
        return closed;
    }


    /**
     * Clear the current found route, if it exists.
     */

    public static double euclideanDistance(double x1, double y1, double x2, double y2) {
        double dX = x1 - x2;
        double dY = y1 - y2;
        return Math.hypot(dX, dY);
    }

    public static void resetAll() {
        for (HashMap.Entry<Integer, Node> node : g.maphandler.nodeCollectionbackup.entrySet()) {
            Node value = node.getValue();
            value.setDistSofar(Double.POSITIVE_INFINITY);
            value.priority = Double.POSITIVE_INFINITY;
            value.prev = null;
            value.marked = false;
        }
    }

    public static void clearRoute() {
        routeNodes = new ArrayList<Node>();
    }

    /**
     * In linear time, collect all the names of OSM locations that prefix-match the query string.
     * @param prefix Prefix string to be searched for. Could be any case, with or without
     *               punctuation.
     * @return A <code>List</code> of the full names of locations whose cleaned name matches the
     * cleaned <code>prefix</code>.
     */
    public static List<String> getLocationsByPrefix(String prefix) {
        return new LinkedList<>();
    }

    /**
     * Collect all locations that match a cleaned <code>locationName</code>, and return
     * information about each node that matches.
     * @param locationName A full name of a location searched for.
     * @return A list of locations whose cleaned name matches the
     * cleaned <code>locationName</code>, and each location is a map of parameters for the Json
     * response as specified: <br>
     * "lat" -> Number, The latitude of the node. <br>
     * "lon" -> Number, The longitude of the node. <br>
     * "name" -> String, The actual name of the node. <br>
     * "id" -> Number, The id of the node. <br>
     */
    public static List<Map<String, Object>> getLocations(String locationName) {
        //System.out.println(g.maphandler.locationMap);
        return g.maphandler.locationMap.get(locationName);
    }

}
