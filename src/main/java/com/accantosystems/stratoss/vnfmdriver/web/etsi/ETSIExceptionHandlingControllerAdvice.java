package com.accantosystems.stratoss.vnfmdriver.web.etsi;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.accantosystems.stratoss.vnfmdriver.model.etsi.ProblemDetails;

/**
 * Handles the conversion of Exceptions thrown by SOL003-compliant Rest API calls
 */
@RestControllerAdvice("com.accantosystems.stratoss.vnfmdriver.web.etsi")
public class ETSIExceptionHandlingControllerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ETSIExceptionHandlingControllerAdvice.class);

    @ExceptionHandler(HttpMessageConversionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    protected ProblemDetails handleHttpMessageConversionException(HttpServletRequest req, HttpMessageConversionException cause) {
        return defaultHandle("Unable to parse request", cause, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    protected ProblemDetails handleHttpMessageNotReadableException(HttpServletRequest req, HttpMessageNotReadableException cause) {
        return defaultHandle("Invalid content provided to request", cause, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    protected ProblemDetails handleMissingServletRequestParameterException(HttpServletRequest req, MissingServletRequestParameterException cause) {
        return defaultHandle(cause.getLocalizedMessage(), cause, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ResponseBody
    protected ProblemDetails handleHttpMediaTypeNotSupportedException(HttpServletRequest req, HttpMediaTypeNotSupportedException cause) {
        return defaultHandle("Invalid content provided to request", cause, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ResponseBody
    protected ProblemDetails handleHttpMediaTypeNotAcceptableException(HttpServletRequest req, HttpMediaTypeNotAcceptableException cause) {
        return defaultHandle("Invalid content provided to request", cause, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ResponseBody
    protected ProblemDetails handleHttpRequestMethodNotSupportedException(HttpServletRequest req, HttpRequestMethodNotSupportedException cause) {
        return defaultHandle("Invalid method used in request", cause, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    protected ProblemDetails handleAllExceptions(HttpServletRequest req, Exception cause) {
        logger.error("Attempting to handle an exception of type [{}]", cause.getClass().getSimpleName());
        return defaultHandle(cause.getLocalizedMessage(), cause, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ProblemDetails defaultHandle(String message, Exception cause, HttpStatus statusCode) {
        logError(message, cause);
        return buildProblemDetails(cause, statusCode);
    }

    private ProblemDetails buildProblemDetails(final Exception cause, final HttpStatus statusCode) {
        final ProblemDetails problemDetails = new ProblemDetails();
        problemDetails.setStatus(statusCode.value());
        problemDetails.setDetail(cause.getLocalizedMessage());
        return problemDetails;
    }

    private void logError(String message, Throwable cause) {
        if (logger.isDebugEnabled()) {
            logger.warn(message, cause);
        } else {
            logger.warn(String.format("%s: %s", message, cause.getMessage()));
        }
    }

}
