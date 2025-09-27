package org.upyog.gis.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

import javax.validation.Valid;

/**
 * Request wrapper for GIS operations following eGov municipal services standard pattern.
 * 
 * <p>This wrapper class encapsulates both the standard eGov RequestInfo object
 * and GIS-specific request data. It follows the same pattern used across
 * other municipal services for consistent API structure.</p>
 * 
 * <p>Request structure:</p>
 * <pre>
 * {
 *   "RequestInfo": {
 *     "apiId": "gis-api",
 *     "userInfo": { ... }
 *   },
 *   "gisRequest": {
 *     "tenantId": "pg.citya",
 *     "applicationNo": "APP-123",
 *     "rtpiId": "RTPI-456"
 *   }
 * }
 * </pre>
 * 
 * @author GIS Service Team
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GISRequestWrapper {

    @JsonProperty("RequestInfo")
    @Valid
    private RequestInfo requestInfo;

    @JsonProperty("gisRequest")
    @Valid
    private GISRequest gisRequest;
}
