package com.accantosystems.stratoss.vnfmdriver.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.accantosystems.stratoss.vnfmdriver.model.alm.ExecutionAsyncResponse;
import com.accantosystems.stratoss.vnfmdriver.service.ExternalMessagingService;

public class LoggingExternalMessagingServiceImpl implements ExternalMessagingService {

    private static final Logger logger = LoggerFactory.getLogger(LoggingExternalMessagingServiceImpl.class);

    @Override public void sendExecutionAsyncResponse(ExecutionAsyncResponse request) {
        logger.info("Would send: {}", request);
    }

}
