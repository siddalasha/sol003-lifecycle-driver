package com.accantosystems.stratoss.vnfmdriver.service;

import org.etsi.sol003.lifecyclemanagement.CreateVnfRequest;

import com.accantosystems.stratoss.vnfmdriver.model.alm.ExecutionRequest;

public interface MessageConversionService {

    String generateMessageFromRequest(ExecutionRequest executionRequest, String script) throws MessageConversionException;

}
