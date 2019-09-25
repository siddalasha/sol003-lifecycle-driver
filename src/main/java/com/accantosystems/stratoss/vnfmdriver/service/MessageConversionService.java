package com.accantosystems.stratoss.vnfmdriver.service;

import com.accantosystems.stratoss.vnfmdriver.model.alm.ExecutionRequest;

public interface MessageConversionService {

    String generateMessageFromRequest(ExecutionRequest executionRequest) throws MessageConversionException;

}
