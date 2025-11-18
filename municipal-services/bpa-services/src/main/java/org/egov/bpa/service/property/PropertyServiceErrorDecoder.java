package org.egov.bpa.service.property;


import feign.Response;
import feign.codec.ErrorDecoder;
import org.egov.bpa.exception.PropertyNotFoundException;
import org.egov.bpa.exception.PropertyServiceException;

public class PropertyServiceErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        switch (response.status()) {
            case 404:
                return new PropertyNotFoundException("Property not found");
            case 400:
                return new PropertyServiceException("Bad request - Invalid property number");
            case 401:
                return new PropertyServiceException("Authentication failed");
            case 500:
                return new PropertyServiceException("Internal server error");
            default:
                return defaultErrorDecoder.decode(methodKey, response);
        }
    }
}

