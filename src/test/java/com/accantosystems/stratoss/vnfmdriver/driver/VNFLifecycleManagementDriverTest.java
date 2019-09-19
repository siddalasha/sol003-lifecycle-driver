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

import com.accantosystems.stratoss.vnfmdriver.model.etsi.*;

@RunWith(SpringRunner.class)
@RestClientTest({ VNFLifecycleManagementDriver.class, SOL003ResponseErrorHandler.class })
public class VNFLifecycleManagementDriverTest {

    private static final String BASE_API_ROOT = "/vnflcm/v1";
    private static final String VNF_INSTANCE_ENDPOINT = BASE_API_ROOT + "/vnf_instances";
    private static final String LCM_OP_OCC_ENDPOINT = BASE_API_ROOT + "/vnf_lcm_op_occs";
    private static final String SUBSCRIPTIONS_ENDPOINT = BASE_API_ROOT + "/subscriptions";

    @Autowired private VNFLifecycleManagementDriver driver;
    @Autowired private MockRestServiceServer server;

    @Rule public TestName testName = new TestName();

    @Test
    public void testCreateVnfInstance() throws Exception {
        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withCreatedEntity(URI.create(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID))
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
        server.expect(requestTo(SECURE_TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andExpect(header(HttpHeaders.AUTHORIZATION, BASIC_AUTHORIZATION_HEADER))
              .andRespond(withCreatedEntity(URI.create(SECURE_TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID))
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
        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT))
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
        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT))
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
        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT))
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
        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT))
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
        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT))
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
        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT))
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
        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID))
              .andExpect(method(HttpMethod.DELETE))
              .andRespond(withNoContent());

        driver.deleteVnfInstance(VNFM_CONNECTION_DETAILS_NO_AUTHENTICATION, TEST_VNF_INSTANCE_ID);
    }

    @Test
    public void testDeleteVnfInstanceNotFound() {
        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID))
              .andExpect(method(HttpMethod.DELETE))
              .andRespond(withStatus(HttpStatus.NOT_FOUND));

        SOL003ResponseException exception = catchThrowableOfType(() -> driver.deleteVnfInstance(VNFM_CONNECTION_DETAILS_NO_AUTHENTICATION, TEST_VNF_INSTANCE_ID), SOL003ResponseException.class);

        assertThat(exception.getProblemDetails()).isNotNull();
        assertThat(exception.getProblemDetails().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(exception.getProblemDetails().getDetail()).isEqualTo(HttpStatus.NOT_FOUND.getReasonPhrase());
    }

    @Test
    public void testDeleteVnfInstanceFailed() throws Exception {
        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID))
              .andExpect(method(HttpMethod.DELETE))
              .andRespond(withServerError().body(loadFileIntoString("examples/ProblemDetails.json"))
                                           .contentType(MediaType.APPLICATION_JSON));

        SOL003ResponseException exception = catchThrowableOfType(() -> driver.deleteVnfInstance(VNFM_CONNECTION_DETAILS_NO_AUTHENTICATION, TEST_VNF_INSTANCE_ID), SOL003ResponseException.class);

        assertThat(exception.getProblemDetails()).isNotNull();
        assertThat(exception.getProblemDetails().getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(exception.getProblemDetails().getDetail()).isEqualTo("An error has occurred");
    }

    @Test
    public void testInstantiateVnf() {
        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID + "/instantiate"))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withStatus(HttpStatus.ACCEPTED).location(URI.create(TEST_SERVER_BASE_URL + LCM_OP_OCC_ENDPOINT + "/" + TEST_VNF_LCM_OP_OCC_ID)));

        final InstantiateVnfRequest instantiateVnfRequest = new InstantiateVnfRequest();
        instantiateVnfRequest.setFlavourId("Chocolate");
        instantiateVnfRequest.setInstantiationLevelId("Level 1");

        final String vnfLcmOpOccId = driver.instantiateVnf(VNFM_CONNECTION_DETAILS_NO_AUTHENTICATION, TEST_VNF_INSTANCE_ID, instantiateVnfRequest);

        assertThat(vnfLcmOpOccId).isEqualTo(TEST_VNF_LCM_OP_OCC_ID);
    }

    @Test
    public void testInstantiateVnfNoLocationHeaderReturned() {
        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID + "/instantiate"))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withStatus(HttpStatus.ACCEPTED));

        final InstantiateVnfRequest instantiateVnfRequest = new InstantiateVnfRequest();
        instantiateVnfRequest.setFlavourId("Chocolate");
        instantiateVnfRequest.setInstantiationLevelId("Level 1");

        SOL003ResponseException exception = catchThrowableOfType(() -> driver.instantiateVnf(VNFM_CONNECTION_DETAILS_NO_AUTHENTICATION, TEST_VNF_INSTANCE_ID, instantiateVnfRequest),
                                                                 SOL003ResponseException.class);

        assertThat(exception.getProblemDetails()).isNotNull();
        assertThat(exception.getProblemDetails().getStatus()).isEqualTo(SOL003ResponseException.DEFAULT_STATUS_VALUE);
        assertThat(exception.getProblemDetails().getDetail()).isEqualTo("No Location header found");
    }

    @Test
    public void testInstantiateVnfWithProblemDetails() throws Exception {
        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID + "/instantiate"))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withServerError().body(loadFileIntoString("examples/ProblemDetails.json"))
                                           .contentType(MediaType.APPLICATION_JSON));

        final InstantiateVnfRequest instantiateVnfRequest = new InstantiateVnfRequest();
        instantiateVnfRequest.setFlavourId("Chocolate");
        instantiateVnfRequest.setInstantiationLevelId("Level 1");

        SOL003ResponseException exception = catchThrowableOfType(() -> driver.instantiateVnf(VNFM_CONNECTION_DETAILS_NO_AUTHENTICATION, TEST_VNF_INSTANCE_ID, instantiateVnfRequest),
                                                                 SOL003ResponseException.class);

        assertThat(exception.getProblemDetails()).isNotNull();
        assertThat(exception.getProblemDetails().getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(exception.getProblemDetails().getDetail()).isEqualTo("An error has occurred");
    }

    @Test
    public void testScaleVnf() {
        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID + "/scale"))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withStatus(HttpStatus.ACCEPTED).location(URI.create(TEST_SERVER_BASE_URL + LCM_OP_OCC_ENDPOINT + "/" + TEST_VNF_LCM_OP_OCC_ID)));

        final ScaleVnfRequest scaleVnfRequest = new ScaleVnfRequest();
        scaleVnfRequest.setType(ScaleType.SCALE_OUT);
        scaleVnfRequest.setNumberOfSteps(1);
        scaleVnfRequest.setAspectId("South-facing");

        final String vnfLcmOpOccId = driver.scaleVnf(VNFM_CONNECTION_DETAILS_NO_AUTHENTICATION, TEST_VNF_INSTANCE_ID, scaleVnfRequest);

        assertThat(vnfLcmOpOccId).isEqualTo(TEST_VNF_LCM_OP_OCC_ID);
    }

    @Test
    public void testScaleVnfToLevel() {
        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID + "/scale_to_level"))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withStatus(HttpStatus.ACCEPTED).location(URI.create(TEST_SERVER_BASE_URL + LCM_OP_OCC_ENDPOINT + "/" + TEST_VNF_LCM_OP_OCC_ID)));

        final ScaleVnfToLevelRequest scaleVnfToLevelRequest = new ScaleVnfToLevelRequest();
        scaleVnfToLevelRequest.setInstantiationLevelId("Level 2");

        final String vnfLcmOpOccId = driver.scaleVnfToLevel(VNFM_CONNECTION_DETAILS_NO_AUTHENTICATION, TEST_VNF_INSTANCE_ID, scaleVnfToLevelRequest);

        assertThat(vnfLcmOpOccId).isEqualTo(TEST_VNF_LCM_OP_OCC_ID);
    }

    @Test
    public void testChangeVnfFlavour() {
        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID + "/change_flavour"))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withStatus(HttpStatus.ACCEPTED).location(URI.create(TEST_SERVER_BASE_URL + LCM_OP_OCC_ENDPOINT + "/" + TEST_VNF_LCM_OP_OCC_ID)));

        final ChangeVnfFlavourRequest changeVnfFlavourRequest = new ChangeVnfFlavourRequest();
        changeVnfFlavourRequest.setNewFlavourId("Vanilla");

        final String vnfLcmOpOccId = driver.changeVnfFlavour(VNFM_CONNECTION_DETAILS_NO_AUTHENTICATION, TEST_VNF_INSTANCE_ID, changeVnfFlavourRequest);

        assertThat(vnfLcmOpOccId).isEqualTo(TEST_VNF_LCM_OP_OCC_ID);
    }

    @Test
    public void testStartVnf() {
        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID + "/operate"))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withStatus(HttpStatus.ACCEPTED).location(URI.create(TEST_SERVER_BASE_URL + LCM_OP_OCC_ENDPOINT + "/" + TEST_VNF_LCM_OP_OCC_ID)));

        final OperateVnfRequest operateVnfRequest = new OperateVnfRequest();
        operateVnfRequest.setChangeStateTo(VnfOperationalStateType.STARTED);

        final String vnfLcmOpOccId = driver.operateVnf(VNFM_CONNECTION_DETAILS_NO_AUTHENTICATION, TEST_VNF_INSTANCE_ID, operateVnfRequest);

        assertThat(vnfLcmOpOccId).isEqualTo(TEST_VNF_LCM_OP_OCC_ID);
    }

    @Test
    public void testStopVnf() {
        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID + "/operate"))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withStatus(HttpStatus.ACCEPTED).location(URI.create(TEST_SERVER_BASE_URL + LCM_OP_OCC_ENDPOINT + "/" + TEST_VNF_LCM_OP_OCC_ID)));

        final OperateVnfRequest operateVnfRequest = new OperateVnfRequest();
        operateVnfRequest.setChangeStateTo(VnfOperationalStateType.STOPPED);
        operateVnfRequest.setStopType(StopType.GRACEFUL);
        operateVnfRequest.setGracefulStopTimeout(300);

        final String vnfLcmOpOccId = driver.operateVnf(VNFM_CONNECTION_DETAILS_NO_AUTHENTICATION, TEST_VNF_INSTANCE_ID, operateVnfRequest);

        assertThat(vnfLcmOpOccId).isEqualTo(TEST_VNF_LCM_OP_OCC_ID);
    }

    @Test
    public void testHealVnf() {
        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID + "/heal"))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withStatus(HttpStatus.ACCEPTED).location(URI.create(TEST_SERVER_BASE_URL + LCM_OP_OCC_ENDPOINT + "/" + TEST_VNF_LCM_OP_OCC_ID)));

        final HealVnfRequest healVnfRequest = new HealVnfRequest();
        healVnfRequest.setCause("Because its broken?");

        final String vnfLcmOpOccId = driver.healVnf(VNFM_CONNECTION_DETAILS_NO_AUTHENTICATION, TEST_VNF_INSTANCE_ID, healVnfRequest);

        assertThat(vnfLcmOpOccId).isEqualTo(TEST_VNF_LCM_OP_OCC_ID);
    }

    @Test
    public void testChangeExtVnfConnectivity() {
        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID + "/change_ext_conn"))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withStatus(HttpStatus.ACCEPTED).location(URI.create(TEST_SERVER_BASE_URL + LCM_OP_OCC_ENDPOINT + "/" + TEST_VNF_LCM_OP_OCC_ID)));

        final ChangeExtVnfConnectivityRequest changeExtVnfConnectivityRequest = new ChangeExtVnfConnectivityRequest();

        final String vnfLcmOpOccId = driver.changeExtVnfConnectivity(VNFM_CONNECTION_DETAILS_NO_AUTHENTICATION, TEST_VNF_INSTANCE_ID, changeExtVnfConnectivityRequest);

        assertThat(vnfLcmOpOccId).isEqualTo(TEST_VNF_LCM_OP_OCC_ID);
    }

    @Test
    public void testTerminateVnf() {
        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID + "/terminate"))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withStatus(HttpStatus.ACCEPTED).location(URI.create(TEST_SERVER_BASE_URL + LCM_OP_OCC_ENDPOINT + "/" + TEST_VNF_LCM_OP_OCC_ID)));

        final TerminateVnfRequest terminateVnfRequest = new TerminateVnfRequest();
        terminateVnfRequest.setTerminationType(TerminationType.GRACEFUL);
        terminateVnfRequest.setGracefulTerminationTimeout(300);

        final String vnfLcmOpOccId = driver.terminateVnf(VNFM_CONNECTION_DETAILS_NO_AUTHENTICATION, TEST_VNF_INSTANCE_ID, terminateVnfRequest);

        assertThat(vnfLcmOpOccId).isEqualTo(TEST_VNF_LCM_OP_OCC_ID);
    }

    @Test
    public void testQueryAllLifecycleOperationOccurrences() {
    }

    @Test
    public void testQueryLifecycleOperationOccurrence() {
    }

    @Test
    public void testCreateLifecycleSubscription() throws Exception {
        server.expect(requestTo(TEST_SERVER_BASE_URL + SUBSCRIPTIONS_ENDPOINT))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withCreatedEntity(URI.create(TEST_SERVER_BASE_URL + SUBSCRIPTIONS_ENDPOINT + "/" + TEST_LCCN_SUBSCRIPTION_ID))
                                  .body(loadFileIntoString("examples/LccnSubscription.json"))
                                  .contentType(MediaType.APPLICATION_JSON));

        final LccnSubscriptionRequest lccnSubscriptionRequest = new LccnSubscriptionRequest();
        lccnSubscriptionRequest.setCallbackUri(NOTIFICATIONS_ENDPOINT);

        final LccnSubscription lccnSubscription = driver.createLifecycleSubscription(VNFM_CONNECTION_DETAILS_NO_AUTHENTICATION, lccnSubscriptionRequest);

        assertThat(lccnSubscription).isNotNull();
        assertThat(lccnSubscription.getId()).isEqualTo(TEST_LCCN_SUBSCRIPTION_ID);
        assertThat(lccnSubscription.getCallbackUri()).isEqualTo(NOTIFICATIONS_ENDPOINT);
    }

    @Test
    public void testQueryAllLifecycleSubscriptions() {
    }

    @Test
    public void testQueryLifecycleSubscription() {
    }

    @Test
    public void testDeleteLifecycleSubscription() {
        server.expect(requestTo(TEST_SERVER_BASE_URL + SUBSCRIPTIONS_ENDPOINT + "/" + TEST_LCCN_SUBSCRIPTION_ID))
              .andExpect(method(HttpMethod.DELETE))
              .andRespond(withNoContent());

        driver.deleteLifecycleSubscription(VNFM_CONNECTION_DETAILS_NO_AUTHENTICATION, TEST_LCCN_SUBSCRIPTION_ID);
    }

}