package com.accantosystems.stratoss.vnfmdriver.service;

import static com.accantosystems.stratoss.vnfmdriver.test.TestConstants.TEST_DL_NO_AUTH;
import static com.accantosystems.stratoss.vnfmdriver.test.TestConstants.loadFileIntoString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.Test;

import com.accantosystems.stratoss.vnfmdriver.config.VNFMDriverProperties;
import com.accantosystems.stratoss.vnfmdriver.driver.VNFLifecycleManagementDriver;
import com.accantosystems.stratoss.vnfmdriver.model.alm.ExecutionAcceptedResponse;
import com.accantosystems.stratoss.vnfmdriver.model.alm.ExecutionRequest;
import com.accantosystems.stratoss.vnfmdriver.model.alm.StringPropertyValue;
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
        final LifecycleManagementService lifecycleManagementService = new LifecycleManagementService(mockDriver, messageConversionService, mockExternalMessagingService, new VNFMDriverProperties());

        when(mockDriver.createVnfInstance(any(), any())).thenReturn(loadFileIntoString("examples/VnfInstance.json"));

        final ExecutionRequest executionRequest = new ExecutionRequest();
        executionRequest.setLifecycleName("Install");
        executionRequest.setDeploymentLocation(TEST_DL_NO_AUTH);
        executionRequest.getPropertiesAsPropertyValues().put("vnfdId", new StringPropertyValue("fa2343af-2a81-4e84-a667-e40662e5ed93"));
        executionRequest.getPropertiesAsPropertyValues().put("vnfInstanceName", new StringPropertyValue("CSCF-1"));
        executionRequest.getPropertiesAsPropertyValues().put("additionalParams.vnfPkgId", new StringPropertyValue("316aa140-c99a-4a08-b8f5-8e2cb73c83e8"));
        executionRequest.getPropertiesAsPropertyValues().put("additionalParams.testProperty", new StringPropertyValue("TestValue"));
        // These properties should be ignored
        executionRequest.getPropertiesAsPropertyValues().put("property1", new StringPropertyValue("value1"));
        executionRequest.getPropertiesAsPropertyValues().put("property2", new StringPropertyValue("value2"));
        executionRequest.getPropertiesAsPropertyValues().put("property3", new StringPropertyValue("value3"));

        final ExecutionAcceptedResponse executionAcceptedResponse = lifecycleManagementService.executeLifecycle(executionRequest);

        assertThat(executionAcceptedResponse).isNotNull();

        verify(mockExternalMessagingService).sendDelayedExecutionAsyncResponse(any(), any());
    }

    @Test
    public void testExecuteLifecycleInvalidLifecycleName() {
        final LifecycleManagementService lifecycleManagementService = new LifecycleManagementService(null, null, null, null);

        final ExecutionRequest executionRequest = new ExecutionRequest();
        executionRequest.setLifecycleName("Integrity");
        executionRequest.setDeploymentLocation(TEST_DL_NO_AUTH);

        assertThatThrownBy(() -> lifecycleManagementService.executeLifecycle(executionRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Requested transition [Integrity] is not supported by this lifecycle driver");
    }

}