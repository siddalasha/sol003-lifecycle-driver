package com.accantosystems.stratoss.vnfmdriver.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.accantosystems.stratoss.vnfmdriver.model.alm.ExecutionAcceptedResponse;
import com.accantosystems.stratoss.vnfmdriver.model.alm.ExecutionRequest;
import com.accantosystems.stratoss.vnfmdriver.model.alm.ResourceManagerDeploymentLocation;
import com.accantosystems.stratoss.vnfmdriver.model.etsi.LcmOperationStateType;
import com.accantosystems.stratoss.vnfmdriver.model.etsi.LcmOperationType;
import com.accantosystems.stratoss.vnfmdriver.model.etsi.VnfLcmOperationOccurenceNotification;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({ "test" })
public class LifecycleNotificationControllerTest {

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
                                                                    .postForEntity("/vnflcm/v1/notifications", notification, Void.class);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

}