package org.egov.dx.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.AESDecrypter;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.pkce.CodeChallengeMethod;
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.OIDCScopeValue;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.dx.util.Configurations;
import org.egov.dx.web.models.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.*;

@Service
@Slf4j
public class EPramaanRequestService {

      
    private static final User User = null;

	@Autowired
    private RestTemplate restTemplate;

    @Autowired
	private Configurations configurations;

    public static final String SCOPE = "openid";
    public static final String RESPONSE_TYPE = "code";
    public static final String CPDE_CHALLENGE_METHOD = "S256";
    public static final String GRANT_TYPE = "authorization_code";
    public static final String ISS = "ePramaan";


    private static final String CLIENT_ID = "10*****1523";
    private static final String AES_KEY = "68*********9e" ;
    //private static final String REDIRECT_URI = "https://assamuat.niua.in/upyog-ui/ePramaan-login/";
    private static final String REDIRECT_URI = "http://localhost:3000/upyog-ui/citizen/";//"http://localhost:8082/Epramaan/ProcessAuthCodeAndGetToken";
    private static final String SERVICE_LOGOUT_URI = "http://localhost:3000/upyog-ui/citizen/";
    private static final String CERTIFICATE_PATH = "certificates/epramaan_staging.crt";
    private static final String CUSTOM_PARAMETER = "WhateverValueServiceWant";

    public static final String AUTH_GRANT_REQUEST_URI = "https://epstg.meripehchaan.gov.in/openid/jwt/processJwtAuthGrantRequest.do";
    public static final String TOKEN_REQUEST_URI = "https://epstg.meripehchaan.gov.in/openid/jwt/processJwtTokenRequest.do";
    public static final String LOGOUT_URI = "https://epstg.meripehchaan.gov.in/openid/jwt/processOIDCSLORequest.do";




    public AuthResponse getRedirectionURL(String module) throws Exception
    {
       /* // 1. Generate and save PKCE parameters (save these in DB/session)
        State stateID = new State(UUID.randomUUID().toString());
        Nonce nonce = new Nonce();
        CodeVerifier codeVerifier = new CodeVerifier();

        // TODO: Save these to database/session for later verification
        // saveToDatabase(stateID, nonce, codeVerifier);

        // 2. Generate code challenge from verifier
        CodeChallenge codeChallenge = CodeChallenge.compute(CodeChallengeMethod.S256, codeVerifier);

      //  String encodedRedirectUri = URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8);

        // 3. Build parameters using Spring's MultiValueMap
        Map<String, String> params = new LinkedHashMap<>();

        // Basic OIDC parameters
        params.put("response_type", "code"); // or configurations.getResponseType()
        params.put("client_id", CLIENT_ID);
        params.put("redirect_uri", REDIRECT_URI);
        params.put("scope", "openid"); // Add other scopes if needed
        params.put("state", stateID.getValue());
        params.put("nonce", nonce.getValue());

        // PKCE parameters
        params.put("code_challenge", codeChallenge.getValue());
        params.put("code_challenge_method", "S256");

        // 4. Calculate apiHmac for ePramaan
        String inputValue = CLIENT_ID + AES_KEY + stateID.getValue() +
                nonce.getValue() + REDIRECT_URI + "openid" +
                codeChallenge.getValue();
        String apiHmac = hashHMACHex(inputValue, AES_KEY);
        params.put("apiHmac", apiHmac);


        String authorizationUrl = buildAuthorizationUrl(
                AUTH_GRANT_REQUEST_URI,
                params
        );


        // 5. Build final URI
      //  UriComponents uriComponents = UriComponentsBuilder
        //        .fromHttpUrl(AUTH_GRANT_REQUEST_URI)
          //      .queryParams(params)
            //    .build();

        UriComponents uriComponents = UriComponentsBuilder
                .fromHttpUrl(authorizationUrl)
                .build();

        return uriComponents.toUri();*/

        //1. save codeVerifier, stateID, nonce in db
       State stateID = new State(UUID.randomUUID().toString());
       Nonce nonce = new Nonce();
       CodeVerifier codeVerifier = new CodeVerifier();

        Scope scope = new Scope();
        scope.add(OIDCScopeValue.OPENID);

        AuthenticationRequest authenticationRequest =
                new AuthenticationRequest.Builder(URI.create(AUTH_GRANT_REQUEST_URI), new ClientID(CLIENT_ID))
                        .scope(scope)
                        .state(stateID)
                        .redirectionURI(URI.create(REDIRECT_URI))
                        .endpointURI(URI.create(AUTH_GRANT_REQUEST_URI))
                        .codeChallenge(codeVerifier, CodeChallengeMethod.S256)
                        .nonce(nonce)
                        .responseType(new ResponseType(RESPONSE_TYPE)).build();

        String inputValue = CLIENT_ID + AES_KEY + stateID + nonce + REDIRECT_URI + SCOPE + authenticationRequest.getCodeChallenge();
        String apiHmac = hashHMACHex(inputValue, AES_KEY);
        String finalUrl = authenticationRequest.toURI().toString() + "&apiHmac=" + apiHmac;
        //return finalUrl;
        UriComponents uriComponents = UriComponentsBuilder
                .fromHttpUrl(finalUrl)
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .redirectURL(uriComponents.toUri().toString())
                .epramaanData(EPramaanData.builder()
                                .codeVerifier(codeVerifier.getValue())
                                .nonce(nonce.getValue())
                                .state(stateID.getValue())
                                .build()).build();

        return authResponse;
    }

