package com.accantosystems.stratoss.vnfmdriver.driver;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withCreatedEntity;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

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

import com.accantosystems.stratoss.vnfmdriver.model.etsi.CreateVnfRequest;
import com.accantosystems.stratoss.vnfmdriver.model.etsi.VnfInstance;

@RunWith(SpringRunner.class)
@RestClientTest({VNFLifecycleManagementDriver.class, SOL003ResponseErrorHandler.class})
public class VNFLifecycleManagementDriverTest {

    @Autowired private VNFLifecycleManagementDriver driver;
    @Autowired private MockRestServiceServer server;
    @Rule public TestName testName = new TestName();

    @Test
    public void createVnfInstance() {
        server.expect(requestTo("http://localhost:8080/vnflcm/v1/vnf_instances"))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, "application/json"))
              .andRespond(withCreatedEntity(URI.create("http://localhost:8080/vnflcm/v1/vnf_instances/id")).body("{\"id\": \"TEST_ID\"}").contentType(MediaType.APPLICATION_JSON));

        final CreateVnfRequest createVnfRequest = new CreateVnfRequest();
        createVnfRequest.setVnfdId(UUID.randomUUID().toString());
        createVnfRequest.setVnfInstanceName(testName.getMethodName());

        final VnfInstance vnfInstance = driver.createVnfInstance(createVnfRequest);

        assertThat(vnfInstance).isNotNull();
        assertThat(vnfInstance.getId()).isEqualTo("TEST_ID");
    }

    @Test
    public void deleteVnfInstance() {
    }

    @Test
    public void queryAllLifecycleOperationOccurrences() {
    }

    @Test
    public void queryLifecycleOperationOccurrence() {
    }

    @Test
    public void createLifecycleSubscription() {
    }

    @Test
    public void queryAllLifecycleSubscriptions() {
    }

    @Test
    public void queryLifecycleSubscription() {
    }

    @Test
    public void deleteLifecycleSubscription() {
    }
}