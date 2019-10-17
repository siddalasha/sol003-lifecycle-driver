package com.accantosystems.stratoss.vnfmdriver.service.impl;

import static com.accantosystems.stratoss.vnfmdriver.test.TestConstants.TEST_DL_NO_AUTH;
import static com.accantosystems.stratoss.vnfmdriver.test.TestConstants.loadZipIntoBase64String;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.test.json.JsonContent;

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
        executionRequest.getProperties().put("vnfdId", "xyz-xyz-xyz-xyz");
        executionRequest.getProperties().put("vnfInstanceName", "Install");
        executionRequest.getProperties().put("vnfInstanceDescription", "testing testing 123");

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
        executionRequest.getProperties().put("vnfdId", "fa2343af-2a81-4e84-a667-e40662e5ed93");
        executionRequest.getProperties().put("vnfInstanceId", "0000-0002-0000-0001");
        executionRequest.getProperties().put("vnfInstanceName", "HelloWorld1");
        executionRequest.getProperties().put("vnfPkgId", "316aa140-c99a-4a08-b8f5-8e2cb73c83e8");
        executionRequest.getProperties().put("vnfProvider", "ACME");
        executionRequest.getProperties().put("vnfProductName", "ACME-Product");
        executionRequest.getProperties().put("vnfSoftwareVersion", "1.0");
        executionRequest.getProperties().put("vnfdVersion", "1.0");

        executionRequest.getProperties().put("flavourId", "Chocolate");
        executionRequest.getProperties().put("instantiationLevelId", "1");

        executionRequest.getProperties().put("extVirtualLinks.0.id", "45672c22-7c12-49ed-8a4f-e4532b3026fb");
        executionRequest.getProperties().put("extVirtualLinks.0.vimConnectionId", "f03a29d5-1fc6-11e9-83ea-fa163e045578");
        executionRequest.getProperties().put("extVirtualLinks.0.resourceId", "45672c22-5r54-49ed-8a4f-e4532b3026fb");

        executionRequest.getProperties().put("extVirtualLinks.0.extCps.0.cpdId", "SERVICES_ExtCp");
        executionRequest.getProperties().put("extVirtualLinks.0.extCps.0.cpConfig.0.cpInstanceId", "cpInstanceId111");
        executionRequest.getProperties().put("extVirtualLinks.0.extCps.0.cpConfig.0.linkPortId", "linkPortId222");
        executionRequest.getProperties().put("extVirtualLinks.0.extCps.0.cpConfig.0.cpProtocolData.0.layerProtocol", "IP_OVER_ETHERNET");
        executionRequest.getProperties().put("extVirtualLinks.0.extCps.0.cpConfig.0.cpProtocolData.0.ipOverEthernet.macAddress", "fa:16:3e:23:fd:d7");
        executionRequest.getProperties().put("extVirtualLinks.0.extCps.0.cpConfig.0.cpProtocolData.0.ipOverEthernet.ipAddresses.0.type", "IPV4");
        executionRequest.getProperties().put("extVirtualLinks.0.extCps.0.cpConfig.0.cpProtocolData.0.ipOverEthernet.ipAddresses.0.fixedAddresses.0", "131.160.162.32");
        executionRequest.getProperties().put("extVirtualLinks.0.extCps.0.cpConfig.0.cpProtocolData.0.ipOverEthernet.ipAddresses.0.numDynamicAddresses", "1");
        executionRequest.getProperties().put("extVirtualLinks.0.extCps.0.cpConfig.0.cpProtocolData.0.ipOverEthernet.ipAddresses.0.addressRange.minAddress", "131.160.162.32");
        executionRequest.getProperties().put("extVirtualLinks.0.extCps.0.cpConfig.0.cpProtocolData.0.ipOverEthernet.ipAddresses.0.addressRange.maxAddress", "131.160.162.36");
        executionRequest.getProperties().put("extVirtualLinks.0.extCps.0.cpConfig.0.cpProtocolData.0.ipOverEthernet.ipAddresses.0.subnetId", "");

        executionRequest.getProperties().put("extVirtualLinks.0.extLinkPorts.0.id", "id");
        executionRequest.getProperties().put("extVirtualLinks.0.extLinkPorts.0.resourceHandle.vimConnectionId", "47772c22-7c12-49ed-8a4f-e7625b3026fb");
        executionRequest.getProperties().put("extVirtualLinks.0.extLinkPorts.0.resourceHandle.resourceProviderId", "");
        executionRequest.getProperties().put("extVirtualLinks.0.extLinkPorts.0.resourceHandle.resourceId", "45672c22-5r54-49ed-8a4f-e4532b3026fb");
        executionRequest.getProperties().put("extVirtualLinks.0.extLinkPorts.0.resourceHandle.vimLevelResourceType", "");

        executionRequest.getProperties().put("vimConnectionInfo.0.id", "4408b119-eb54-11e7-bae0-fa163eb90b5c");
        executionRequest.getProperties().put("vimConnectionInfo.0.vimId", "vim1");
        executionRequest.getProperties().put("vimConnectionInfo.0.vimType", "Openstack");
        executionRequest.getProperties().put("vimConnectionInfo.0.interfaceInfo.identityEndPoint", "https://openstack:5000/v2.0");
        executionRequest.getProperties().put("vimConnectionInfo.0.accessInfo.projectId", "cab32f669c18404d8bed0fae6bf088aa");
        executionRequest.getProperties().put("vimConnectionInfo.0.accessInfo.credentials.username", "dummy");
        executionRequest.getProperties().put("vimConnectionInfo.0.accessInfo.credentials.password", "RXJpY3Nzb24uLjYyNA==");

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