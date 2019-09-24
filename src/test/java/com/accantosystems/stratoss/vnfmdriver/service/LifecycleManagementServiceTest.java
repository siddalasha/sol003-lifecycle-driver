package com.accantosystems.stratoss.vnfmdriver.service;

import static com.accantosystems.stratoss.vnfmdriver.test.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.etsi.sol003.lifecyclemanagement.VnfInstance;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.accantosystems.stratoss.vnfmdriver.driver.VNFLifecycleManagementDriver;
import com.accantosystems.stratoss.vnfmdriver.model.alm.ExecutionAcceptedResponse;
import com.accantosystems.stratoss.vnfmdriver.model.alm.ExecutionRequest;
import com.accantosystems.stratoss.vnfmdriver.service.impl.JavascriptMessageConversionServiceImpl;

public class LifecycleManagementServiceTest {

    @Test
    public void testExecuteLifecycle() {
        final LifecycleManagementService lifecycleManagementService = new LifecycleManagementService(null, null);

        final ExecutionRequest executionRequest = new ExecutionRequest();
        executionRequest.setLifecycleName("Install");
        executionRequest.setDeploymentLocation(TEST_DL);

        final ExecutionAcceptedResponse executionAcceptedResponse = lifecycleManagementService.executeLifecycle(executionRequest);

        assertThat(executionAcceptedResponse).isNotNull();
    }

    @Test
    public void testExecuteLifecycleWithLifecycleScripts() throws IOException  {
        final MessageConversionService messageConversionService = new JavascriptMessageConversionServiceImpl();
        final VNFLifecycleManagementDriver vnfLifecycleManagementDriver = mock(VNFLifecycleManagementDriver.class);
        final LifecycleManagementService lifecycleManagementService = new LifecycleManagementService(messageConversionService, vnfLifecycleManagementDriver);

        final ExecutionRequest executionRequest = new ExecutionRequest();
        executionRequest.setLifecycleName("Install");
        executionRequest.setDeploymentLocation(TEST_DL);
        executionRequest.getProperties().put("description", "testing testing 123");
        executionRequest.getProperties().put("script", loadFileIntoString("scripts/2.3.1/CreateVnfRequest.js"));
        executionRequest.setLifecycleScripts(loadZipIntoBase64String("examples/lifecyclescripts.zip"));

        when(vnfLifecycleManagementDriver.createVnfInstance(any(), anyString())).thenAnswer(invocation -> {
            System.out.println("Received message for sending: " + invocation.getArgument(1).toString());
            return null;
        });

        final ExecutionAcceptedResponse executionAcceptedResponse = lifecycleManagementService.executeLifecycle(executionRequest);

        lifecycleManagementService.executeLifecycle(executionRequest);

        assertThat(executionAcceptedResponse).isNotNull();
    }

}