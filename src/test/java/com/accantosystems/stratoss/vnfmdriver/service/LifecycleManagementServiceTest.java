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
import com.fasterxml.jackson.databind.ObjectMapper;

public class LifecycleManagementServiceTest {

    @Test
    public void testExecuteLifecycle() throws Exception {
        final VNFLifecycleManagementDriver mockDriver = mock(VNFLifecycleManagementDriver.class);
        final ExternalMessagingService mockExternalMessagingService = mock(ExternalMessagingService.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        final MessageConversionService messageConversionService = new JavascriptMessageConversionServiceImpl(objectMapper);
        final LifecycleManagementService lifecycleManagementService = new LifecycleManagementService(mockDriver, messageConversionService, mockExternalMessagingService);

        when(mockDriver.createVnfInstance(any(), any())).thenReturn(loadFileIntoString("examples/VnfInstance.json"));

        final ExecutionRequest executionRequest = new ExecutionRequest();
        executionRequest.setLifecycleName("Install");
        executionRequest.setDeploymentLocation(TEST_DL_NO_AUTH);
        executionRequest.getProperties().put("vnfdId", "fa2343af-2a81-4e84-a667-e40662e5ed93");
        executionRequest.getProperties().put("vnfInstanceName", "CSCF-1");
        executionRequest.getProperties().put("additionalParams.vnfPkgId", "316aa140-c99a-4a08-b8f5-8e2cb73c83e8");
        executionRequest.getProperties().put("additionalParams.testProperty", "TestValue");
        // These properties should be ignored
        executionRequest.getProperties().put("property1", "value1");
        executionRequest.getProperties().put("property2", "value2");
        executionRequest.getProperties().put("property3", "value3");

        final ExecutionAcceptedResponse executionAcceptedResponse = lifecycleManagementService.executeLifecycle(executionRequest);

        assertThat(executionAcceptedResponse).isNotNull();
    }

}