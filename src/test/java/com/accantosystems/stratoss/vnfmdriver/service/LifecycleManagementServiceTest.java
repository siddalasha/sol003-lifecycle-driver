package com.accantosystems.stratoss.vnfmdriver.service;

import static com.accantosystems.stratoss.vnfmdriver.test.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.accantosystems.stratoss.vnfmdriver.driver.VNFLifecycleManagementDriver;
import com.accantosystems.stratoss.vnfmdriver.model.alm.ExecutionAcceptedResponse;
import com.accantosystems.stratoss.vnfmdriver.model.alm.ExecutionRequest;
import com.accantosystems.stratoss.vnfmdriver.service.impl.JavascriptMessageConversionServiceImpl;

public class LifecycleManagementServiceTest {

    @Test
    public void testExecuteLifecycle() throws Exception {
        final VNFLifecycleManagementDriver mockDriver = mock(VNFLifecycleManagementDriver.class);
        final ExternalMessagingService mockExternalMessagingService = mock(ExternalMessagingService.class);
        final MessageConversionService messageConversionService = new JavascriptMessageConversionServiceImpl();
        final LifecycleManagementService lifecycleManagementService = new LifecycleManagementService(mockDriver, messageConversionService, mockExternalMessagingService);

        when(mockDriver.createVnfInstance(any(), any())).thenReturn(loadFileIntoString("examples/VnfInstance.json"));

        final ExecutionRequest executionRequest = new ExecutionRequest();
        executionRequest.setLifecycleName("Install");
        executionRequest.setDeploymentLocation(TEST_DL_NO_AUTH);

        final ExecutionAcceptedResponse executionAcceptedResponse = lifecycleManagementService.executeLifecycle(executionRequest);

        assertThat(executionAcceptedResponse).isNotNull();
    }

}