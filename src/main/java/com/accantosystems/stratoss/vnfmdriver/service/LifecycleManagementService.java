package com.accantosystems.stratoss.vnfmdriver.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.accantosystems.stratoss.vnfmdriver.model.alm.ExecutionAcceptedResponse;
import com.accantosystems.stratoss.vnfmdriver.model.alm.ExecutionRequest;

@Service("LifecycleManagementService")
public class LifecycleManagementService {

    private final static Logger logger = LoggerFactory.getLogger(LifecycleManagementService.class);

    public ExecutionAcceptedResponse executeLifecycle(ExecutionRequest executionRequest) {
        logger.info("Processing execution request");
        throw new UnsupportedOperationException("Not implemented yet");
    }

}
