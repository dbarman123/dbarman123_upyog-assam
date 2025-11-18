package org.egov.bpa.web.model.property;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyDataResponse {
        @JsonProperty("holding")
        private String property;

        @JsonProperty("owner_name")
        private String ownerName;

        @JsonProperty("guardian_name")
        private String guardianName;

        private String address;
        private String phone;
        private String ulb;
        private String ward;

        @JsonProperty("building_use")
        private String buildingUse;

        @JsonProperty("construction_type")
        private List<ConstructionType> constructionType;

        @JsonProperty("tax_paid")
        private Boolean taxPaid;
    }
