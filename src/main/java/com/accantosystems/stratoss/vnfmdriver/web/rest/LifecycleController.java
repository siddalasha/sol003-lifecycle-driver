package com.accantosystems.stratoss.vnfmdriver.web.rest;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.accantosystems.stratoss.vnfmdriver.model.alm.ExecutionAcceptedResponse;
import com.accantosystems.stratoss.vnfmdriver.model.alm.ExecutionRequest;

import io.swagger.annotations.ApiOperation;

@RestController("LifecycleController")
@RequestMapping("/api/lifecycle")
public class LifecycleController {

    private final static Logger logger = LoggerFactory.getLogger(LifecycleController.class);

    @Autowired
    public LifecycleController() {}

    @PostMapping("/execute")
    @ApiOperation(value = "Execute a lifecycle against a VNFM", notes = "Initiates a lifecycle against a VNF, managed by a VNFM")
    public ResponseEntity<ExecutionAcceptedResponse> executeLifecycle(@RequestBody ExecutionRequest executionRequest) {
        logger.info("Received request to execute a lifecycle [{}] at deployment location [{}]", executionRequest.getLifecycleName(), executionRequest.getDeploymentLocation().getName());
        return ResponseEntity.accepted().body(new ExecutionAcceptedResponse(UUID.randomUUID().toString()));
    }

}
