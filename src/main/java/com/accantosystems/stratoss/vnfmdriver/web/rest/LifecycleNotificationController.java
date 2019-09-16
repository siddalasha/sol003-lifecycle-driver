package com.accantosystems.stratoss.vnfmdriver.web.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

@RestController("LifecycleNotificationController")
@RequestMapping("/vnflcm/v1/notifications")
public class LifecycleNotificationController {

    @PostMapping
    @ApiOperation(value = "Receives a lifecycle operation occurrence notification from a VNFM", code = 204)
    public ResponseEntity<Void> receiveNotification(String notification) {
        return ResponseEntity.noContent().build();
    }

}
