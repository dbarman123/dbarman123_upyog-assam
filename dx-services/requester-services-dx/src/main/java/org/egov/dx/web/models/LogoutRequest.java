package org.egov.dx.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

import javax.validation.Valid;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequest {

    @JsonProperty("RequestInfo")
    @Valid
    private RequestInfo requestInfo;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("sessionId")
    private String sessionId;

    @JsonProperty("sub")
    private String sub;  // Subject from JWT token - ePramaan user identifier

    @JsonProperty("tenantId")
    private String tenantId;
}

