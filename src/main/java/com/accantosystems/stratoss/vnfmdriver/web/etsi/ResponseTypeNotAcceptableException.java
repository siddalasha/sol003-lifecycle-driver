package com.accantosystems.stratoss.vnfmdriver.web.etsi;

public class ResponseTypeNotAcceptableException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ResponseTypeNotAcceptableException(String message) {
        super(message);
    }

    public ResponseTypeNotAcceptableException(String message, Throwable cause) {
        super(message, cause);
    }

}
