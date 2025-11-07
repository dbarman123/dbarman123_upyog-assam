package org.egov.dx.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Form data object for ePramaan logout submission
 * This is returned to the frontend which will submit it as a form to ePramaan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EPramaanLogoutFormData {

    @JsonProperty("sub")
    private String sub;

    @JsonProperty("clientId")
    private String clientId;

    @JsonProperty("redirectUrl")
    private String redirectUrl;

    @JsonProperty("logoutRequestId")
    private String logoutRequestId;

    @JsonProperty("hmac")
    private String hmac;

    @JsonProperty("iss")
    private String iss;

    @JsonProperty("customParameter")
    private String customParameter;

    @JsonProperty("sessionId")
    private String sessionId;
}

