package com.accantosystems.stratoss.vnfmdriver.driver;

import static com.accantosystems.stratoss.vnfmdriver.config.VNFMDriverConstants.AUTHENTICATION_ACCESS_TOKEN_URI;
import static com.accantosystems.stratoss.vnfmdriver.config.VNFMDriverConstants.AUTHENTICATION_URL;
import static com.accantosystems.stratoss.vnfmdriver.test.TestConstants.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import java.net.URI;
import java.util.UUID;

import org.etsi.sol003.lifecyclemanagement.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;

import com.accantosystems.stratoss.vnfmdriver.service.AuthenticatedRestTemplateService;

@RunWith(SpringRunner.class)
@RestClientTest({ VNFLifecycleManagementDriver.class, SOL003ResponseErrorHandler.class, AuthenticatedRestTemplateService.class })
@AutoConfigureWireMock(port = 0)
public class VNFLifecycleManagementDriverTest {

    private static final String BASE_API_ROOT = "/vnflcm/v1";
    private static final String VNF_INSTANCE_ENDPOINT = BASE_API_ROOT + "/vnf_instances";
    private static final String LCM_OP_OCC_ENDPOINT = BASE_API_ROOT + "/vnf_lcm_op_occs";
    private static final String SUBSCRIPTIONS_ENDPOINT = BASE_API_ROOT + "/subscriptions";

    @Autowired private VNFLifecycleManagementDriver driver;
    @Autowired private AuthenticatedRestTemplateService authenticatedRestTemplateService;

    @Value("${wiremock.server.port}") private int wiremockServerPort;

    @Rule public TestName testName = new TestName();

    @Test
    public void testCreateVnfInstance() throws Exception {
        final MockRestServiceServer server = MockRestServiceServer.bindTo(authenticatedRestTemplateService.getRestTemplate(TEST_DL_NO_AUTH)).build();

        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withCreatedEntity(URI.create(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID))
                                  .body(loadFileIntoString("examples/VnfInstance.json"))
                                  .contentType(MediaType.APPLICATION_JSON));

        final CreateVnfRequest createVnfRequest = new CreateVnfRequest();
        createVnfRequest.setVnfdId(UUID.randomUUID().toString());
        createVnfRequest.setVnfInstanceName(testName.getMethodName());

        final VnfInstance vnfInstance = driver.createVnfInstance(TEST_DL_NO_AUTH, createVnfRequest);

