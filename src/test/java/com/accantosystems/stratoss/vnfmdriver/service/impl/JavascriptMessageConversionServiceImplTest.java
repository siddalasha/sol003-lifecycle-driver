package com.accantosystems.stratoss.vnfmdriver.service.impl;

import static com.accantosystems.stratoss.vnfmdriver.test.TestConstants.TEST_DL_NO_AUTH;
import static com.accantosystems.stratoss.vnfmdriver.test.TestConstants.loadZipIntoBase64String;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;

import com.accantosystems.stratoss.vnfmdriver.model.alm.ExecutionRequest;
import com.accantosystems.stratoss.vnfmdriver.service.MessageConversionService;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JavascriptMessageConversionServiceImplTest {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    @BeforeClass
    public static void setUpClass() {
        objectMapper.findAndRegisterModules();
    }

    @Test
    public void testGenerateMessageFromRequestUsingProvidedScript() throws Exception {
        final ExecutionRequest executionRequest = new ExecutionRequest();
        executionRequest.setLifecycleName("Configure");
        executionRequest.setDeploymentLocation(TEST_DL_NO_AUTH);
        executionRequest.getProperties().put("description", "testing testing 123");
        executionRequest.setLifecycleScripts(loadZipIntoBase64String("examples/lifecyclescripts.zip"));

        final MessageConversionService messageConversionService = new JavascriptMessageConversionServiceImpl(objectMapper);
        final String message = messageConversionService.generateMessageFromRequest("InstantiateVnfRequest", executionRequest);

        assertThat(message).isEqualTo("{\"vnfdId\":\"123-123-123-123\",\"vnfInstanceName\":\"Configure\",\"vnfInstanceDescription\":\"testing testing 123\"}");
    }

    @Test
    public void testGenerateMessageFromRequestUsingSpecifiedVersion() throws Exception {
        final ExecutionRequest executionRequest = new ExecutionRequest();
        executionRequest.setLifecycleName("Configure");
        executionRequest.setDeploymentLocation(TEST_DL_NO_AUTH);
        executionRequest.getProperties().put("description", "testing testing 123");
        executionRequest.getProperties().put("interfaceVersion", "2.5.1");

        final MessageConversionService messageConversionService = new JavascriptMessageConversionServiceImpl(objectMapper);
        final String message = messageConversionService.generateMessageFromRequest("InstantiateVnfRequest", executionRequest);

        assertThat(message).isEqualTo("{\"vnfdId\":\"xxx-xxx-xxx-xxx\",\"vnfInstanceName\":\"Configure\",\"vnfInstanceDescription\":\"testing testing 123\"}");
    }

    @Test
    public void testGenerateMessageFromRequestUsingDefault() throws Exception {
        final ExecutionRequest executionRequest = new ExecutionRequest();
        executionRequest.setLifecycleName("Install");
        executionRequest.setDeploymentLocation(TEST_DL_NO_AUTH);
        executionRequest.getProperties().put("description", "testing testing 123");

        final MessageConversionService messageConversionService = new JavascriptMessageConversionServiceImpl(objectMapper);
        final String message = messageConversionService.generateMessageFromRequest("CreateVnfRequest", executionRequest);

        assertThat(message).isEqualTo("{\"vnfdId\":\"xyz-xyz-xyz-xyz\",\"vnfInstanceName\":\"Install\",\"vnfInstanceDescription\":\"testing testing 123\"}");
    }

}