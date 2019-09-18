package com.accantosystems.stratoss.vnfmdriver.driver;

import static com.accantosystems.stratoss.vnfmdriver.test.TestConstants.*;
import static org.assertj.core.api.Assertions.*;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;

import com.accantosystems.stratoss.vnfmdriver.model.etsi.CreateVnfRequest;
import com.accantosystems.stratoss.vnfmdriver.model.etsi.VnfInstance;

@RunWith(SpringRunner.class)
@RestClientTest({ VNFLifecycleManagementDriver.class, SOL003ResponseErrorHandler.class })
public class VNFLifecycleManagementDriverTest {

    private static final String INSTANCE_ENDPOINT = "/vnflcm/v1/vnf_instances";

    @Autowired private VNFLifecycleManagementDriver driver;
    @Autowired private MockRestServiceServer server;

    @Rule public TestName testName = new TestName();

    @Test
    public void testCreateVnfInstance() throws Exception {
        server.expect(requestTo(TEST_SERVER_BASE_URL + INSTANCE_ENDPOINT))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withCreatedEntity(URI.create(TEST_SERVER_BASE_URL + INSTANCE_ENDPOINT + "/TEST_ID"))
                                  .body(loadFileIntoString("examples/VnfInstance.json"))
                                  .contentType(MediaType.APPLICATION_JSON));

        final CreateVnfRequest createVnfRequest = new CreateVnfRequest();
        createVnfRequest.setVnfdId(UUID.randomUUID().toString());
        createVnfRequest.setVnfInstanceName(testName.getMethodName());

        final VnfInstance vnfInstance = driver.createVnfInstance(VNFM_CONNECTION_DETAILS_NO_AUTHENTICATION, createVnfRequest);

