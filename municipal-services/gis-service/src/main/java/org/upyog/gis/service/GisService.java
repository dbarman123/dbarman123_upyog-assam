package org.upyog.gis.service;

import org.upyog.gis.model.GISResponse;
import org.upyog.gis.model.GISRequestWrapper;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for GIS operations
 */
public interface GisService {

    /**
     * Find zone information from geometry file (polygon, line, or point)
     *
     * @param file the geometry file (KML)
     * @param gisRequestWrapper the GIS request wrapper containing RequestInfo and GIS request data
     * @return response containing district, zone, and WFS response
     * @throws Exception if processing fails
     */
    GISResponse findZoneFromGeometry(MultipartFile file, GISRequestWrapper gisRequestWrapper) throws Exception;
}
