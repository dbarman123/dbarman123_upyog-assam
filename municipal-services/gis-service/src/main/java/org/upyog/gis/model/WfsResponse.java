package org.upyog.gis.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * WFS response model
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WfsResponse {

    private String type;
    private List<WfsFeature> features;
    private Integer totalFeatures;
    private Integer numberMatched;
    private Integer numberReturned;
}
