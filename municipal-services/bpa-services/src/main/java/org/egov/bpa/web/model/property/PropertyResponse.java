package org.egov.bpa.web.model.property;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyResponse {
    private Integer status;
    private String message;
    private PropertyDataResponse data;
}




@Data
@NoArgsConstructor
@AllArgsConstructor
class ConstructionType {
    @JsonProperty("building_type")
    private String buildingType;

    @JsonProperty("construction_type")
    private String constructionType;

    @JsonProperty("construction_year")
    private String constructionYear;

    @JsonProperty("land_area")
    private String landArea;
}