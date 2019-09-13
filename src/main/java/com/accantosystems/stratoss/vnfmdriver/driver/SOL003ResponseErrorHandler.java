package com.accantosystems.stratoss.vnfmdriver.driver;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClientResponseException;

import com.accantosystems.stratoss.vnfmdriver.model.etsi.ProblemDetails;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component("SOL003ResponseErrorHandler")
public class SOL003ResponseErrorHandler extends DefaultResponseErrorHandler {

    private final ObjectMapper objectMapper;

    @Autowired
    public SOL003ResponseErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
        try {
            super.handleError(clientHttpResponse);
        } catch (RestClientResponseException e) {
            // Attempt to get a ProblemDetails record out of the body of the response
            String responseBody = e.getResponseBodyAsString();
            ProblemDetails problemDetails = objectMapper.readValue(responseBody, ProblemDetails.class);
            // Check mandatory fields to see if this is indeed a valid ETSI SOL003-compliant error response
            if (problemDetails.getStatus() != null && problemDetails.getDetail() != null) {
                throw new SOL003ResponseException(problemDetails.getTitle(), e, problemDetails);
            }
            // Else, just re-throw the original exception
            throw e;
        }
    }

}
