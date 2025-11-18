package org.egov.bpa.web.model.property;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyDetails {
    private String ownerName;
    private String guardianName;
    private String address;
    private String phone;
    private String ulb;
    private String ward;
    private String buildingUse;
}
