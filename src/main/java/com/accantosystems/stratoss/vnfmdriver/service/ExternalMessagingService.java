package com.accantosystems.stratoss.vnfmdriver.service;

import com.accantosystems.stratoss.vnfmdriver.model.alm.ExecutionAsyncResponse;

public interface ExternalMessagingService {

    void sendExecutionAsyncResponse(ExecutionAsyncResponse request);

}
