package com.accantosystems.stratoss.vnfmdriver.web.rest;

import static com.accantosystems.stratoss.vnfmdriver.test.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.accantosystems.stratoss.vnfmdriver.model.etsi.LcmOperationStateType;
import com.accantosystems.stratoss.vnfmdriver.model.etsi.LcmOperationType;
import com.accantosystems.stratoss.vnfmdriver.model.etsi.ProblemDetails;
import com.accantosystems.stratoss.vnfmdriver.model.etsi.VnfLcmOperationOccurenceNotification;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({ "test" })
public class LifecycleNotificationControllerTest {

    public static final String NOTIFICATIONS_ENDPOINT = "/vnflcm/v1/notifications";

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void testReceiveNotification() {
        final VnfLcmOperationOccurenceNotification notification = new VnfLcmOperationOccurenceNotification();
        notification.setId(UUID.randomUUID().toString());
        notification.setVnfLcmOpOccId(UUID.randomUUID().toString());
        notification.setVnfInstanceId(UUID.randomUUID().toString());
        notification.setOperation(LcmOperationType.INSTANTIATE);
        notification.setOperationState(LcmOperationStateType.PROCESSING);
        notification.setTimeStamp(OffsetDateTime.now());
        notification.setSubscriptionId(UUID.randomUUID().toString());

        final ResponseEntity<Void> responseEntity = testRestTemplate.withBasicAuth("user", "password")
                                                                    .postForEntity(NOTIFICATIONS_ENDPOINT, notification, Void.class);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void testReceiveNotificationInvalidMessage() {
        final ResponseEntity<?> responseEntity = testRestTemplate.withBasicAuth("user", "password")
                                                                    .postForEntity(NOTIFICATIONS_ENDPOINT, "NOT_VALID_JSON", Void.class);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseEntity.getBody()).isInstanceOf(ProblemDetails.class);
    }

    @Test
    public void testReceiveNotificationNoAuthentication() {
        final ResponseEntity<?> responseEntity = testRestTemplate.postForEntity(NOTIFICATIONS_ENDPOINT, EMPTY_JSON, Void.class);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void testReceiveNotificationBadCredentials() {
        final ResponseEntity<?> responseEntity = testRestTemplate.withBasicAuth("invalid_user", "invalid_password")
                                                                 .postForEntity(NOTIFICATIONS_ENDPOINT, EMPTY_JSON, Void.class);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void testReceiveNotificationNoAuthorization() {
        final ResponseEntity<?> responseEntity = testRestTemplate.withBasicAuth("user_with_no_roles", "password")
                                                                 .postForEntity(NOTIFICATIONS_ENDPOINT, EMPTY_JSON, Void.class);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void testReceiveNotificationLockedUser() {
        final ResponseEntity<?> responseEntity = testRestTemplate.withBasicAuth("locked_user", "password")
                                                                 .postForEntity(NOTIFICATIONS_ENDPOINT, EMPTY_JSON, Void.class);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

}