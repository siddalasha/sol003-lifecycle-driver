package com.accantosystems.stratoss.vnfmdriver.service;

import java.time.Duration;

import com.accantosystems.stratoss.vnfmdriver.model.alm.ExecutionAsyncResponse;

public interface ExternalMessagingService {

    void sendExecutionAsyncResponse(ExecutionAsyncResponse request);

    void sendDelayedExecutionAsyncResponse(ExecutionAsyncResponse request, Duration delay);

}
