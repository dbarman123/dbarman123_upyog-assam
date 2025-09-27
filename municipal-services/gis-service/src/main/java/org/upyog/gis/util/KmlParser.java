package org.upyog.gis.util;

import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for parsing KML (Keyhole Markup Language) files and extracting polygon geometries.
 * 
 * <p>This class provides static methods for parsing KML files that contain polygon data
 * and converting them to JTS (Java Topology Suite) Polygon objects for spatial operations.
 * The parser supports standard KML polygon structure with outer boundary definitions.</p>
 * 
 * <p>Supported KML structure:</p>
 * <pre>
 * &lt;Polygon&gt;
 *   &lt;outerBoundaryIs&gt;
 *     &lt;LinearRing&gt;
 *       &lt;coordinates&gt;lon,lat,alt lon,lat,alt ...&lt;/coordinates&gt;
 *     &lt;/LinearRing&gt;
 *   &lt;/outerBoundaryIs&gt;
 * &lt;/Polygon&gt;
 * </pre>
 * 
 * @author GIS Service Team
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class KmlParser {

    /** JTS GeometryFactory for creating polygon geometries */
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    /**
     * Parses a KML file input stream and extracts the first geometry found (Polygon, LineString, or Point).
     * 
     * <p>This method automatically detects the geometry type and returns the appropriate JTS Geometry object.
     * It supports Polygon, LineString, and Point geometries from KML files.</p>
     *
     * @param kmlInputStream the input stream containing KML data
     * @return JTS Geometry object representing the parsed geometry
     * @throws Exception if KML parsing fails, no geometry found, or invalid coordinate data
     */
    public static Geometry parseGeometry(InputStream kmlInputStream) throws Exception {
        log.debug("Starting KML geometry parsing");
        
        try {
            // Configure XML parser for namespace-aware parsing
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(kmlInputStream);
            
            log.debug("KML document parsed successfully");

            // Try to find Polygon first
            NodeList polygonNodes = document.getElementsByTagName("Polygon");
            if (polygonNodes.getLength() > 0) {
                log.debug("Found {} polygon(s) in KML file, processing first one", polygonNodes.getLength());
                return parsePolygonElement((Element) polygonNodes.item(0));
            }

            // Try to find LineString
            NodeList lineStringNodes = document.getElementsByTagName("LineString");
            if (lineStringNodes.getLength() > 0) {
                log.debug("Found {} lineString(s) in KML file, processing first one", lineStringNodes.getLength());
                return parseLineStringElement((Element) lineStringNodes.item(0));
            }

            // Try to find Point
            NodeList pointNodes = document.getElementsByTagName("Point");
            if (pointNodes.getLength() > 0) {
                log.debug("Found {} point(s) in KML file, processing first one", pointNodes.getLength());
                return parsePointElement((Element) pointNodes.item(0));
            }

            log.error("No supported geometry elements (Polygon, LineString, Point) found in KML file");
            throw new RuntimeException("No supported geometry found in KML file");

        } catch (Exception e) {
            log.error("Failed to parse KML geometry", e);
            throw new RuntimeException("Failed to parse KML geometry: " + e.getMessage(), e);
        }
    }


    /**
     * Parses a Polygon element from KML and returns a JTS Polygon object.
     *
     * @param polygonElement the Polygon XML element
     * @return JTS Polygon object
     * @throws Exception if parsing fails
     */
    private static Polygon parsePolygonElement(Element polygonElement) throws Exception {
        // Look for outerBoundaryIs -> LinearRing -> coordinates
        NodeList outerBoundaryNodes = polygonElement.getElementsByTagName("outerBoundaryIs");
        if (outerBoundaryNodes.getLength() == 0) {
            log.error("No outer boundary found in Polygon element");
            throw new RuntimeException("No outer boundary found in Polygon");
        }

        Element outerBoundaryElement = (Element) outerBoundaryNodes.item(0);
        NodeList linearRingNodes = outerBoundaryElement.getElementsByTagName("LinearRing");
        if (linearRingNodes.getLength() == 0) {
            log.error("No LinearRing found in outer boundary");
            throw new RuntimeException("No LinearRing found in outer boundary");
        }

        Element linearRingElement = (Element) linearRingNodes.item(0);
        NodeList coordinatesNodes = linearRingElement.getElementsByTagName("coordinates");
        if (coordinatesNodes.getLength() == 0) {
            log.error("No coordinates found in LinearRing");
            throw new RuntimeException("No coordinates found in LinearRing");
        }

        String coordinatesText = coordinatesNodes.item(0).getTextContent().trim();
        log.debug("Extracted coordinates text: {}", coordinatesText.substring(0, Math.min(100, coordinatesText.length())));
        
        List<Coordinate> coordinates = parseCoordinates(coordinatesText);
        log.debug("Parsed {} coordinates from KML", coordinates.size());

        if (coordinates.size() < 4) {
            log.error("Insufficient coordinates: {}. Polygon must have at least 4 coordinates", coordinates.size());
            throw new RuntimeException("Polygon must have at least 4 coordinates");
        }

        // Ensure the polygon is closed (first and last coordinates are the same)
        if (!coordinates.get(0).equals(coordinates.get(coordinates.size() - 1))) {
            log.debug("Closing polygon by adding first coordinate as last");
            coordinates.add(new Coordinate(coordinates.get(0)));
        }

        Coordinate[] coordinateArray = coordinates.toArray(new Coordinate[0]);
        Polygon polygon = GEOMETRY_FACTORY.createPolygon(coordinateArray);
        log.info("Successfully created polygon with {} vertices", polygon.getCoordinates().length);
        
        return polygon;
    }

    /**
     * Parses a LineString element from KML and returns a JTS LineString object.
     *
     * @param lineStringElement the LineString XML element
     * @return JTS LineString object
     * @throws Exception if parsing fails
     */
    private static LineString parseLineStringElement(Element lineStringElement) throws Exception {
        NodeList coordinatesNodes = lineStringElement.getElementsByTagName("coordinates");
        if (coordinatesNodes.getLength() == 0) {
            log.error("No coordinates found in LineString");
            throw new RuntimeException("No coordinates found in LineString");
        }

        String coordinatesText = coordinatesNodes.item(0).getTextContent().trim();
        log.debug("Extracted coordinates text: {}", coordinatesText.substring(0, Math.min(100, coordinatesText.length())));
        
        List<Coordinate> coordinates = parseCoordinates(coordinatesText);
        log.debug("Parsed {} coordinates from KML", coordinates.size());

        if (coordinates.size() < 2) {
            log.error("Insufficient coordinates: {}. LineString must have at least 2 coordinates", coordinates.size());
            throw new RuntimeException("LineString must have at least 2 coordinates");
        }

        Coordinate[] coordinateArray = coordinates.toArray(new Coordinate[0]);
        LineString lineString = GEOMETRY_FACTORY.createLineString(coordinateArray);
        log.info("Successfully created lineString with {} vertices", lineString.getCoordinates().length);
        
        return lineString;
    }

    /**
     * Parses a Point element from KML and returns a JTS Point object.
     *
     * @param pointElement the Point XML element
     * @return JTS Point object
     * @throws Exception if parsing fails
     */
    private static Point parsePointElement(Element pointElement) throws Exception {
        NodeList coordinatesNodes = pointElement.getElementsByTagName("coordinates");
        if (coordinatesNodes.getLength() == 0) {
            log.error("No coordinates found in Point");
            throw new RuntimeException("No coordinates found in Point");
        }

        String coordinatesText = coordinatesNodes.item(0).getTextContent().trim();
        log.debug("Extracted coordinates text: {}", coordinatesText);
        
        List<Coordinate> coordinates = parseCoordinates(coordinatesText);
        log.debug("Parsed {} coordinates from KML", coordinates.size());

        if (coordinates.size() != 1) {
            log.error("Invalid coordinates: {}. Point must have exactly 1 coordinate", coordinates.size());
            throw new RuntimeException("Point must have exactly 1 coordinate");
        }

        Point point = GEOMETRY_FACTORY.createPoint(coordinates.get(0));
        log.info("Successfully created point at ({}, {})", point.getX(), point.getY());
        
        return point;
    }

    /**
     * Parses coordinate string from KML format into JTS Coordinate objects.
     * 
     * <p>KML coordinates are formatted as "longitude,latitude,altitude" separated by spaces.
     * This method handles the parsing and creates Coordinate objects with proper
     * longitude, latitude, and optional altitude values.</p>
     * 
     * <p>Example input: "-75.2,39.6,0 -74.6,39.6,0 -74.6,40.1,0"</p>
     *
     * @param coordinatesText the coordinate string from KML coordinates element
     * @return list of JTS Coordinate objects parsed from the input
     * @throws NumberFormatException if coordinate values cannot be parsed as doubles
     */
    private static List<Coordinate> parseCoordinates(String coordinatesText) {
        List<Coordinate> coordinates = new ArrayList<>();
        String[] coordStrings = coordinatesText.split("\\s+");
        
        log.debug("Parsing {} coordinate strings", coordStrings.length);

        for (String coordString : coordStrings) {
            coordString = coordString.trim();
            if (!coordString.isEmpty()) {
                String[] parts = coordString.split(",");
                if (parts.length >= 2) {
                    try {
                        double lon = Double.parseDouble(parts[0]);
                        double lat = Double.parseDouble(parts[1]);
                        double alt = parts.length > 2 ? Double.parseDouble(parts[2]) : 0.0;
                        coordinates.add(new Coordinate(lon, lat, alt));
                        log.trace("Parsed coordinate: lon={}, lat={}, alt={}", lon, lat, alt);
                    } catch (NumberFormatException e) {
                        log.warn("Skipping invalid coordinate: {}", coordString);
                    }
                } else {
                    log.warn("Skipping malformed coordinate (insufficient parts): {}", coordString);
                }
            }
        }
        
        log.debug("Successfully parsed {} valid coordinates", coordinates.size());
        return coordinates;
    }
}