    public URI getCitizenRedirectionURL(String module,AuthResponse authResponse) throws NoSuchAlgorithmException
    {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("response_type", configurations.getResponseType());
        params.add("state", configurations.getState());
        if(module.equalsIgnoreCase("SSO")) {
            params.add("redirect_uri", configurations.getRegisterRedirectURL());
            params.add("client_id", configurations.getRegisterClientId());}

        else {
            params.add("redirect_uri", configurations.getPtRedirectURL());
            params.add("client_id", configurations.getClientId());
        }
        params.add("code_challenge",getCodeChallenge(authResponse));
        params.add("code_challenge_method", "S256");
        params.add("dl_flow", configurations.getDlFlow());
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(configurations.getAuthorizationURL()).queryParams(params)
                .build();



        return uriComponents.toUri();
    }


    private String buildAuthorizationUrl(String baseUrl, Map<String, String> params) {
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        urlBuilder.append("?");

        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                urlBuilder.append("&");
            }
            try {
                urlBuilder.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("UTF-8 encoding not supported", e);
            }
            first = false;
        }

        return urlBuilder.toString();
    }



    private static String hashHMACHex(String inputValue, String hMACKey) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(hMACKey.getBytes(StandardCharsets.US_ASCII), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        return Base64.getUrlEncoder().encodeToString(sha256_HMAC.doFinal(inputValue.getBytes(StandardCharsets.US_ASCII)));
    }


    private String getCodeChallenge(AuthResponse authResponse) throws NoSuchAlgorithmException
    {     
    	String codeVerifier=getCodeVerifier();     
    	log.info("verifier is: " +codeVerifier ); 
    	//authResponse.setDlReqRef(codeVerifier);  
    	MessageDigest digest = MessageDigest.getInstance("SHA-256");     
    	byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));     
    	String encoded = Base64.getEncoder().withoutPadding().encodeToString(hash);     
    	encoded = encoded.replace("+", "-"); //Replace ’+’ with ’-’     
    	encoded= encoded.replace("/", "_");     
    	log.info("challenge is: " +encoded );        
    	EncReqObject encReqObject = EncReqObject.builder().tenantId("pg").type("Normal").value(codeVerifier).build();        
    	EncryptionRequest encryptionRequest = EncryptionRequest.builder().encryptionRequests(Collections.singletonList(encReqObject)).build();        
    	String responseBody= restTemplate.postForEntity(configurations.getEncHost() + configurations.getEncEncryptURL(), encryptionRequest, String.class).getBody();     
        try {
       	    String value = new ObjectMapper().readValue(responseBody, String[].class)[0];
       	 authResponse.setDlReqRef(value);
       	} catch (Exception e) {
       	    e.printStackTrace();
       	}
    	return encoded;              
    	}
    
    private String getCodeVerifier()
    {
    	 int leftLimit = 45; // numeral '0'
    	    int rightLimit = 126; // letter 'z'
    	    int targetStringLength = 60;
    	    Random random = new Random();

    	    String generatedString = random.ints(leftLimit, rightLimit + 1)
    	      .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 95) && i!=47 && i!=96 && i!=123 && i!=124 && i!=125)
    	      .limit(targetStringLength)
    	      .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
    	      .toString();

    	    return generatedString;
    }
    
    public TokenRes getToken(TokenReq tokenReq) {

        EPramaanData ePramaanData = tokenReq.getEPramaanData();

        JSONObject data = new JSONObject();
        data.put("code", new String[] { tokenReq.getCode() });
        data.put("grant_type", new String[] { GRANT_TYPE });
        data.put("scope", new String[] { SCOPE });
        data.put("redirect_uri", new String[] { AUTH_GRANT_REQUEST_URI });
        data.put("request_uri", new String[] { REDIRECT_URI });
        data.put("code_verifier", new String[] { ePramaanData.getCodeVerifier() });
        data.put("client_id", new String[] { CLIENT_ID });

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(data.toString(), headers);
        log.info("Request to ePramaan Token API: " + data.toString());
        ResponseEntity<String> responseData = restTemplate.exchange(TOKEN_REQUEST_URI, HttpMethod.POST, entity, String.class);
        log.info("Response from ePramaan Token API: " + responseData.getBody());
        String jweToken = responseData.getBody();
        SecretKeySpec secretKeySpec = null;
        try {
            secretKeySpec = (SecretKeySpec) generateAES256Key(ePramaanData.getNonce());
        } catch (Exception e) {
            log.error("Error in generating AES key: ", e);
            throw new RuntimeException(e);
        }
        JWEObject jweObject = null;
        try {
            jweObject = JWEObject.parse(jweToken);
        } catch (ParseException e) {
            log.error("Error in parsing JWE token: ", e);
            throw new RuntimeException(e);
        }
        try {
            jweObject.decrypt(new AESDecrypter(secretKeySpec));
        } catch (JOSEException e) {
            log.error("Error in decrypting JWE token: ", e);
            throw new RuntimeException(e);
        }
        SignedJWT signedJWT = jweObject.getPayload().toSignedJWT();
        JWSVerifier jwsVerifier = null;
        try {
            jwsVerifier = new RSASSAVerifier((RSAPublicKey) getPublicKey());
        } catch (Exception e) {
            log.error("Error in getting public key: ", e);
            throw new RuntimeException(e);
        }
        boolean signatureVerified = false;
        try {
            signatureVerified = signedJWT.verify(jwsVerifier);
        } catch (JOSEException e) {
            log.error("Error in verifying JWS signature: ", e);
            throw new RuntimeException(e);
        }
        Map<String, Object> objectObjectMap;
        if (signatureVerified) {
            Map<String, Object> JWS = signedJWT.getPayload().toJSONObject();
            System.out.println("JWT: " + JWS);
            objectObjectMap = signedJWT.getPayload().toJSONObject();
            log.info("Token Response from ePramaan: " + objectObjectMap);
        }
         return new TokenRes();
    }

    public Key generateAES256Key(String seed) throws Exception {
        MessageDigest sha256 = null;
        sha256 = MessageDigest.getInstance("SHA-256");
        byte[] passBytes = seed.getBytes();
        byte[] passHash = sha256.digest(passBytes);
        SecretKeySpec secretKeySpec = new SecretKeySpec(passHash, "AES");
        return secretKeySpec;
    }

    public static PublicKey getPublicKey() throws Exception {
        CertificateFactory certFac = CertificateFactory.getInstance("X.509");
        PublicKey publicKey = null;
        // Load certificate from resources folder
        try (InputStream certStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(CERTIFICATE_PATH)) {

            if (certStream == null) {
                throw new IllegalArgumentException("Certificate not found at: " + CERTIFICATE_PATH);
            }

            X509Certificate cer = (X509Certificate) certFac.generateCertificate(certStream);
            publicKey = cer.getPublicKey();
        }

       // FileInputStream fis = new FileInputStream(CERTIFICATE_PATH);

        return publicKey;
    }
    
    
    public UserRes getUser(TokenReq tokenReq)
    {
    	
    	 HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
         headers.set("Authorization", "Bearer "+tokenReq.getAuthToken());
      

         HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(null,
                 headers);
         UserRes userRes= restTemplate.postForEntity(configurations.getApiHost() + configurations.getUserOauthURI(), request, UserRes.class).getBody();
         return userRes;
    }

    public  List<IssuedDocument> getIssuedDocument(TokenReq tokenReq)
    {
    	
    	 HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
         headers.set("Authorization", "Bearer "+tokenReq.getAuthToken());
         
         HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(null,
                 headers);
         IssuedDocumentList issuedDocumentList= restTemplate.postForEntity(configurations.getApiHost() + configurations.getIssuedFilesURI(), request, IssuedDocumentList.class).getBody();
         return issuedDocumentList.getItems();
    }
    public byte[] getDoc(TokenReq tokenReq,String uri)
    {
    	
    	 HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
         headers.set("Authorization", "Bearer "+tokenReq.getAuthToken());
         headers.set("Authorization", "Bearer "+tokenReq.getAuthToken());
         MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

         map.add("uri",uri);
      

         HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(null,
                 headers);
         //MultipartFile doc= restTemplate.getForEntity(configurations.getApiHost() + configurations.getGetFileURI()+"/"+uri, request);
         ResponseEntity<String> entity = restTemplate.exchange(configurations.getApiHost() + configurations.getGetFileURI()+"/"+uri, HttpMethod.GET, 
                 request, String.class);
               
         
         return entity.getBody().getBytes();
    }

    public Object getOauthToken(RequestInfo requestinfo , TokenRes tokenRes)
    {
        RestTemplate restTemplate = new RestTemplate();
        UserRequest user = new UserRequest();
        user.setMobileNumber(tokenRes.getMobile());
        user.setName(tokenRes.getName());
        user.setDigilockerid(tokenRes.getDigilockerId());
        //TODO: remove hard coded tenant id
        user.setTenantId("pg");
        user.setAccess_token(tokenRes.getAccessToken());
        //user.setDob(tokenRes.getDob());
        
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setRequestInfo(requestinfo);
        createUserRequest.setUser(user);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        Object userOauth= restTemplate.postForEntity(configurations.getUserHost() + configurations.getUserEndpoint(), createUserRequest, Object.class).getBody();
        return userOauth;
    }
    
    public HttpEntity<String> decryptReq(List<String> decReqObject){
        HttpHeaders decryptHeaders = new HttpHeaders();
        decryptHeaders.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload = null;
        try {
            jsonPayload = objectMapper.writeValueAsString(decReqObject);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return new HttpEntity<String>(jsonPayload, decryptHeaders);

    }


   /* public static void main(String[] args) throws IOException {
       // get file "/certificates/epramaan_staging.crt" and print content from resource folder
        // Load file from classpath
        try (InputStream inputStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(CERTIFICATE_PATH)) {

            if (inputStream == null) {
                throw new IllegalArgumentException("Certificate not found at: " + CERTIFICATE_PATH);
            }

            // Read and print file content
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

                String line;
                System.out.println("---- Certificate Content ----");
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                System.out.println("---- End of Certificate ----");
            }
        }
    }*/
}
