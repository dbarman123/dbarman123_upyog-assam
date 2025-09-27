package org.upyog.gis.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.upyog.gis.client.FilestoreClient;
import org.upyog.gis.config.GisProperties;
import org.upyog.gis.model.GisLog;
import org.upyog.gis.model.GISResponse;
import org.upyog.gis.model.GISRequestWrapper;
import org.upyog.gis.model.GISRequest;
import org.upyog.gis.model.WfsFeature;
import org.upyog.gis.model.WfsResponse;
import org.egov.common.contract.response.ResponseInfo;
import org.upyog.gis.repository.GisLogRepository;
import org.upyog.gis.service.GisService;
import org.upyog.gis.util.KmlParser;
import org.upyog.gis.wfs.WfsClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTWriter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.util.UUID;

/**
 * Service implementation for GIS operations such as finding zone information from polygon files,
 * interacting with WFS, and logging GIS-related activities.
 * Handles KML uploads, WFS queries, and response formatting.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GisServiceImpl implements GisService {

    private static final long MAX_FILE_SIZE = 10L * 1024L * 1024L; // 10 MB

    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILURE = "FAILURE";

    private final WfsClient wfsClient;
    private final GisLogRepository logRepository;
    private final GisProperties gisProperties;
    private final ObjectMapper objectMapper;
    private final FilestoreClient filestoreClient;

    /**
     * Finds zone information from a geometry file (KML/XML), uploads it to filestore, parses the geometry,
     * queries WFS for district/zone, logs the operation, and returns a structured response.
     * Supports polygon, line, and point geometries.
     *
     * @param file the uploaded geometry file (KML/XML)
     * @param gisRequestWrapper the GIS request wrapper containing RequestInfo and GIS request data
     * @return structured response containing district, zone, and WFS data
     * @throws Exception if any processing step fails
     */
    @Override
    public GISResponse findZoneFromGeometry(MultipartFile file, GISRequestWrapper gisRequestWrapper) throws Exception {
        GISRequest gisRequest = gisRequestWrapper.getGisRequest();
        String fileStoreId = null;

        try {
            validatePolygonFile(file);

            // Upload to Filestore
            log.info("Uploading KML file to Filestore: {}", file.getOriginalFilename());
            fileStoreId = filestoreClient.uploadFile(file, gisRequest.getTenantId(), "gis", "kml-upload");
            log.info("File uploaded successfully with ID: {}", fileStoreId);

            // Parse KML to get geometry
            log.info("Parsing KML file to extract geometry");
            Geometry geometry = parseKmlFile(file);
            log.info("Successfully parsed {} geometry with {} vertices", geometry.getGeometryType(), geometry.getCoordinates().length);

            // Convert geometry to WKT format for WFS query
            WKTWriter wktWriter = new WKTWriter();
            String geometryWkt = wktWriter.write(geometry);
            log.info("Converted geometry to WKT: {}", geometryWkt);

            // Query WFS for district/zone information
            log.info("Querying WFS for district/zone information");
            WfsResponse wfsResponse = wfsClient.queryFeatures(geometryWkt);
            log.info("WFS query completed successfully");

            // Extract district and zone from WFS response
            String district = extractDistrict(wfsResponse);
            String zone = extractZone(wfsResponse);
            log.info("Extracted district: {}, zone: {}", district, zone);

            // Create details for logging
            ObjectNode detailsJson = objectMapper.createObjectNode();
            detailsJson.put("fileName", file.getOriginalFilename());
            detailsJson.put("fileSize", file.getSize());
            detailsJson.put("district", district);
            detailsJson.put("zone", zone);
            detailsJson.put("geometryType", geometry.getGeometryType());
            detailsJson.put("geometryVertices", geometry.getCoordinates().length);

            // Send success log to Kafka via persister
            GisLog successLog = createGisLog(gisRequest.getApplicationNo(), gisRequest.getRtpiId(), fileStoreId,
                    gisRequest.getTenantId(), STATUS_SUCCESS, "SUCCESS", "Successfully processed geometry and found district/zone", detailsJson,
                    gisRequestWrapper.getRequestInfo() != null && gisRequestWrapper.getRequestInfo().getUserInfo() != null
                        ? gisRequestWrapper.getRequestInfo().getUserInfo().getUuid() : "system");
            logRepository.save(successLog);

            // Clean WFS response for return
            JsonNode cleanWfsResponse = cleanWfsResponse(wfsResponse);

            // Create ResponseInfo
            ResponseInfo responseInfo = ResponseInfo.builder()
                    .apiId(gisRequestWrapper.getRequestInfo() != null ? gisRequestWrapper.getRequestInfo().getApiId() : null)
                    .ver(gisRequestWrapper.getRequestInfo() != null ? gisRequestWrapper.getRequestInfo().getVer() : null)
                    .ts(Instant.now().toEpochMilli())
                    .resMsgId("uief87324")
                    .msgId(gisRequestWrapper.getRequestInfo() != null ? gisRequestWrapper.getRequestInfo().getMsgId() : null)
                    .status("SUCCESSFUL")
                    .build();

            // Return successful response
            return GISResponse.builder()
                    .responseInfo(responseInfo)
                    .district(district)
                    .zone(zone)
                    .wfsResponse(cleanWfsResponse)
                    .fileStoreId(fileStoreId)
                    .build();

        } catch (Exception e) {
            log.error("Error finding zone from polygon file: {}", e.getMessage(), e);

            // Send failure log to Kafka via persister
            ObjectNode errorDetails = objectMapper.createObjectNode();
            errorDetails.put("fileName", file.getOriginalFilename());
            errorDetails.put("error", e.getMessage());
            if (fileStoreId != null) {
                errorDetails.put("fileStoreId", fileStoreId);
            }
            
            GisLog failureLog = createGisLog(gisRequest.getApplicationNo(), gisRequest.getRtpiId(), fileStoreId,
                    gisRequest.getTenantId(), STATUS_FAILURE, "FAILURE", e.getMessage(), errorDetails,
                    gisRequestWrapper.getRequestInfo() != null && gisRequestWrapper.getRequestInfo().getUserInfo() != null
                        ? gisRequestWrapper.getRequestInfo().getUserInfo().getUuid() : "system");
            logRepository.save(failureLog);

            throw new RuntimeException("Failed to process polygon file: " + e.getMessage(), e);
        }
    }

    /**
     * Validates the uploaded polygon file
     */
    private void validatePolygonFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 10MB");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.toLowerCase().endsWith(".kml") && !fileName.toLowerCase().endsWith(".xml"))) {
            throw new IllegalArgumentException("File must be a KML or XML file");
        }
    }

    /**
     * Parses KML file to extract geometry (polygon, line, or point)
     */
    private Geometry parseKmlFile(MultipartFile file) throws Exception {
        try (InputStream inputStream = file.getInputStream()) {
            return KmlParser.parseGeometry(inputStream);
        } catch (Exception e) {
            log.error("Failed to parse KML file: {}", e.getMessage());
            throw new Exception("Invalid KML file format: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts district information from WFS response using typed objects.
     * 
     * <p>This method provides type-safe access to WFS feature properties
     * and extracts the district information from the first matching feature.</p>
     *
     * @param wfsResponse the WFS response containing features with properties
     * @return district name or "Unknown" if not found
     */
    private String extractDistrict(WfsResponse wfsResponse) {
        if (wfsResponse != null && wfsResponse.getFeatures() != null && !wfsResponse.getFeatures().isEmpty()) {
            WfsFeature firstFeature = wfsResponse.getFeatures().get(0);
            if (firstFeature.getProperties() != null) {
                Object districtValue = firstFeature.getProperties().get(gisProperties.getWfsDistrictAttribute());
                if (districtValue != null) {
                    return districtValue.toString();
                }
            }
        }
        return "Unknown";
    }

    /**
     * Extracts zone information from WFS response using typed objects.
     * 
     * <p>This method provides type-safe access to WFS feature properties
     * and extracts the zone information from the first matching feature.</p>
     *
     * @param wfsResponse the WFS response containing features with properties
     * @return zone name or "Unknown" if not found
     */
    private String extractZone(WfsResponse wfsResponse) {
        if (wfsResponse != null && wfsResponse.getFeatures() != null && !wfsResponse.getFeatures().isEmpty()) {
            WfsFeature firstFeature = wfsResponse.getFeatures().get(0);
            if (firstFeature.getProperties() != null) {
                Object zoneValue = firstFeature.getProperties().get(gisProperties.getWfsZoneAttribute());
                if (zoneValue != null) {
                    return zoneValue.toString();
                }
            }
        }
        return "Unknown";
    }

    /**
     * Cleans WFS response for client consumption using typed objects.
     * 
     * <p>This method creates a simplified JSON response containing only essential
     * information from the WFS features, excluding demographic data and geometry objects.</p>
     *
     * @param wfsResponse the typed WFS response containing features
     * @return cleaned JsonNode with only essential properties
     */
    private JsonNode cleanWfsResponse(WfsResponse wfsResponse) {
        if (wfsResponse == null) {
            return objectMapper.createObjectNode();
        }
        
        // Create a simplified response with only essential information
        ObjectNode cleanResponse = objectMapper.createObjectNode();
        cleanResponse.put("type", "FeatureCollection");

        if (wfsResponse.getFeatures() != null && !wfsResponse.getFeatures().isEmpty()) {
        ArrayNode cleanFeatures = objectMapper.createArrayNode();

            for (WfsFeature feature : wfsResponse.getFeatures()) {
                ObjectNode cleanFeature = objectMapper.createObjectNode();
                cleanFeature.put("type", "Feature");

                // Only include essential properties, exclude demographic and geometric data
                if (feature.getProperties() != null) {
                ObjectNode cleanProperties = objectMapper.createObjectNode();
                    
                    // Only include state-related fields as per requirements
                    if (feature.getProperties().containsKey("STATE_NAME")) {
                        cleanProperties.put("STATE_NAME", feature.getProperties().get("STATE_NAME").toString());
                    }
                    if (feature.getProperties().containsKey("STATE_ABBR")) {
                        cleanProperties.put("STATE_ABBR", feature.getProperties().get("STATE_ABBR").toString());
                    }
                    if (feature.getProperties().containsKey("STATE_FIPS")) {
                        cleanProperties.put("STATE_FIPS", feature.getProperties().get("STATE_FIPS").toString());
                }

                cleanFeature.set("properties", cleanProperties);
                }
                
                // Exclude geometry object entirely as requested
                // cleanFeature.set("geometry", null);

                cleanFeatures.add(cleanFeature);
            }

            cleanResponse.set("features", cleanFeatures);
        }

        return cleanResponse;
    }

    /**
     * Creates a GIS log entry with a unique ID for Kafka publishing.
     * 
     * @param applicationNo the application number
     * @param rtpiId the RTPI ID
     * @param fileStoreId the file store ID
     * @param tenantId the tenant ID
     * @param status the status
     * @param responseStatus the response status
     * @param responseJson the response JSON
     * @param details the details as JsonNode
     * @param createdBy the user who created the log
     * @return GisLog object ready for Kafka publishing
     */
    private GisLog createGisLog(String applicationNo, String rtpiId, String fileStoreId, String tenantId, 
                               String status, String responseStatus, String responseJson, JsonNode details, String createdBy) {
        return GisLog.builder()
                .id(UUID.randomUUID().toString()) // Generate unique ID for Kafka
                .applicationNo(applicationNo)
                .rtpiId(rtpiId)
                .fileStoreId(fileStoreId)
                .tenantId(tenantId)
                .status(status)
                .responseStatus(responseStatus)
                .responseJson(responseJson)
                .createdby(createdBy)
                .createdtime(Instant.now().toEpochMilli())
                .details(details)
                .build();
    }
}