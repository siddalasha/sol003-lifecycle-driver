package com.accantosystems.stratoss.vnfmdriver.service.impl;

import static com.accantosystems.stratoss.vnfmdriver.test.TestConstants.TEST_DL_NO_AUTH;
import static com.accantosystems.stratoss.vnfmdriver.test.TestConstants.loadZipIntoBase64String;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.test.json.JsonContent;

import com.accantosystems.stratoss.vnfmdriver.model.alm.ExecutionRequest;
import com.accantosystems.stratoss.vnfmdriver.model.alm.GenericExecutionRequestPropertyValue;
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
        executionRequest.getResourceProperties().put("description", new GenericExecutionRequestPropertyValue("testing testing 123"));
        executionRequest.setDriverFiles(loadZipIntoBase64String("examples/lifecyclescripts.zip"));

        final MessageConversionService messageConversionService = new JavascriptMessageConversionServiceImpl(objectMapper);
        final String message = messageConversionService.generateMessageFromRequest("InstantiateVnfRequest", executionRequest);

        assertThat(message).isEqualTo("{\"vnfdId\":\"123-123-123-123\",\"vnfInstanceName\":\"Configure\",\"vnfInstanceDescription\":\"testing testing 123\"}");
    }

    @Test
    public void testGenerateMessageFromRequestUsingSpecifiedVersion() throws Exception {
        final ExecutionRequest executionRequest = new ExecutionRequest();
        executionRequest.setLifecycleName("Configure");
        executionRequest.setDeploymentLocation(TEST_DL_NO_AUTH);
        executionRequest.getResourceProperties().put("description", new GenericExecutionRequestPropertyValue("testing testing 123"));
        executionRequest.getResourceProperties().put("interfaceVersion", new GenericExecutionRequestPropertyValue("2.5.1"));

        final MessageConversionService messageConversionService = new JavascriptMessageConversionServiceImpl(objectMapper);
        final String message = messageConversionService.generateMessageFromRequest("InstantiateVnfRequest", executionRequest);

        assertThat(message).isEqualTo("{\"vnfdId\":\"xxx-xxx-xxx-xxx\",\"vnfInstanceName\":\"Configure\",\"vnfInstanceDescription\":\"testing testing 123\"}");
    }

    @Test
    public void testGenerateMessageFromRequestUsingDefault() throws Exception {
        final ExecutionRequest executionRequest = new ExecutionRequest();
        executionRequest.setLifecycleName("Install");
        executionRequest.setDeploymentLocation(TEST_DL_NO_AUTH);
        executionRequest.getResourceProperties().put("vnfdId", new GenericExecutionRequestPropertyValue("xyz-xyz-xyz-xyz"));
        executionRequest.getResourceProperties().put("vnfInstanceName", new GenericExecutionRequestPropertyValue("Install"));
        executionRequest.getResourceProperties().put("vnfInstanceDescription", new GenericExecutionRequestPropertyValue("testing testing 123"));

        final MessageConversionService messageConversionService = new JavascriptMessageConversionServiceImpl(objectMapper);
        final String message = messageConversionService.generateMessageFromRequest("CreateVnfRequest", executionRequest);

        assertThat(message).isEqualTo("{\"additionalParams\":{},\"vnfdId\":\"xyz-xyz-xyz-xyz\",\"vnfInstanceName\":\"Install\",\"vnfInstanceDescription\":\"testing testing 123\"}");
    }

    @Test
    public void testNoScriptFound() {
        final ExecutionRequest executionRequest = new ExecutionRequest();
        final MessageConversionService messageConversionService = new JavascriptMessageConversionServiceImpl(objectMapper);
        assertThatThrownBy(() -> messageConversionService.generateMessageFromRequest("UnknownMessageType", executionRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to find a script called [UnknownMessageType.js]");
    }

    @Test
    public void testGenerateInstantiateMessage() throws Exception {
        final ExecutionRequest executionRequest = new ExecutionRequest();
        executionRequest.setLifecycleName("Configure");
        executionRequest.setDeploymentLocation(TEST_DL_NO_AUTH);
        executionRequest.getResourceProperties().put("vnfdId", new GenericExecutionRequestPropertyValue("fa2343af-2a81-4e84-a667-e40662e5ed93"));
        executionRequest.getResourceProperties().put("vnfInstanceId", new GenericExecutionRequestPropertyValue("0000-0002-0000-0001"));
        executionRequest.getResourceProperties().put("vnfInstanceName", new GenericExecutionRequestPropertyValue("HelloWorld1"));
        executionRequest.getResourceProperties().put("vnfPkgId", new GenericExecutionRequestPropertyValue("316aa140-c99a-4a08-b8f5-8e2cb73c83e8"));
        executionRequest.getResourceProperties().put("vnfProvider", new GenericExecutionRequestPropertyValue("ACME"));
        executionRequest.getResourceProperties().put("vnfProductName", new GenericExecutionRequestPropertyValue("ACME-Product"));
        executionRequest.getResourceProperties().put("vnfSoftwareVersion", new GenericExecutionRequestPropertyValue("1.0"));
        executionRequest.getResourceProperties().put("vnfdVersion", new GenericExecutionRequestPropertyValue("1.0"));

        executionRequest.getResourceProperties().put("flavourId", new GenericExecutionRequestPropertyValue("Chocolate"));
        executionRequest.getResourceProperties().put("instantiationLevelId", new GenericExecutionRequestPropertyValue("1"));

        executionRequest.getResourceProperties().put("extVirtualLinks.0.id", new GenericExecutionRequestPropertyValue("45672c22-7c12-49ed-8a4f-e4532b3026fb"));
        executionRequest.getResourceProperties().put("extVirtualLinks.0.vimConnectionId", new GenericExecutionRequestPropertyValue("f03a29d5-1fc6-11e9-83ea-fa163e045578"));
        executionRequest.getResourceProperties().put("extVirtualLinks.0.resourceId", new GenericExecutionRequestPropertyValue("45672c22-5r54-49ed-8a4f-e4532b3026fb"));

        executionRequest.getResourceProperties().put("extVirtualLinks.0.extCps.0.cpdId", new GenericExecutionRequestPropertyValue("SERVICES_ExtCp"));
        executionRequest.getResourceProperties().put("extVirtualLinks.0.extCps.0.cpConfig.0.cpInstanceId", new GenericExecutionRequestPropertyValue("cpInstanceId111"));
        executionRequest.getResourceProperties().put("extVirtualLinks.0.extCps.0.cpConfig.0.linkPortId", new GenericExecutionRequestPropertyValue("linkPortId222"));
        executionRequest.getResourceProperties().put("extVirtualLinks.0.extCps.0.cpConfig.0.cpProtocolData.0.layerProtocol", new GenericExecutionRequestPropertyValue("IP_OVER_ETHERNET"));
        executionRequest.getResourceProperties().put("extVirtualLinks.0.extCps.0.cpConfig.0.cpProtocolData.0.ipOverEthernet.macAddress", new GenericExecutionRequestPropertyValue("fa:16:3e:23:fd:d7"));
        executionRequest.getResourceProperties().put("extVirtualLinks.0.extCps.0.cpConfig.0.cpProtocolData.0.ipOverEthernet.ipAddresses.0.type", new GenericExecutionRequestPropertyValue("IPV4"));
        executionRequest.getResourceProperties().put("extVirtualLinks.0.extCps.0.cpConfig.0.cpProtocolData.0.ipOverEthernet.ipAddresses.0.fixedAddresses.0", new GenericExecutionRequestPropertyValue("131.160.162.32"));
        executionRequest.getResourceProperties().put("extVirtualLinks.0.extCps.0.cpConfig.0.cpProtocolData.0.ipOverEthernet.ipAddresses.0.numDynamicAddresses", new GenericExecutionRequestPropertyValue("1"));
        executionRequest.getResourceProperties().put("extVirtualLinks.0.extCps.0.cpConfig.0.cpProtocolData.0.ipOverEthernet.ipAddresses.0.addressRange.minAddress", new GenericExecutionRequestPropertyValue("131.160.162.32"));
        executionRequest.getResourceProperties().put("extVirtualLinks.0.extCps.0.cpConfig.0.cpProtocolData.0.ipOverEthernet.ipAddresses.0.addressRange.maxAddress", new GenericExecutionRequestPropertyValue("131.160.162.36"));
        executionRequest.getResourceProperties().put("extVirtualLinks.0.extCps.0.cpConfig.0.cpProtocolData.0.ipOverEthernet.ipAddresses.0.subnetId", new GenericExecutionRequestPropertyValue(""));

        executionRequest.getResourceProperties().put("extVirtualLinks.0.extLinkPorts.0.id", new GenericExecutionRequestPropertyValue("id"));
        executionRequest.getResourceProperties().put("extVirtualLinks.0.extLinkPorts.0.resourceHandle.vimConnectionId", new GenericExecutionRequestPropertyValue("47772c22-7c12-49ed-8a4f-e7625b3026fb"));
        executionRequest.getResourceProperties().put("extVirtualLinks.0.extLinkPorts.0.resourceHandle.resourceProviderId", new GenericExecutionRequestPropertyValue(""));
        executionRequest.getResourceProperties().put("extVirtualLinks.0.extLinkPorts.0.resourceHandle.resourceId", new GenericExecutionRequestPropertyValue("45672c22-5r54-49ed-8a4f-e4532b3026fb"));
        executionRequest.getResourceProperties().put("extVirtualLinks.0.extLinkPorts.0.resourceHandle.vimLevelResourceType", new GenericExecutionRequestPropertyValue(""));

        executionRequest.getResourceProperties().put("vimConnectionInfo.0.id", new GenericExecutionRequestPropertyValue("4408b119-eb54-11e7-bae0-fa163eb90b5c"));
        executionRequest.getResourceProperties().put("vimConnectionInfo.0.vimId", new GenericExecutionRequestPropertyValue("vim1"));
        executionRequest.getResourceProperties().put("vimConnectionInfo.0.vimType", new GenericExecutionRequestPropertyValue("Openstack"));
        executionRequest.getResourceProperties().put("vimConnectionInfo.0.interfaceInfo.identityEndPoint", new GenericExecutionRequestPropertyValue("https://openstack:5000/v2.0"));
        executionRequest.getResourceProperties().put("vimConnectionInfo.0.accessInfo.projectId", new GenericExecutionRequestPropertyValue("cab32f669c18404d8bed0fae6bf088aa"));
        executionRequest.getResourceProperties().put("vimConnectionInfo.0.accessInfo.credentials.username", new GenericExecutionRequestPropertyValue("dummy"));
        executionRequest.getResourceProperties().put("vimConnectionInfo.0.accessInfo.credentials.password", new GenericExecutionRequestPropertyValue("RXJpY3Nzb24uLjYyNA=="));

        final MessageConversionService messageConversionService = new JavascriptMessageConversionServiceImpl(objectMapper);
        final String message = messageConversionService.generateMessageFromRequest("InstantiateVnfRequest", executionRequest);

        assertThat(new JsonContent<>(JavascriptMessageConversionServiceImplTest.class, null, message)).isEqualToJson("{\n"
                                                                                                                             + "  \"flavourId\": \"Chocolate\",\n"
                                                                                                                             + "  \"instantiationLevelId\": \"1\",\n"
                                                                                                                             + "  \"extVirtualLinks\": [\n"
                                                                                                                             + "    {\n"
                                                                                                                             + "      \"id\": \"45672c22-7c12-49ed-8a4f-e4532b3026fb\",\n"
                                                                                                                             + "      \"vimConnectionId\": \"f03a29d5-1fc6-11e9-83ea-fa163e045578\",\n"
                                                                                                                             + "      \"resourceId\": \"45672c22-5r54-49ed-8a4f-e4532b3026fb\",\n"
                                                                                                                             + "      \"extCps\": [\n"
                                                                                                                             + "        {\n"
                                                                                                                             + "          \"cpdId\": \"SERVICES_ExtCp\",\n"
                                                                                                                             + "          \"cpConfig\": [\n"
                                                                                                                             + "            {\n"
                                                                                                                             + "              \"cpInstanceId\": \"cpInstanceId111\",\n"
                                                                                                                             + "              \"linkPortId\": \"linkPortId222\",\n"
                                                                                                                             + "              \"cpProtocolData\": [\n"
                                                                                                                             + "                {\n"
                                                                                                                             + "                  \"layerProtocol\": \"IP_OVER_ETHERNET\",\n"
                                                                                                                             + "                  \"ipOverEthernet\": {\n"
                                                                                                                             + "                    \"macAddress\": \"fa:16:3e:23:fd:d7\",\n"
                                                                                                                             + "                    \"ipAddresses\": [\n"
                                                                                                                             + "                      {\n"
                                                                                                                             + "                        \"type\": \"IPV4\",\n"
                                                                                                                             + "                        \"fixedAddresses\": [\n"
                                                                                                                             + "                          \"131.160.162.32\"\n"
                                                                                                                             + "                        ],\n"
                                                                                                                             + "                        \"numDynamicAddresses\": \"1\",\n"
                                                                                                                             + "                        \"addressRange\": {\n"
                                                                                                                             + "                          \"minAddress\": \"131.160.162.32\",\n"
                                                                                                                             + "                          \"maxAddress\": \"131.160.162.36\"\n"
                                                                                                                             + "                        },\n"
                                                                                                                             + "                        \"subnetId\": \"\"\n"
                                                                                                                             + "                      }\n"
                                                                                                                             + "                    ]\n"
                                                                                                                             + "                  }\n"
                                                                                                                             + "                }\n"
                                                                                                                             + "              ]\n"
                                                                                                                             + "            }\n"
                                                                                                                             + "          ]\n"
                                                                                                                             + "        }\n"
                                                                                                                             + "      ],\n"
                                                                                                                             + "      \"extLinkPorts\": [\n"
                                                                                                                             + "        {\n"
                                                                                                                             + "          \"id\": \"id\",\n"
                                                                                                                             + "          \"resourceHandle\": {\n"
                                                                                                                             + "            \"vimConnectionId\": \"47772c22-7c12-49ed-8a4f-e7625b3026fb\",\n"
                                                                                                                             + "            \"resourceProviderId\": \"\",\n"
                                                                                                                             + "            \"resourceId\": \"45672c22-5r54-49ed-8a4f-e4532b3026fb\",\n"
                                                                                                                             + "            \"vimLevelResourceType\": \"\"\n"
                                                                                                                             + "          }\n"
                                                                                                                             + "        }\n"
                                                                                                                             + "      ]\n"
                                                                                                                             + "    }\n"
                                                                                                                             + "  ],\n"
                                                                                                                             + "  \"vimConnectionInfo\": [\n"
                                                                                                                             + "    {\n"
                                                                                                                             + "      \"id\": \"4408b119-eb54-11e7-bae0-fa163eb90b5c\",\n"
                                                                                                                             + "      \"vimId\": \"vim1\",\n"
                                                                                                                             + "      \"vimType\": \"Openstack\",\n"
                                                                                                                             + "      \"interfaceInfo\": {\n"
                                                                                                                             + "        \"identityEndPoint\": \"https://openstack:5000/v2.0\"\n"
                                                                                                                             + "      },\n"
                                                                                                                             + "      \"accessInfo\": {\n"
                                                                                                                             + "        \"projectId\": \"cab32f669c18404d8bed0fae6bf088aa\",\n"
                                                                                                                             + "        \"credentials\": {\n"
                                                                                                                             + "          \"username\": \"dummy\",\n"
                                                                                                                             + "          \"password\": \"RXJpY3Nzb24uLjYyNA==\"\n"
                                                                                                                             + "        }\n"
                                                                                                                             + "      }\n"
                                                                                                                             + "    }\n"
                                                                                                                             + "  ]\n"
                                                                                                                             + "}");
    }

}