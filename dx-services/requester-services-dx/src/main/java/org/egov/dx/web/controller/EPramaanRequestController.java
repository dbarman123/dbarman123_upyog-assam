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
import java.util.List;


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
    public ResponseEntity<TokenResponse> getToken(@Valid @RequestBody TokenRequest tokenRequest) {

        TokenRes tokenRes = null;
        try {
            tokenRes = ePramaanRequestService.getToken(tokenRequest.getTokenReq());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfoFromRequestInfo(tokenRequest.getRequestInfo(), null);
        TokenResponse tokenResponse = TokenResponse.builder().responseInfo(responseInfo).tokenRes(tokenRes).build();

        return new ResponseEntity<>(tokenResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/token/citizen", method = RequestMethod.POST)
    public ResponseEntity<Object> getTokenCitizen(@Valid @RequestBody TokenRequest tokenRequest) {

        TokenRes tokenRes = null;
        tokenRes = ePramaanRequestService.getToken(tokenRequest.getTokenReq());
        Object user = ePramaanRequestService.getOauthToken(tokenRequest.getRequestInfo(), tokenRes);

        return new ResponseEntity<>(user, HttpStatus.OK);
    }


    @RequestMapping(value = "/details", method = RequestMethod.POST)
    public ResponseEntity<TokenResponse> getDetails(@Valid @RequestBody TokenRequest tokenRequest) {
        UserRes userRes = ePramaanRequestService.getUser(tokenRequest.getTokenReq());
        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfoFromRequestInfo(tokenRequest.getRequestInfo(), null);
        TokenResponse tokenResponse = TokenResponse.builder().responseInfo(responseInfo).tokenRes(null).userRes(userRes).build();
        return new ResponseEntity<>(tokenResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/callback", method = RequestMethod.POST)
    public ResponseEntity<EparmaanReponse> eparmaanCallback(@Valid @RequestBody EparmaanRequest eparmaanRequest) {

        System.out.println("Eparmaan Callback Request Received: " + eparmaanRequest);
        String code = eparmaanRequest.getCode();
        String authToken = eparmaanRequest.getAuthToken();

        EparmaanReponse eparmaanReponse = EparmaanReponse.builder().code(code).authToken(authToken).build();
        return new ResponseEntity<>(eparmaanReponse, HttpStatus.OK);
    }

}