        assertThat(vnfInstance).isNotNull();
        assertThat(vnfInstance.getId()).isEqualTo(TEST_VNF_INSTANCE_ID);
    }

    @Test
    public void testCreateVnfInstanceWithBasicAuth() throws Exception {
        final MockRestServiceServer server = MockRestServiceServer.bindTo(authenticatedRestTemplateService.getRestTemplate(TEST_DL_BASIC_AUTH)).build();

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

        final VnfInstance vnfInstance = driver.createVnfInstance(TEST_DL_BASIC_AUTH, createVnfRequest);

        assertThat(vnfInstance).isNotNull();
        assertThat(vnfInstance.getId()).isEqualTo(TEST_VNF_INSTANCE_ID);
    }

    @Test
    public void testCreateVnfInstanceWithOAuth2() throws Exception {
        TEST_DL_OAUTH2_AUTH.getProperties().put(AUTHENTICATION_ACCESS_TOKEN_URI, String.format("http://localhost:%s/oauth/token", wiremockServerPort));

        stubFor(post(urlEqualTo("/oauth/token"))
                        .withBasicAuth("LmClient", "pass123")
                        .withRequestBody(equalTo("grant_type=client_credentials"))
                        .willReturn(aResponse().withBody(TEST_ACCESS_TOKEN_RESPONSE).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        final MockRestServiceServer server = MockRestServiceServer.bindTo(authenticatedRestTemplateService.getRestTemplate(TEST_DL_OAUTH2_AUTH)).build();

        server.expect(requestTo(SECURE_TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_ACCESS_TOKEN))
              .andRespond(withCreatedEntity(URI.create(SECURE_TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID))
                                  .body(loadFileIntoString("examples/VnfInstance.json"))
                                  .contentType(MediaType.APPLICATION_JSON));

        final CreateVnfRequest createVnfRequest = new CreateVnfRequest();
        createVnfRequest.setVnfdId(UUID.randomUUID().toString());
        createVnfRequest.setVnfInstanceName(testName.getMethodName());

        final VnfInstance vnfInstance = driver.createVnfInstance(TEST_DL_OAUTH2_AUTH, createVnfRequest);

        assertThat(vnfInstance).isNotNull();
        assertThat(vnfInstance.getId()).isEqualTo(TEST_VNF_INSTANCE_ID);
    }

    @Test
    public void testCreateVnfInstanceWithCookieAuth() throws Exception {
        TEST_DL_SESSION_AUTH.getProperties().put(AUTHENTICATION_URL, String.format("http://localhost:%s/login", wiremockServerPort));

        stubFor(post(urlEqualTo("/login"))
                        .withRequestBody(equalTo("IDToken1=Administrator&IDToken2=TestPassw0rd"))
                        .willReturn(aResponse().withHeader("Set-Cookie", TEST_SESSION_COOKIE)));

        final MockRestServiceServer server = MockRestServiceServer.bindTo(authenticatedRestTemplateService.getRestTemplate(TEST_DL_SESSION_AUTH)).build();

        server.expect(requestTo(SECURE_TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andExpect(header(HttpHeaders.COOKIE, TEST_SESSION_TOKEN))
              .andRespond(withCreatedEntity(URI.create(SECURE_TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID))
                                  .body(loadFileIntoString("examples/VnfInstance.json"))
                                  .contentType(MediaType.APPLICATION_JSON));

        final CreateVnfRequest createVnfRequest = new CreateVnfRequest();
        createVnfRequest.setVnfdId(UUID.randomUUID().toString());
        createVnfRequest.setVnfInstanceName(testName.getMethodName());

        final VnfInstance vnfInstance = driver.createVnfInstance(TEST_DL_SESSION_AUTH, createVnfRequest);

        assertThat(vnfInstance).isNotNull();
        assertThat(vnfInstance.getId()).isEqualTo(TEST_VNF_INSTANCE_ID);
    }

    @Test
    public void testCreateVnfInstanceWithProblemDetails() throws Exception {
        final MockRestServiceServer server = MockRestServiceServer.bindTo(authenticatedRestTemplateService.getRestTemplate(TEST_DL_NO_AUTH)).build();

        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withServerError().body(loadFileIntoString("examples/ProblemDetails.json"))
                                           .contentType(MediaType.APPLICATION_JSON));

        final CreateVnfRequest createVnfRequest = new CreateVnfRequest();
        createVnfRequest.setVnfdId(UUID.randomUUID().toString());
        createVnfRequest.setVnfInstanceName(testName.getMethodName());

        SOL003ResponseException exception = catchThrowableOfType(() -> driver.createVnfInstance(TEST_DL_NO_AUTH, createVnfRequest), SOL003ResponseException.class);

        assertThat(exception.getProblemDetails()).isNotNull();
        assertThat(exception.getProblemDetails().getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(exception.getProblemDetails().getDetail()).isEqualTo("An error has occurred");
    }

    @Test
    public void testCreateVnfInstanceWithUnknownException() {
        final MockRestServiceServer server = MockRestServiceServer.bindTo(authenticatedRestTemplateService.getRestTemplate(TEST_DL_NO_AUTH)).build();

        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withServerError());

        final CreateVnfRequest createVnfRequest = new CreateVnfRequest();
        createVnfRequest.setVnfdId(UUID.randomUUID().toString());
        createVnfRequest.setVnfInstanceName(testName.getMethodName());

        SOL003ResponseException exception = catchThrowableOfType(() -> driver.createVnfInstance(TEST_DL_NO_AUTH, createVnfRequest), SOL003ResponseException.class);

        assertThat(exception.getProblemDetails()).isNotNull();
        assertThat(exception.getProblemDetails().getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(exception.getProblemDetails().getDetail()).isEqualTo("Internal Server Error");
    }

    @Test
    public void testCreateVnfInstanceWithUnknownExceptionAndBody() {
        final MockRestServiceServer server = MockRestServiceServer.bindTo(authenticatedRestTemplateService.getRestTemplate(TEST_DL_NO_AUTH)).build();

        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withServerError().body(TEST_EXCEPTION_MESSAGE));

        final CreateVnfRequest createVnfRequest = new CreateVnfRequest();
        createVnfRequest.setVnfdId(UUID.randomUUID().toString());
        createVnfRequest.setVnfInstanceName(testName.getMethodName());

        SOL003ResponseException exception = catchThrowableOfType(() -> driver.createVnfInstance(TEST_DL_NO_AUTH, createVnfRequest), SOL003ResponseException.class);

        assertThat(exception.getProblemDetails()).isNotNull();
        assertThat(exception.getProblemDetails().getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(exception.getProblemDetails().getDetail()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase() + ": " + TEST_EXCEPTION_MESSAGE);
    }

    @Test
    public void testCreateVnfInstanceWithInvalidSuccessCode() throws Exception {
        final MockRestServiceServer server = MockRestServiceServer.bindTo(authenticatedRestTemplateService.getRestTemplate(TEST_DL_NO_AUTH)).build();

        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withSuccess().body(loadFileIntoString("examples/VnfInstance.json"))
                                       .contentType(MediaType.APPLICATION_JSON));

        final CreateVnfRequest createVnfRequest = new CreateVnfRequest();
        createVnfRequest.setVnfdId(UUID.randomUUID().toString());
        createVnfRequest.setVnfInstanceName(testName.getMethodName());

        final VnfInstance vnfInstance = driver.createVnfInstance(TEST_DL_NO_AUTH, createVnfRequest);

        assertThat(vnfInstance).isNotNull();
        assertThat(vnfInstance.getId()).isEqualTo(TEST_VNF_INSTANCE_ID);
    }

    @Test
    public void testCreateVnfInstanceWithInvalidResponseCode() {
        final MockRestServiceServer server = MockRestServiceServer.bindTo(authenticatedRestTemplateService.getRestTemplate(TEST_DL_NO_AUTH)).build();

        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withStatus(HttpStatus.MOVED_PERMANENTLY));

        final CreateVnfRequest createVnfRequest = new CreateVnfRequest();
        createVnfRequest.setVnfdId(UUID.randomUUID().toString());
        createVnfRequest.setVnfInstanceName(testName.getMethodName());

        assertThatThrownBy(() -> driver.createVnfInstance(TEST_DL_NO_AUTH, createVnfRequest))
                .isInstanceOf(SOL003ResponseException.class)
                .hasMessage("Invalid status code [301 MOVED_PERMANENTLY] received");
    }

    @Test
    public void testCreateVnfInstanceWithEmptyResponseBody() {
        final MockRestServiceServer server = MockRestServiceServer.bindTo(authenticatedRestTemplateService.getRestTemplate(TEST_DL_NO_AUTH)).build();

        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withCreatedEntity(null));

        final CreateVnfRequest createVnfRequest = new CreateVnfRequest();
        createVnfRequest.setVnfdId(UUID.randomUUID().toString());
        createVnfRequest.setVnfInstanceName(testName.getMethodName());

        assertThatThrownBy(() -> driver.createVnfInstance(TEST_DL_NO_AUTH, createVnfRequest))
                .isInstanceOf(SOL003ResponseException.class)
                .hasMessage("No response body");
    }

    @Test
    public void testDeleteVnfInstance() {
        final MockRestServiceServer server = MockRestServiceServer.bindTo(authenticatedRestTemplateService.getRestTemplate(TEST_DL_NO_AUTH)).build();

        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID))
              .andExpect(method(HttpMethod.DELETE))
              .andRespond(withNoContent());

        driver.deleteVnfInstance(TEST_DL_NO_AUTH, TEST_VNF_INSTANCE_ID);
    }

    @Test
    public void testDeleteVnfInstanceNotFound() {
        final MockRestServiceServer server = MockRestServiceServer.bindTo(authenticatedRestTemplateService.getRestTemplate(TEST_DL_NO_AUTH)).build();

        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID))
              .andExpect(method(HttpMethod.DELETE))
              .andRespond(withStatus(HttpStatus.NOT_FOUND));

        SOL003ResponseException exception = catchThrowableOfType(() -> driver.deleteVnfInstance(TEST_DL_NO_AUTH, TEST_VNF_INSTANCE_ID), SOL003ResponseException.class);

        assertThat(exception.getProblemDetails()).isNotNull();
        assertThat(exception.getProblemDetails().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(exception.getProblemDetails().getDetail()).isEqualTo(HttpStatus.NOT_FOUND.getReasonPhrase());
    }

    @Test
    public void testDeleteVnfInstanceFailed() throws Exception {
        final MockRestServiceServer server = MockRestServiceServer.bindTo(authenticatedRestTemplateService.getRestTemplate(TEST_DL_NO_AUTH)).build();

        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID))
              .andExpect(method(HttpMethod.DELETE))
              .andRespond(withServerError().body(loadFileIntoString("examples/ProblemDetails.json"))
                                           .contentType(MediaType.APPLICATION_JSON));

        SOL003ResponseException exception = catchThrowableOfType(() -> driver.deleteVnfInstance(TEST_DL_NO_AUTH, TEST_VNF_INSTANCE_ID), SOL003ResponseException.class);

        assertThat(exception.getProblemDetails()).isNotNull();
        assertThat(exception.getProblemDetails().getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(exception.getProblemDetails().getDetail()).isEqualTo("An error has occurred");
    }

    @Test
    public void testInstantiateVnf() {
        final MockRestServiceServer server = MockRestServiceServer.bindTo(authenticatedRestTemplateService.getRestTemplate(TEST_DL_NO_AUTH)).build();

        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID + "/instantiate"))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withStatus(HttpStatus.ACCEPTED).location(URI.create(TEST_SERVER_BASE_URL + LCM_OP_OCC_ENDPOINT + "/" + TEST_VNF_LCM_OP_OCC_ID)));

        final InstantiateVnfRequest instantiateVnfRequest = new InstantiateVnfRequest();
        instantiateVnfRequest.setFlavourId("Chocolate");
        instantiateVnfRequest.setInstantiationLevelId("Level 1");

        final String vnfLcmOpOccId = driver.instantiateVnf(TEST_DL_NO_AUTH, TEST_VNF_INSTANCE_ID, instantiateVnfRequest);

        assertThat(vnfLcmOpOccId).isEqualTo(TEST_VNF_LCM_OP_OCC_ID);
    }

    @Test
    public void testInstantiateVnfNoLocationHeaderReturned() {
        final MockRestServiceServer server = MockRestServiceServer.bindTo(authenticatedRestTemplateService.getRestTemplate(TEST_DL_NO_AUTH)).build();

        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID + "/instantiate"))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withStatus(HttpStatus.ACCEPTED));

        final InstantiateVnfRequest instantiateVnfRequest = new InstantiateVnfRequest();
        instantiateVnfRequest.setFlavourId("Chocolate");
        instantiateVnfRequest.setInstantiationLevelId("Level 1");

        SOL003ResponseException exception = catchThrowableOfType(() -> driver.instantiateVnf(TEST_DL_NO_AUTH, TEST_VNF_INSTANCE_ID, instantiateVnfRequest),
                                                                 SOL003ResponseException.class);

        assertThat(exception.getProblemDetails()).isNotNull();
        assertThat(exception.getProblemDetails().getStatus()).isEqualTo(SOL003ResponseException.DEFAULT_STATUS_VALUE);
        assertThat(exception.getProblemDetails().getDetail()).isEqualTo("No Location header found");
    }

    @Test
    public void testInstantiateVnfWithProblemDetails() throws Exception {
        final MockRestServiceServer server = MockRestServiceServer.bindTo(authenticatedRestTemplateService.getRestTemplate(TEST_DL_NO_AUTH)).build();

        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID + "/instantiate"))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withServerError().body(loadFileIntoString("examples/ProblemDetails.json"))
                                           .contentType(MediaType.APPLICATION_JSON));

        final InstantiateVnfRequest instantiateVnfRequest = new InstantiateVnfRequest();
        instantiateVnfRequest.setFlavourId("Chocolate");
        instantiateVnfRequest.setInstantiationLevelId("Level 1");

        SOL003ResponseException exception = catchThrowableOfType(() -> driver.instantiateVnf(TEST_DL_NO_AUTH, TEST_VNF_INSTANCE_ID, instantiateVnfRequest),
                                                                 SOL003ResponseException.class);

        assertThat(exception.getProblemDetails()).isNotNull();
        assertThat(exception.getProblemDetails().getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(exception.getProblemDetails().getDetail()).isEqualTo("An error has occurred");
    }

    @Test
    public void testScaleVnf() {
        final MockRestServiceServer server = MockRestServiceServer.bindTo(authenticatedRestTemplateService.getRestTemplate(TEST_DL_NO_AUTH)).build();

        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID + "/scale"))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withStatus(HttpStatus.ACCEPTED).location(URI.create(TEST_SERVER_BASE_URL + LCM_OP_OCC_ENDPOINT + "/" + TEST_VNF_LCM_OP_OCC_ID)));

        final ScaleVnfRequest scaleVnfRequest = new ScaleVnfRequest();
        scaleVnfRequest.setType(ScaleType.SCALE_OUT);
        scaleVnfRequest.setNumberOfSteps(1);
        scaleVnfRequest.setAspectId("South-facing");

        final String vnfLcmOpOccId = driver.scaleVnf(TEST_DL_NO_AUTH, TEST_VNF_INSTANCE_ID, scaleVnfRequest);

        assertThat(vnfLcmOpOccId).isEqualTo(TEST_VNF_LCM_OP_OCC_ID);
    }

    @Test
    public void testScaleVnfToLevel() {
        final MockRestServiceServer server = MockRestServiceServer.bindTo(authenticatedRestTemplateService.getRestTemplate(TEST_DL_NO_AUTH)).build();

        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID + "/scale_to_level"))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withStatus(HttpStatus.ACCEPTED).location(URI.create(TEST_SERVER_BASE_URL + LCM_OP_OCC_ENDPOINT + "/" + TEST_VNF_LCM_OP_OCC_ID)));

        final ScaleVnfToLevelRequest scaleVnfToLevelRequest = new ScaleVnfToLevelRequest();
        scaleVnfToLevelRequest.setInstantiationLevelId("Level 2");

        final String vnfLcmOpOccId = driver.scaleVnfToLevel(TEST_DL_NO_AUTH, TEST_VNF_INSTANCE_ID, scaleVnfToLevelRequest);

        assertThat(vnfLcmOpOccId).isEqualTo(TEST_VNF_LCM_OP_OCC_ID);
    }

    @Test
    public void testChangeVnfFlavour() {
        final MockRestServiceServer server = MockRestServiceServer.bindTo(authenticatedRestTemplateService.getRestTemplate(TEST_DL_NO_AUTH)).build();

        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID + "/change_flavour"))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withStatus(HttpStatus.ACCEPTED).location(URI.create(TEST_SERVER_BASE_URL + LCM_OP_OCC_ENDPOINT + "/" + TEST_VNF_LCM_OP_OCC_ID)));

        final ChangeVnfFlavourRequest changeVnfFlavourRequest = new ChangeVnfFlavourRequest();
        changeVnfFlavourRequest.setNewFlavourId("Vanilla");

        final String vnfLcmOpOccId = driver.changeVnfFlavour(TEST_DL_NO_AUTH, TEST_VNF_INSTANCE_ID, changeVnfFlavourRequest);

        assertThat(vnfLcmOpOccId).isEqualTo(TEST_VNF_LCM_OP_OCC_ID);
    }

    @Test
    public void testStartVnf() {
        final MockRestServiceServer server = MockRestServiceServer.bindTo(authenticatedRestTemplateService.getRestTemplate(TEST_DL_NO_AUTH)).build();

        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID + "/operate"))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withStatus(HttpStatus.ACCEPTED).location(URI.create(TEST_SERVER_BASE_URL + LCM_OP_OCC_ENDPOINT + "/" + TEST_VNF_LCM_OP_OCC_ID)));

        final OperateVnfRequest operateVnfRequest = new OperateVnfRequest();
        operateVnfRequest.setChangeStateTo(VnfOperationalStateType.STARTED);

        final String vnfLcmOpOccId = driver.operateVnf(TEST_DL_NO_AUTH, TEST_VNF_INSTANCE_ID, operateVnfRequest);

        assertThat(vnfLcmOpOccId).isEqualTo(TEST_VNF_LCM_OP_OCC_ID);
    }

    @Test
    public void testStopVnf() {
        final MockRestServiceServer server = MockRestServiceServer.bindTo(authenticatedRestTemplateService.getRestTemplate(TEST_DL_NO_AUTH)).build();

        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID + "/operate"))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withStatus(HttpStatus.ACCEPTED).location(URI.create(TEST_SERVER_BASE_URL + LCM_OP_OCC_ENDPOINT + "/" + TEST_VNF_LCM_OP_OCC_ID)));

        final OperateVnfRequest operateVnfRequest = new OperateVnfRequest();
        operateVnfRequest.setChangeStateTo(VnfOperationalStateType.STOPPED);
        operateVnfRequest.setStopType(StopType.GRACEFUL);
        operateVnfRequest.setGracefulStopTimeout(300);

        final String vnfLcmOpOccId = driver.operateVnf(TEST_DL_NO_AUTH, TEST_VNF_INSTANCE_ID, operateVnfRequest);

        assertThat(vnfLcmOpOccId).isEqualTo(TEST_VNF_LCM_OP_OCC_ID);
    }

    @Test
    public void testHealVnf() {
        final MockRestServiceServer server = MockRestServiceServer.bindTo(authenticatedRestTemplateService.getRestTemplate(TEST_DL_NO_AUTH)).build();

        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID + "/heal"))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withStatus(HttpStatus.ACCEPTED).location(URI.create(TEST_SERVER_BASE_URL + LCM_OP_OCC_ENDPOINT + "/" + TEST_VNF_LCM_OP_OCC_ID)));

        final HealVnfRequest healVnfRequest = new HealVnfRequest();
        healVnfRequest.setCause("Because its broken?");

        final String vnfLcmOpOccId = driver.healVnf(TEST_DL_NO_AUTH, TEST_VNF_INSTANCE_ID, healVnfRequest);

        assertThat(vnfLcmOpOccId).isEqualTo(TEST_VNF_LCM_OP_OCC_ID);
    }

    @Test
    public void testChangeExtVnfConnectivity() {
        final MockRestServiceServer server = MockRestServiceServer.bindTo(authenticatedRestTemplateService.getRestTemplate(TEST_DL_NO_AUTH)).build();

        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID + "/change_ext_conn"))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withStatus(HttpStatus.ACCEPTED).location(URI.create(TEST_SERVER_BASE_URL + LCM_OP_OCC_ENDPOINT + "/" + TEST_VNF_LCM_OP_OCC_ID)));

        final ChangeExtVnfConnectivityRequest changeExtVnfConnectivityRequest = new ChangeExtVnfConnectivityRequest();

        final String vnfLcmOpOccId = driver.changeExtVnfConnectivity(TEST_DL_NO_AUTH, TEST_VNF_INSTANCE_ID, changeExtVnfConnectivityRequest);

        assertThat(vnfLcmOpOccId).isEqualTo(TEST_VNF_LCM_OP_OCC_ID);
    }

    @Test
    public void testTerminateVnf() {
        final MockRestServiceServer server = MockRestServiceServer.bindTo(authenticatedRestTemplateService.getRestTemplate(TEST_DL_NO_AUTH)).build();

        server.expect(requestTo(TEST_SERVER_BASE_URL + VNF_INSTANCE_ENDPOINT + "/" + TEST_VNF_INSTANCE_ID + "/terminate"))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withStatus(HttpStatus.ACCEPTED).location(URI.create(TEST_SERVER_BASE_URL + LCM_OP_OCC_ENDPOINT + "/" + TEST_VNF_LCM_OP_OCC_ID)));

        final TerminateVnfRequest terminateVnfRequest = new TerminateVnfRequest();
        terminateVnfRequest.setTerminationType(TerminationType.GRACEFUL);
        terminateVnfRequest.setGracefulTerminationTimeout(300);

        final String vnfLcmOpOccId = driver.terminateVnf(TEST_DL_NO_AUTH, TEST_VNF_INSTANCE_ID, terminateVnfRequest);

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
        final MockRestServiceServer server = MockRestServiceServer.bindTo(authenticatedRestTemplateService.getRestTemplate(TEST_DL_NO_AUTH)).build();

        server.expect(requestTo(TEST_SERVER_BASE_URL + SUBSCRIPTIONS_ENDPOINT))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
              .andRespond(withCreatedEntity(URI.create(TEST_SERVER_BASE_URL + SUBSCRIPTIONS_ENDPOINT + "/" + TEST_LCCN_SUBSCRIPTION_ID))
                                  .body(loadFileIntoString("examples/LccnSubscription.json"))
                                  .contentType(MediaType.APPLICATION_JSON));

        final LccnSubscriptionRequest lccnSubscriptionRequest = new LccnSubscriptionRequest();
        lccnSubscriptionRequest.setCallbackUri(NOTIFICATIONS_ENDPOINT);

        final LccnSubscription lccnSubscription = driver.createLifecycleSubscription(TEST_DL_NO_AUTH, lccnSubscriptionRequest);

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
        final MockRestServiceServer server = MockRestServiceServer.bindTo(authenticatedRestTemplateService.getRestTemplate(TEST_DL_NO_AUTH)).build();

        server.expect(requestTo(TEST_SERVER_BASE_URL + SUBSCRIPTIONS_ENDPOINT + "/" + TEST_LCCN_SUBSCRIPTION_ID))
              .andExpect(method(HttpMethod.DELETE))
              .andRespond(withNoContent());

        driver.deleteLifecycleSubscription(TEST_DL_NO_AUTH, TEST_LCCN_SUBSCRIPTION_ID);
    }

}