        assertThat(vnfInstance).isNotNull();
        assertThat(vnfInstance.getId()).isEqualTo(TEST_VNF_INSTANCE_ID);
    }

    @Test
    public void testCreateVnfInstanceWithBasicAuth() throws Exception {
        server.expect(requestTo(SECURE_TEST_SERVER_BASE_URL + INSTANCE_ENDPOINT))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andExpect(header(HttpHeaders.AUTHORIZATION, BASIC_AUTHORIZATION_HEADER))
              .andRespond(withCreatedEntity(URI.create(SECURE_TEST_SERVER_BASE_URL + INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID))
                                  .body(loadFileIntoString("examples/VnfInstance.json"))
                                  .contentType(MediaType.APPLICATION_JSON));

        final CreateVnfRequest createVnfRequest = new CreateVnfRequest();
        createVnfRequest.setVnfdId(UUID.randomUUID().toString());
        createVnfRequest.setVnfInstanceName(testName.getMethodName());

        final VnfInstance vnfInstance = driver.createVnfInstance(VNFM_CONNECTION_DETAILS_BASIC_AUTHENTICATION, createVnfRequest);

        assertThat(vnfInstance).isNotNull();
        assertThat(vnfInstance.getId()).isEqualTo(TEST_VNF_INSTANCE_ID);
    }

    @Test
    public void testCreateVnfInstanceWithProblemDetails() throws Exception {
        server.expect(requestTo(TEST_SERVER_BASE_URL + INSTANCE_ENDPOINT))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withServerError().body(loadFileIntoString("examples/ProblemDetails.json"))
                                           .contentType(MediaType.APPLICATION_JSON));

        final CreateVnfRequest createVnfRequest = new CreateVnfRequest();
        createVnfRequest.setVnfdId(UUID.randomUUID().toString());
        createVnfRequest.setVnfInstanceName(testName.getMethodName());

        SOL003ResponseException exception = catchThrowableOfType(() -> driver.createVnfInstance(VNFM_CONNECTION_DETAILS_NO_AUTHENTICATION, createVnfRequest), SOL003ResponseException.class);

        assertThat(exception.getProblemDetails()).isNotNull();
        assertThat(exception.getProblemDetails().getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(exception.getProblemDetails().getDetail()).isEqualTo("An error has occurred");
    }

    @Test
    public void testCreateVnfInstanceWithUnknownException() {
        server.expect(requestTo(TEST_SERVER_BASE_URL + INSTANCE_ENDPOINT))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withServerError());

        final CreateVnfRequest createVnfRequest = new CreateVnfRequest();
        createVnfRequest.setVnfdId(UUID.randomUUID().toString());
        createVnfRequest.setVnfInstanceName(testName.getMethodName());

        SOL003ResponseException exception = catchThrowableOfType(() -> driver.createVnfInstance(VNFM_CONNECTION_DETAILS_NO_AUTHENTICATION, createVnfRequest), SOL003ResponseException.class);

        assertThat(exception.getProblemDetails()).isNotNull();
        assertThat(exception.getProblemDetails().getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(exception.getProblemDetails().getDetail()).isEqualTo("Internal Server Error");
    }

    @Test
    public void testCreateVnfInstanceWithUnknownExceptionAndBody() {
        server.expect(requestTo(TEST_SERVER_BASE_URL + INSTANCE_ENDPOINT))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withServerError().body("This message should also appear"));

        final CreateVnfRequest createVnfRequest = new CreateVnfRequest();
        createVnfRequest.setVnfdId(UUID.randomUUID().toString());
        createVnfRequest.setVnfInstanceName(testName.getMethodName());

        SOL003ResponseException exception = catchThrowableOfType(() -> driver.createVnfInstance(VNFM_CONNECTION_DETAILS_NO_AUTHENTICATION, createVnfRequest), SOL003ResponseException.class);

        assertThat(exception.getProblemDetails()).isNotNull();
        assertThat(exception.getProblemDetails().getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(exception.getProblemDetails().getDetail()).isEqualTo("Internal Server Error: This message should also appear");
    }

    @Test
    public void testCreateVnfInstanceWithInvalidSuccessCode() throws Exception {
        server.expect(requestTo(TEST_SERVER_BASE_URL + INSTANCE_ENDPOINT))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withSuccess().body(loadFileIntoString("examples/VnfInstance.json"))
                                       .contentType(MediaType.APPLICATION_JSON));

        final CreateVnfRequest createVnfRequest = new CreateVnfRequest();
        createVnfRequest.setVnfdId(UUID.randomUUID().toString());
        createVnfRequest.setVnfInstanceName(testName.getMethodName());

        final VnfInstance vnfInstance = driver.createVnfInstance(VNFM_CONNECTION_DETAILS_NO_AUTHENTICATION, createVnfRequest);

        assertThat(vnfInstance).isNotNull();
        assertThat(vnfInstance.getId()).isEqualTo(TEST_VNF_INSTANCE_ID);
    }

    @Test
    public void testCreateVnfInstanceWithInvalidResponseCode() {
        server.expect(requestTo(TEST_SERVER_BASE_URL + INSTANCE_ENDPOINT))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withStatus(HttpStatus.MOVED_PERMANENTLY));

        final CreateVnfRequest createVnfRequest = new CreateVnfRequest();
        createVnfRequest.setVnfdId(UUID.randomUUID().toString());
        createVnfRequest.setVnfInstanceName(testName.getMethodName());

        assertThatThrownBy(() -> driver.createVnfInstance(VNFM_CONNECTION_DETAILS_NO_AUTHENTICATION, createVnfRequest))
                .isInstanceOf(SOL003ResponseException.class)
                .hasMessage("Invalid status code [301 MOVED_PERMANENTLY] received");
    }

    @Test
    public void testCreateVnfInstanceWithEmptyResponseBody() {
        server.expect(requestTo(TEST_SERVER_BASE_URL + INSTANCE_ENDPOINT))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withCreatedEntity(null));

        final CreateVnfRequest createVnfRequest = new CreateVnfRequest();
        createVnfRequest.setVnfdId(UUID.randomUUID().toString());
        createVnfRequest.setVnfInstanceName(testName.getMethodName());

        assertThatThrownBy(() -> driver.createVnfInstance(VNFM_CONNECTION_DETAILS_NO_AUTHENTICATION, createVnfRequest))
                .isInstanceOf(SOL003ResponseException.class)
                .hasMessage("No response body");
    }

    @Test
    public void testDeleteVnfInstance() {
        server.expect(requestTo(TEST_SERVER_BASE_URL + INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID))
              .andExpect(method(HttpMethod.DELETE))
              .andRespond(withNoContent());

        driver.deleteVnfInstance(VNFM_CONNECTION_DETAILS_NO_AUTHENTICATION, TEST_VNF_INSTANCE_ID);
    }

    @Test
    public void testDeleteVnfInstanceNotFound() {
        server.expect(requestTo(TEST_SERVER_BASE_URL + INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID))
              .andExpect(method(HttpMethod.DELETE))
              .andRespond(withStatus(HttpStatus.NOT_FOUND));

        SOL003ResponseException exception = catchThrowableOfType(() -> driver.deleteVnfInstance(VNFM_CONNECTION_DETAILS_NO_AUTHENTICATION, TEST_VNF_INSTANCE_ID), SOL003ResponseException.class);

        assertThat(exception.getProblemDetails()).isNotNull();
        assertThat(exception.getProblemDetails().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(exception.getProblemDetails().getDetail()).isEqualTo(HttpStatus.NOT_FOUND.getReasonPhrase());
    }

    @Test
    public void testDeleteVnfInstanceFailed() throws Exception {
        server.expect(requestTo(TEST_SERVER_BASE_URL + INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID))
              .andExpect(method(HttpMethod.DELETE))
              .andRespond(withServerError().body(loadFileIntoString("examples/ProblemDetails.json"))
                                           .contentType(MediaType.APPLICATION_JSON));

        SOL003ResponseException exception = catchThrowableOfType(() -> driver.deleteVnfInstance(VNFM_CONNECTION_DETAILS_NO_AUTHENTICATION, TEST_VNF_INSTANCE_ID), SOL003ResponseException.class);

        assertThat(exception.getProblemDetails()).isNotNull();
        assertThat(exception.getProblemDetails().getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(exception.getProblemDetails().getDetail()).isEqualTo("An error has occurred");
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