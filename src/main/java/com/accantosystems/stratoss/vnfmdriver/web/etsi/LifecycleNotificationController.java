package com.accantosystems.stratoss.vnfmdriver.web.etsi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.accantosystems.stratoss.vnfmdriver.model.etsi.SOL003Notification;
import com.accantosystems.stratoss.vnfmdriver.service.ExternalMessagingService;

import io.swagger.annotations.ApiOperation;

@RestController("LifecycleNotificationController")
@RequestMapping("/vnflcm/v1/notifications")
public class LifecycleNotificationController {

    private final static Logger logger = LoggerFactory.getLogger(LifecycleNotificationController.class);

    private final ExternalMessagingService externalMessagingService;

    @Autowired
    public LifecycleNotificationController(ExternalMessagingService externalMessagingService) {
        this.externalMessagingService = externalMessagingService;
    }

    @PostMapping
    @ApiOperation(value = "Receives a lifecycle operation occurrence notification from a VNFM", code = 204)
    public ResponseEntity<Void> receiveNotification(@RequestBody SOL003Notification notification) {
        logger.info("Received notification:\n{}", notification);
        return ResponseEntity.noContent().build();
    }

}
