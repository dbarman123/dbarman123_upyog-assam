package org.upyog.gis.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

/**
 * WFS Feature response model
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WfsFeature {

    private String type;
    private String id;
    private Map<String, Object> properties;
    private Object geometry;
}
