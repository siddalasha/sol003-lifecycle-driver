package com.accantosystems.stratoss.vnfmdriver.service;

import static com.accantosystems.stratoss.vnfmdriver.test.TestConstants.TEST_DL;
import static com.accantosystems.stratoss.vnfmdriver.test.TestConstants.loadZipIntoBase64String;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.accantosystems.stratoss.vnfmdriver.model.alm.ExecutionAcceptedResponse;
import com.accantosystems.stratoss.vnfmdriver.model.alm.ExecutionRequest;

public class LifecycleManagementServiceTest {

    @Test
    public void testExecuteLifecycle() {
        final LifecycleManagementService lifecycleManagementService = new LifecycleManagementService();

        final ExecutionRequest executionRequest = new ExecutionRequest();
        executionRequest.setLifecycleName("Install");
        executionRequest.setDeploymentLocation(TEST_DL);

        final ExecutionAcceptedResponse executionAcceptedResponse = lifecycleManagementService.executeLifecycle(executionRequest);

        assertThat(executionAcceptedResponse).isNotNull();
    }

    @Test
    public void testExecuteLifecycleWithLifecycleScripts() throws IOException  {
        final LifecycleManagementService lifecycleManagementService = new LifecycleManagementService();

        final ExecutionRequest executionRequest = new ExecutionRequest();
        executionRequest.setLifecycleName("Install");
        executionRequest.setDeploymentLocation(TEST_DL);
        executionRequest.setLifecycleScripts(loadZipIntoBase64String("examples/lifecyclescripts.zip"));

        final ExecutionAcceptedResponse executionAcceptedResponse = lifecycleManagementService.executeLifecycle(executionRequest);

        assertThat(executionAcceptedResponse).isNotNull();
    }

}