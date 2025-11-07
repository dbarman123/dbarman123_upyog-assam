package org.egov.dx.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.dx.service.EPramaanRequestService;
import org.egov.dx.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.security.NoSuchAlgorithmException;


@RestController
@Slf4j
@RequestMapping("/epramaan")
@CrossOrigin
public class EPramaanRequestController {

    @Autowired
    private EPramaanRequestService ePramaanRequestService;

    @Autowired
    ResponseInfoFactory responseInfoFactory;

    @RequestMapping(value = {"/authorization/url"}, method = RequestMethod.POST)
    public ResponseEntity<AuthResponse> search(@Valid @RequestBody RequestInfo requestInfo, @RequestParam("module") String module) throws NoSuchAlgorithmException {
        AuthResponse authResponse = null;
        try {
            authResponse = ePramaanRequestService.getRedirectionURL(module);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        log.info("Auth response : {}", authResponse);
        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }

    @RequestMapping(value = {"/authorization/url/citizen"}, method = RequestMethod.POST)
    public ResponseEntity<AuthResponse> searchForcitizen(@Valid @RequestBody RequestInfo requestInfo, @RequestParam("module") String module) throws NoSuchAlgorithmException {
        AuthResponse authResponse = new AuthResponse();
        URI redirectionURL = null;
        try {
            redirectionURL = ePramaanRequestService.getCitizenRedirectionURL(module, authResponse);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        authResponse.setRedirectURL(redirectionURL.toString());
        log.info("Redirection URL" + redirectionURL.toString());
        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }


    @RequestMapping(value = "/token", method = RequestMethod.POST)
    public ResponseEntity<EPramaanTokenResponse>  getToken(@Valid @RequestBody TokenRequest tokenRequest)    {

        EPramaanTokenRes tokenRes= null;
        try {
            tokenRes = ePramaanRequestService.getToken(tokenRequest.getTokenReq());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ResponseInfo responseInfo=ResponseInfoFactory.createResponseInfoFromRequestInfo(tokenRequest.getRequestInfo(), null);
        EPramaanTokenResponse tokenResponse=EPramaanTokenResponse.builder().responseInfo(responseInfo).tokenRes(tokenRes).build();

        return new ResponseEntity<>(tokenResponse,HttpStatus.OK);
    }

    @RequestMapping(value = "/token/citizen", method = RequestMethod.POST)
    public ResponseEntity<Object>  getTokenCitizen(@Valid @RequestBody TokenRequest tokenRequest)    {

        EPramaanTokenRes tokenRes= null;
        tokenRes = ePramaanRequestService.getToken(tokenRequest.getTokenReq());
        Object user = ePramaanRequestService.getOauthToken(tokenRequest.getRequestInfo() , tokenRes);
        
        // Add ePramaan sessionId and sub to the OAuth response for frontend to use in logout
        Object enhancedResponse = ePramaanRequestService.addEPramaanUserSessionInfo(user, tokenRes);

        log.info("Enhanced OAuth response with ePramaan session info: {}", enhancedResponse.toString());

        return new ResponseEntity<>(enhancedResponse, HttpStatus.OK);
    }


    @RequestMapping(value = "/details", method = RequestMethod.POST)
    public ResponseEntity<TokenResponse> getDetails(@Valid @RequestBody TokenRequest tokenRequest) {
        UserRes userRes = ePramaanRequestService.getUser(tokenRequest.getTokenReq());
        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfoFromRequestInfo(tokenRequest.getRequestInfo(), null);
        TokenResponse tokenResponse = TokenResponse.builder().responseInfo(responseInfo).tokenRes(null).userRes(userRes).build();
        return new ResponseEntity<>(tokenResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/callback", method = RequestMethod.POST)
    public ResponseEntity<Object> eparmaanCallback(@Valid @RequestBody EparmaanRequest eparmaanRequest) {

        log.info("Eparmaan Callback Request Received: " + eparmaanRequest);

        Object user = ePramaanRequestService.getToken(eparmaanRequest);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    /**
     * Generate ePramaan logout form data
     * Returns JSON object that frontend will submit as form to ePramaan logout URL
     * 
     * @param logoutRequest - Contains sessionId, sub, and tenantId
     * @return EPramaanLogoutFormData with HMAC and all required fields
     */
    @RequestMapping(value = "/getlogoutdata", method = RequestMethod.POST)
    public ResponseEntity<EPramaanLogoutFormData> getLogoutData(@Valid @RequestBody LogoutRequest logoutRequest) throws Exception {
        if (logoutRequest.getSessionId() == null || logoutRequest.getSessionId().isEmpty()) {
            throw new IllegalArgumentException("sessionId is required");
        }
        if (logoutRequest.getSub() == null || logoutRequest.getSub().isEmpty()) {
            throw new IllegalArgumentException("sub is required");
        }
        
        EPramaanLogoutFormData formData = ePramaanRequestService.generateEPramaanLogoutFormData(
            logoutRequest.getSessionId(),
            logoutRequest.getSub(),
            logoutRequest.getTenantId()
        );
        
        return new ResponseEntity<>(formData, HttpStatus.OK);
    }

}
