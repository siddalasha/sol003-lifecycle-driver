package com.accantosystems.stratoss.vnfmdriver.web.etsi;

import static com.accantosystems.stratoss.vnfmdriver.test.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.accantosystems.stratoss.vnfmdriver.model.alm.ExecutionAsyncResponse;
import com.accantosystems.stratoss.vnfmdriver.model.alm.ExecutionStatus;
import com.accantosystems.stratoss.vnfmdriver.model.alm.FailureDetails;
import com.accantosystems.stratoss.vnfmdriver.model.etsi.ProblemDetails;
import com.accantosystems.stratoss.vnfmdriver.service.ExternalMessagingService;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({ "test" })
public class LifecycleNotificationControllerTest {

    public static final String NOTIFICATIONS_ENDPOINT = "/vnflcm/v1/notifications";

    @Autowired private TestRestTemplate testRestTemplate;
    @MockBean private ExternalMessagingService externalMessagingService;

    @Test
    public void testReceiveNotification() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> httpEntity = new HttpEntity<>(loadFileIntoString("examples/VnfLcmOperationOccurrenceNotification-INSTANTIATE-PROCESSING.json"), headers);
        final ResponseEntity<Void> responseEntity = testRestTemplate.withBasicAuth("user", "password")
                                                                    .postForEntity(NOTIFICATIONS_ENDPOINT, httpEntity, Void.class);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        verifyZeroInteractions(externalMessagingService);
    }

    @Test
    public void testReceiveOpOccCompletedNotification() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> httpEntity = new HttpEntity<>(loadFileIntoString("examples/VnfLcmOperationOccurrenceNotification-INSTANTIATE-COMPLETED.json"), headers);
        final ResponseEntity<Void> responseEntity = testRestTemplate.withBasicAuth("user", "password")
                                                                    .postForEntity(NOTIFICATIONS_ENDPOINT, httpEntity, Void.class);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        final ArgumentCaptor<ExecutionAsyncResponse> argument = ArgumentCaptor.forClass(ExecutionAsyncResponse.class);
        verify(externalMessagingService).sendExecutionAsyncResponse(argument.capture());

        final ExecutionAsyncResponse asyncResponse = argument.getValue();
        assertThat(asyncResponse).isNotNull();
        assertThat(asyncResponse.getRequestId()).isEqualTo("8dbe6621-f6b9-49ba-878b-26803f107f27");
        assertThat(asyncResponse.getStatus()).isEqualTo(ExecutionStatus.COMPLETE);
        assertThat(asyncResponse.getFailureDetails()).isNull();
        assertThat(asyncResponse.getOutputs()).isEmpty();
    }

    @Test
    public void testReceiveOpOccFailedNotification() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> httpEntity = new HttpEntity<>(loadFileIntoString("examples/VnfLcmOperationOccurrenceNotification-INSTANTIATE-FAILED.json"), headers);
        final ResponseEntity<Void> responseEntity = testRestTemplate.withBasicAuth("user", "password")
                                                                    .postForEntity(NOTIFICATIONS_ENDPOINT, httpEntity, Void.class);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        final ArgumentCaptor<ExecutionAsyncResponse> argument = ArgumentCaptor.forClass(ExecutionAsyncResponse.class);
        verify(externalMessagingService).sendExecutionAsyncResponse(argument.capture());

        final ExecutionAsyncResponse asyncResponse = argument.getValue();
        assertThat(asyncResponse).isNotNull();
        assertThat(asyncResponse.getRequestId()).isEqualTo("8dbe6621-f6b9-49ba-878b-26803f107f27");
        assertThat(asyncResponse.getStatus()).isEqualTo(ExecutionStatus.FAILED);
        assertThat(asyncResponse.getFailureDetails()).isNotNull();
        assertThat(asyncResponse.getFailureDetails().getFailureCode()).isEqualTo(FailureDetails.FailureCode.INTERNAL_ERROR);
        assertThat(asyncResponse.getFailureDetails().getDescription()).isEqualTo("Error instantiating VNF");
        assertThat(asyncResponse.getOutputs()).isEmpty();
    }

    @Test
    public void testReceiveNotificationInvalidJSONMessage() {
        final ResponseEntity<ProblemDetails> responseEntity = testRestTemplate.withBasicAuth("user", "password")
                                                                              .postForEntity(NOTIFICATIONS_ENDPOINT, EMPTY_JSON_ENTITY, ProblemDetails.class);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseEntity.getBody()).isNotNull();
    }

    @Test
    public void testReceiveNotificationInvalidMessage() {
        final ResponseEntity<ProblemDetails> responseEntity = testRestTemplate.withBasicAuth("user", "password")
                                                                              .postForEntity(NOTIFICATIONS_ENDPOINT, "NOT_VALID_JSON", ProblemDetails.class);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        assertThat(responseEntity.getBody()).isNotNull();
    }

    @Test
    public void testReceiveNotificationNoAuthentication() {
        final ResponseEntity<Void> responseEntity = testRestTemplate.postForEntity(NOTIFICATIONS_ENDPOINT, EMPTY_JSON, Void.class);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(responseEntity.getBody()).isNull();
    }

    @Test
    public void testReceiveNotificationBadCredentials() {
        final ResponseEntity<ProblemDetails> responseEntity = testRestTemplate.withBasicAuth("invalid_user", "invalid_password")
                                                                              .postForEntity(NOTIFICATIONS_ENDPOINT, EMPTY_JSON, ProblemDetails.class);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(responseEntity.getBody()).isNull();
    }

    @Test
    public void testReceiveNotificationNoAuthorization() {
        final ResponseEntity<ProblemDetails> responseEntity = testRestTemplate.withBasicAuth("user_with_no_roles", "password")
                                                                              .postForEntity(NOTIFICATIONS_ENDPOINT, EMPTY_JSON, ProblemDetails.class);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(responseEntity.getBody()).isNotNull();
    }

    @Test
    public void testReceiveNotificationLockedUser() {
        final ResponseEntity<ProblemDetails> responseEntity = testRestTemplate.withBasicAuth("locked_user", "password")
                                                                              .postForEntity(NOTIFICATIONS_ENDPOINT, EMPTY_JSON, ProblemDetails.class);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(responseEntity.getBody()).isNull();
    }

}