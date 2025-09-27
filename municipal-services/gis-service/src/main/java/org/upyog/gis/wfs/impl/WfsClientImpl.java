package org.upyog.gis.wfs.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.upyog.gis.config.GisProperties;
import org.upyog.gis.model.WfsResponse;
import org.upyog.gis.wfs.WfsClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * HTTP-based Web Feature Service (WFS) client implementation with robust error handling and coordinate axis swapping.
 * 
 * <p>This client provides methods for querying WFS endpoints using spatial intersection queries.
 * It includes automatic retry logic with coordinate axis swapping to handle different
 * coordinate system orientations that may cause empty results.</p>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>CQL (Common Query Language) filter support for spatial queries</li>
 *   <li>Automatic retry with axis swapping for empty results</li>
 *   <li>Reactive HTTP client with timeout handling</li>
 *   <li>Comprehensive error handling and logging</li>
 * </ul>
 * 
 * @author GIS Service Team
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WfsClientImpl implements WfsClient {

    /** Reactive HTTP client for making WFS requests */
    private final WebClient webClient;
    
    /** GIS configuration properties for WFS endpoints and parameters */
    private final GisProperties gisProperties;

    /**
     * Queries WFS service for features that intersect with the given geometry.
     * 
     * <p>This method performs spatial intersection queries using CQL filters. It automatically
     * detects the geometry type (Polygon, LineString, Point) and uses the appropriate
     * spatial predicate. If the initial query returns empty results, it automatically 
     * retries with swapped coordinate axes to handle different coordinate system 
     * orientations (lat,lon vs lon,lat).</p>
     * 
     * <p>Query process:</p>
     * <ol>
     *   <li>Detect geometry type from WKT</li>
     *   <li>Execute initial WFS query with appropriate spatial predicate</li>
     *   <li>If empty results, swap coordinate axes and retry</li>
     *   <li>Return results or empty feature collection</li>
     * </ol>
     *
     * @param geometryWkt the geometry in Well-Known Text (WKT) format (Polygon, LineString, or Point)
     * @return WfsResponse containing WFS response with matching features
     * @throws Exception if WFS query fails or response cannot be parsed
     */
    @Override
    public WfsResponse queryFeatures(String geometryWkt) throws Exception {
        log.info("Starting WFS query for geometry intersection");
        
        String geomAttr = Optional.ofNullable(gisProperties.getWfsGeometryColumn()).orElse("the_geom");
        String typeName = gisProperties.getWfsTypeName();
        String baseUrl = gisProperties.getWfsUrl();
        
        log.debug("WFS query parameters - URL: {}, typeName: {}, geomAttr: {}", baseUrl, typeName, geomAttr);

        // Execute initial WFS query
        WfsResponse result = executeWfsQuery(baseUrl, typeName, geomAttr, geometryWkt);
        
        // If empty results, try with swapped axes
        if (isEmptyFeatureCollection(result)) {
            String swapped = swapWktAxes(geometryWkt);
            log.info("WFS returned empty for original WKT; retrying with swapped axes");
            log.debug("Swapped WKT: {}", swapped);
            result = executeWfsQuery(baseUrl, typeName, geomAttr, swapped);
        }
        
        log.info("WFS query completed, found {} features", 
                result != null && result.getFeatures() != null ? result.getFeatures().size() : 0);
        
        return result;
    }

    /**
     * Executes a WFS GetFeature request with spatial intersection query.
     * 
     * <p>Constructs and executes a WFS request using CQL filters for spatial intersection.
     * The method automatically detects the geometry type and uses the appropriate spatial
     * predicate. The request uses EPSG:4326 coordinate reference system and returns GeoJSON format.</p>
     *
     * @param baseUrl the base WFS service URL
     * @param typeName the feature type name to query
     * @param geomAttr the geometry attribute name for spatial filtering
     * @param geometryWkt the geometry in WKT format for intersection (Polygon, LineString, or Point)
     * @return WfsResponse containing the WFS response with typed features
     * @throws RuntimeException if WFS request fails or returns error status
     */
    private WfsResponse executeWfsQuery(String baseUrl, String typeName, String geomAttr, String geometryWkt) {
        try {
            // Detect geometry type and construct appropriate CQL filter
            String cql = constructCqlFilter(geomAttr, geometryWkt);
            
            // Build WFS GetFeature request URI
            URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .queryParam("service", "WFS")
                    .queryParam("version", "2.0.0")
                    .queryParam("request", "GetFeature")
                    .queryParam("typeName", typeName)
                    .queryParam("outputFormat", "application/json")
                    .queryParam("srsName", "EPSG:4326")          // explicit CRS
                    .queryParam("cql_filter", cql)
                    .build()
                    .encode()
                    .toUri();

            log.info("Executing WFS query: {}", uri);
            log.debug("CQL Filter: {}", cql);

            // Execute reactive HTTP request with error handling and retry logic
            String body = webClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(status -> status.isError(), resp -> resp.bodyToMono(String.class)
                    .flatMap(msg -> Mono.error(new RuntimeException("WFS error: " + resp.statusCode() + " - " + msg))))
                .bodyToMono(String.class)
                .retry(gisProperties.getMaxRetries()) // Configurable retry count
                .timeout(java.time.Duration.ofSeconds(gisProperties.getReadTimeoutSeconds())) // Configurable timeout
                .block();

            log.debug("WFS response received, parsing JSON");
            JsonNode jsonNode = new ObjectMapper().readTree(body);
            return new ObjectMapper().convertValue(jsonNode, WfsResponse.class);
            
        } catch (WebClientResponseException w) {
            log.error("WFS HTTP error: {} - {}", w.getRawStatusCode(), w.getMessage());
            throw new RuntimeException("WFS query failed: " + w.getRawStatusCode() + " " + w.getMessage(), w);
        } catch (Exception e) {
            log.error("WFS query execution failed", e);
            throw new RuntimeException("WFS query failed: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if a WFS response represents an empty feature collection.
     * 
     * <p>A feature collection is considered empty if:</p>
     * <ul>
     *   <li>The response is null</li>
     *   <li>The features list is empty or null</li>
     *   <li>The totalFeatures count is 0 or null</li>
     * </ul>
     *
     * @param response the WFS response as WfsResponse
     * @return true if the feature collection is empty, false otherwise
     */
    private boolean isEmptyFeatureCollection(WfsResponse response) {
        if (response == null) {
            log.debug("WFS response is null, considering empty");
            return true;
        }
        
        if (response.getFeatures() != null && !response.getFeatures().isEmpty()) {
            log.debug("Found {} features in response", response.getFeatures().size());
            return false;
        }
        
        if (response.getTotalFeatures() != null && response.getTotalFeatures() > 0) {
            log.debug("Total features count: {}", response.getTotalFeatures());
            return false;
        }
        
        log.debug("WFS response appears to be empty");
        return true;
    }

    /**
     * Constructs a CQL filter based on the geometry type and spatial predicate.
     * 
     * <p>This method detects the geometry type from the WKT string and constructs
     * the appropriate CQL filter using the correct spatial predicate:</p>
     * <ul>
     *   <li>Polygon: INTERSECTS (finds features that intersect with the polygon)</li>
     *   <li>LineString: INTERSECTS (finds features that intersect with the line)</li>
     *   <li>Point: INTERSECTS (finds features that contain the point)</li>
     * </ul>
     *
     * @param geomAttr the geometry attribute name
     * @param geometryWkt the geometry in WKT format
     * @return CQL filter string for the WFS query
     */
    private String constructCqlFilter(String geomAttr, String geometryWkt) {
        String geometryType = detectGeometryType(geometryWkt);
        String spatialPredicate = "INTERSECTS"; // Default for all geometry types
        
        log.debug("Detected geometry type: {}, using spatial predicate: {}", geometryType, spatialPredicate);
        
        String cql = String.format("%s(%s, %s)", spatialPredicate, geomAttr, geometryWkt);
        log.debug("Constructed CQL filter: {}", cql);
        
        return cql;
    }

    /**
     * Detects the geometry type from a WKT string.
     * 
     * @param wkt the WKT string
     * @return the geometry type (POLYGON, LINESTRING, or POINT)
     */
    private String detectGeometryType(String wkt) {
        if (wkt.toUpperCase().startsWith("POLYGON")) {
            return "POLYGON";
        } else if (wkt.toUpperCase().startsWith("LINESTRING")) {
            return "LINESTRING";
        } else if (wkt.toUpperCase().startsWith("POINT")) {
            return "POINT";
        } else {
            log.warn("Unknown geometry type in WKT: {}, defaulting to POLYGON", wkt);
            return "POLYGON";
        }
    }

    /**
     * Swaps coordinate axes in a WKT geometry string from (x,y) to (y,x) format.
     * 
     * <p>This method is used to handle coordinate system orientation differences
     * between different WFS services. Some services expect coordinates in
     * latitude,longitude order while others expect longitude,latitude.</p>
     * 
     * <p>Supports multiple geometry types:</p>
     * <ul>
     *   <li>Polygon: Swaps coordinates in the polygon ring</li>
     *   <li>LineString: Swaps coordinates in the line vertices</li>
     *   <li>Point: Swaps the single coordinate pair</li>
     * </ul>
     *
     * @param wkt the original WKT geometry string
     * @return WKT geometry string with swapped coordinate axes
     */
    private String swapWktAxes(String wkt) {
        log.debug("Swapping coordinate axes for WKT: {}", wkt);
        
        String geometryType = detectGeometryType(wkt);
        String result;
        
        switch (geometryType) {
            case "POLYGON":
                result = swapPolygonAxes(wkt);
                break;
            case "LINESTRING":
                result = swapLineStringAxes(wkt);
                break;
            case "POINT":
                result = swapPointAxes(wkt);
                break;
            default:
                log.warn("Unknown geometry type for axis swapping: {}, returning original", geometryType);
                result = wkt;
        }
        
        log.debug("Axis swap completed: {}", result);
        return result;
    }

    /**
     * Swaps coordinate axes for a Polygon WKT string.
     */
    private String swapPolygonAxes(String wkt) {
        // Extract coordinate pairs from WKT polygon
        String inner = wkt.replaceAll("^.*\\(\\(", "").replaceAll("\\)\\).*$", "");
        String[] pts = inner.split(",");
        List<String> swapped = new ArrayList<>();
        
        // Swap x,y to y,x for each coordinate pair
        for (String p : pts) {
            String[] xy = p.trim().split("\\s+");
            if (xy.length >= 2) {
                swapped.add(xy[1] + " " + xy[0]);
                log.trace("Swapped {} {} to {} {}", xy[0], xy[1], xy[1], xy[0]);
            }
        }
        
        // Ensure polygon ring is closed
        if (!swapped.isEmpty() && !swapped.get(0).equals(swapped.get(swapped.size()-1))) {
            swapped.add(swapped.get(0));
            log.debug("Closed polygon ring by adding first coordinate");
        }
        
        return "POLYGON((" + String.join(",", swapped) + "))";
    }

    /**
     * Swaps coordinate axes for a LineString WKT string.
     */
    private String swapLineStringAxes(String wkt) {
        // Extract coordinate pairs from WKT linestring
        String inner = wkt.replaceAll("^.*\\(", "").replaceAll("\\).*$", "");
        String[] pts = inner.split(",");
        List<String> swapped = new ArrayList<>();
        
        // Swap x,y to y,x for each coordinate pair
        for (String p : pts) {
            String[] xy = p.trim().split("\\s+");
            if (xy.length >= 2) {
                swapped.add(xy[1] + " " + xy[0]);
                log.trace("Swapped {} {} to {} {}", xy[0], xy[1], xy[1], xy[0]);
            }
        }
        
        return "LINESTRING(" + String.join(",", swapped) + ")";
    }

    /**
     * Swaps coordinate axes for a Point WKT string.
     */
    private String swapPointAxes(String wkt) {
        // Extract coordinate from WKT point
        String inner = wkt.replaceAll("^.*\\(", "").replaceAll("\\).*$", "");
        String[] xy = inner.trim().split("\\s+");
        
        if (xy.length >= 2) {
            log.trace("Swapped {} {} to {} {}", xy[0], xy[1], xy[1], xy[0]);
            return "POINT(" + xy[1] + " " + xy[0] + ")";
        }
        
        return wkt; // Return original if parsing fails
    }

}
