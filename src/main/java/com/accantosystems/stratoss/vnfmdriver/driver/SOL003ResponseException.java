package com.accantosystems.stratoss.vnfmdriver.driver;

import org.springframework.web.client.RestClientException;

import com.accantosystems.stratoss.vnfmdriver.model.etsi.ProblemDetails;

public class SOL003ResponseException extends RestClientException {

    private final ProblemDetails problemDetails;

    public SOL003ResponseException(String msg, ProblemDetails problemDetails) {
        super(msg);
        this.problemDetails = problemDetails;
    }

    public SOL003ResponseException(String msg, Throwable ex, ProblemDetails problemDetails) {
        super(msg, ex);
        this.problemDetails = problemDetails;
    }

    public ProblemDetails getProblemDetails() {
        return problemDetails;
    }

}
