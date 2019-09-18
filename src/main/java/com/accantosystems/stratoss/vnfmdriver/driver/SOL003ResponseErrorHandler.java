package com.accantosystems.stratoss.vnfmdriver.driver;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
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
            // First, check that the response contains JSON
            if (e.getResponseHeaders() != null && e.getResponseHeaders().getContentType() != null && e.getResponseHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON)) {
                // Retrieve the body of the response and check it's not empty
                String responseBody = e.getResponseBodyAsString();
                if (!StringUtils.isEmpty(responseBody)) {
                    // Attempt to parse a ProblemDetails record out of the body of the response
                    ProblemDetails problemDetails = objectMapper.readValue(responseBody, ProblemDetails.class);
                    // Check mandatory fields to see if this is indeed a valid ETSI SOL003-compliant error response
                    if (problemDetails.getStatus() != null && problemDetails.getDetail() != null) {
                        throw new SOL003ResponseException("Received SOL003-compliant error when communicating with VNFM", e, problemDetails);
                    }
                }
            }
            // Else, attempt to extract information out of the error response (as best as possible)
            final String responseBody = e.getResponseBodyAsString();
            String detailsMessage = e.getStatusText();
            if (!StringUtils.isEmpty(responseBody)) {
                detailsMessage += ": " + responseBody;
            }
            throw new SOL003ResponseException("Caught REST client exception when communicating with VNFM", new ProblemDetails(e.getRawStatusCode(), detailsMessage));
        } catch (Exception e) {
            throw new SOL003ResponseException("Caught general exception when communicating with VNFM", e);
        }
    }

}
