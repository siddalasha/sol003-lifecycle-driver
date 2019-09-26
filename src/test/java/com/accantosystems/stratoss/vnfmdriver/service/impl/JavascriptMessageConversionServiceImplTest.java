package com.accantosystems.stratoss.vnfmdriver.service.impl;

import static com.accantosystems.stratoss.vnfmdriver.test.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.accantosystems.stratoss.vnfmdriver.model.alm.ExecutionRequest;
import com.accantosystems.stratoss.vnfmdriver.service.MessageConversionService;

public class JavascriptMessageConversionServiceImplTest {

    @Test
    public void testGenerateMessageFromRequestUsingProvidedScript() throws Exception {
        final ExecutionRequest executionRequest = new ExecutionRequest();
        executionRequest.setLifecycleName("Instantiate");
        executionRequest.setDeploymentLocation(TEST_DL_NO_AUTH);
        executionRequest.getProperties().put("description", "testing testing 123");
        executionRequest.setLifecycleScripts(loadZipIntoBase64String("examples/lifecyclescripts.zip"));

        final MessageConversionService messageConversionService = new JavascriptMessageConversionServiceImpl();
        final String message = messageConversionService.generateMessageFromRequest(executionRequest);

        assertThat(message).isEqualTo("{\"vnfdId\":\"123-123-123-123\",\"vnfInstanceName\":\"Instantiate\",\"vnfInstanceDescription\":\"testing testing 123\"}");
    }

    @Test
    public void testGenerateMessageFromRequestUsingSpecifiedVersion() throws Exception {
        final ExecutionRequest executionRequest = new ExecutionRequest();
        executionRequest.setLifecycleName("Instantiate");
        executionRequest.setDeploymentLocation(TEST_DL_NO_AUTH);
        executionRequest.getProperties().put("description", "testing testing 123");
        executionRequest.getProperties().put("interfaceVersion", "2.5.1");

        final MessageConversionService messageConversionService = new JavascriptMessageConversionServiceImpl();
        final String message = messageConversionService.generateMessageFromRequest(executionRequest);

        assertThat(message).isEqualTo("{\"vnfdId\":\"xxx-xxx-xxx-xxx\",\"vnfInstanceName\":\"Instantiate\",\"vnfInstanceDescription\":\"testing testing 123\"}");
    }

    @Test
    public void testGenerateMessageFromRequestUsingDefault() throws Exception {
        final ExecutionRequest executionRequest = new ExecutionRequest();
        executionRequest.setLifecycleName("Create");
        executionRequest.setDeploymentLocation(TEST_DL_NO_AUTH);
        executionRequest.getProperties().put("description", "testing testing 123");

        final MessageConversionService messageConversionService = new JavascriptMessageConversionServiceImpl();
        final String message = messageConversionService.generateMessageFromRequest(executionRequest);

        assertThat(message).isEqualTo("{\"vnfdId\":\"xyz-xyz-xyz-xyz\",\"vnfInstanceName\":\"Create\",\"vnfInstanceDescription\":\"testing testing 123\"}");
    }


}