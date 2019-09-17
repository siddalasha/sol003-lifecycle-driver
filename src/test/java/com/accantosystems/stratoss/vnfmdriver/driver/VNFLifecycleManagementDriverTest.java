package com.accantosystems.stratoss.vnfmdriver.driver;

import static com.accantosystems.stratoss.vnfmdriver.test.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import java.net.URI;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClientException;

import com.accantosystems.stratoss.vnfmdriver.model.etsi.CreateVnfRequest;
import com.accantosystems.stratoss.vnfmdriver.model.etsi.VnfInstance;

@RunWith(SpringRunner.class)
@RestClientTest({VNFLifecycleManagementDriver.class, SOL003ResponseErrorHandler.class})
public class VNFLifecycleManagementDriverTest {

    private static final String SERVER_BASE_URL = "http://localhost:8080";
    private static final String INSTANCE_ENDPOINT = "/vnflcm/v1/vnf_instances";

    @Autowired private VNFLifecycleManagementDriver driver;
    @Autowired private MockRestServiceServer server;

    @Rule public TestName testName = new TestName();

    @Test
    public void testCreateVnfInstance() throws Exception {
        server.expect(requestTo(SERVER_BASE_URL + INSTANCE_ENDPOINT))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withCreatedEntity(URI.create(SERVER_BASE_URL + INSTANCE_ENDPOINT + "/TEST_ID"))
                                  .body(loadFileIntoString("examples/VnfInstance.json"))
                                  .contentType(MediaType.APPLICATION_JSON));

        final CreateVnfRequest createVnfRequest = new CreateVnfRequest();
        createVnfRequest.setVnfdId(UUID.randomUUID().toString());
        createVnfRequest.setVnfInstanceName(testName.getMethodName());

        final VnfInstance vnfInstance = driver.createVnfInstance(createVnfRequest);

        assertThat(vnfInstance).isNotNull();
        assertThat(vnfInstance.getId()).isEqualTo("TEST_ID");
    }

    @Test
    public void testCreateVnfInstanceWithProblemDetails() throws Exception {
        server.expect(requestTo(SERVER_BASE_URL + INSTANCE_ENDPOINT))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withServerError().body(loadFileIntoString("examples/ProblemDetails.json"))
                                           .contentType(MediaType.APPLICATION_JSON));

        final CreateVnfRequest createVnfRequest = new CreateVnfRequest();
        createVnfRequest.setVnfdId(UUID.randomUUID().toString());
        createVnfRequest.setVnfInstanceName(testName.getMethodName());

        assertThatThrownBy(() -> driver.createVnfInstance(createVnfRequest))
                .isInstanceOf(SOL003ResponseException.class);
    }

    @Test
    public void testCreateVnfInstanceWithUnknownException() {
        server.expect(requestTo(SERVER_BASE_URL + INSTANCE_ENDPOINT))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withServerError());

        final CreateVnfRequest createVnfRequest = new CreateVnfRequest();
        createVnfRequest.setVnfdId(UUID.randomUUID().toString());
        createVnfRequest.setVnfInstanceName(testName.getMethodName());

        assertThatThrownBy(() -> driver.createVnfInstance(createVnfRequest))
                .isInstanceOf(RestClientException.class);
    }

    @Test
    public void testCreateVnfInstanceWithInvalidSuccessCode() throws Exception {
        server.expect(requestTo(SERVER_BASE_URL + INSTANCE_ENDPOINT))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withSuccess().body(loadFileIntoString("examples/VnfInstance.json"))
                                       .contentType(MediaType.APPLICATION_JSON));

        final CreateVnfRequest createVnfRequest = new CreateVnfRequest();
        createVnfRequest.setVnfdId(UUID.randomUUID().toString());
        createVnfRequest.setVnfInstanceName(testName.getMethodName());

        assertThatThrownBy(() -> driver.createVnfInstance(createVnfRequest))
                .isInstanceOf(RestClientException.class);
    }

    @Test
    public void testDeleteVnfInstance() {
    }

    @Test
    public void testQueryAllLifecycleOperationOccurrences() {
    }

    @Test
    public void testQueryLifecycleOperationOccurrence() {
    }

    @Test
    public void testCreateLifecycleSubscription() {
    }

    @Test
    public void testQueryAllLifecycleSubscriptions() {
    }

    @Test
    public void testQueryLifecycleSubscription() {
    }

    @Test
    public void testDeleteLifecycleSubscription() {
    }

}