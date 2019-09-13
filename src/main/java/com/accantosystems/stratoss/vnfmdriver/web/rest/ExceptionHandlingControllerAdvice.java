package com.accantosystems.stratoss.vnfmdriver.web.rest;

import java.util.HashMap;
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

import com.accantosystems.stratoss.vnfmdriver.model.web.ErrorInfo;

/**
 * Handles the conversion of Exceptions thrown by Rest API calls
 */
@RestControllerAdvice
public class ExceptionHandlingControllerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlingControllerAdvice.class);

    protected ErrorInfo buildBasicErrorInfoObject(final HttpServletRequest req, final Exception cause) {
        Throwable rootCause = cause;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        final HashMap<String, Object> details = new HashMap<>();
        if (rootCause != cause) {
            details.put("rootCause", rootCause.getLocalizedMessage());
        }
        return new ErrorInfo(req.getRequestURL().toString(), cause.getLocalizedMessage(), details);
    }

    protected void logError(String message, Throwable cause) {
        if (logger.isDebugEnabled()) {
            logger.warn(message, cause);
        } else {
            logger.warn(String.format("%s: %s", message, cause.getMessage()));
        }
    }

    /**
     * The default mechanism for handling an error is to:
     * <ul>
     * <li>Log the error</li>
     * <li>Return an ErrorInfo object describing the error</li>
     * </ul>
     *
     * @param message
     * @param req
     * @param cause
     * @return
     */
    protected ErrorInfo defaultHandle(String message, HttpServletRequest req, Exception cause) {
        logError(message, cause);
        return buildBasicErrorInfoObject(req, cause);
    }

    /**
     * The default mechanism for handling an error is to:
     * <ul>
     * <li>Return an ErrorInfo object describing the error</li>
     * </ul>
     *
     * @param req
     * @param cause
     * @return
     */
    protected ErrorInfo defaultHandle(HttpServletRequest req, Exception cause) {
        return buildBasicErrorInfoObject(req, cause);
    }

    @ExceptionHandler(HttpMessageConversionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    protected ErrorInfo handleHttpMessageConversionExceptions(HttpServletRequest req, HttpMessageConversionException cause) {
        return defaultHandle("Unable to parse request", req, cause);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    protected ErrorInfo handleMessageNotReadableExceptions(HttpServletRequest req, HttpMessageNotReadableException cause) {
        return defaultHandle("Invalid content provided to request", req, cause);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ResponseBody
    protected ErrorInfo handleMediaTypeNotSupportedExceptions(HttpServletRequest req, HttpMediaTypeNotSupportedException cause) {
        return defaultHandle("Invalid content provided to request", req, cause);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ResponseBody
    protected ErrorInfo handleMissingServletRequestParameterException(HttpServletRequest req, HttpMediaTypeNotAcceptableException cause) {
        return defaultHandle("Invalid content provided to request", req, cause);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ResponseBody
    protected ErrorInfo handleRequestMethodNotSupportedExceptions(HttpServletRequest req, HttpRequestMethodNotSupportedException cause) {
        return defaultHandle("Invalid method used in request", req, cause);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    protected ErrorInfo handleMissingServletRequestParameterException(HttpServletRequest req, MissingServletRequestParameterException cause) {
        return defaultHandle(cause.getLocalizedMessage(), req, cause);
    }
}
