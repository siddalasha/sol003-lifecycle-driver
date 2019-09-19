package com.accantosystems.stratoss.vnfmdriver.web.etsi;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import org.etsi.sol003.granting.Grant;
import org.etsi.sol003.granting.GrantRequest;
import com.accantosystems.stratoss.vnfmdriver.service.GrantRejectedException;
import com.accantosystems.stratoss.vnfmdriver.service.GrantService;

import io.swagger.annotations.ApiOperation;

@RestController("GrantController")
@RequestMapping("/grant/v1/grants")
public class GrantController {

    private GrantService grantService;

    private final static Logger logger = LoggerFactory.getLogger(GrantController.class);

    @Autowired
    public GrantController(GrantService grantService) {
        this.grantService = grantService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Requests a grant for a particular VNF lifecycle operation.", code = 201)
    public ResponseEntity<Grant> requestGrant(@RequestBody GrantRequest grantRequest) throws GrantRejectedException {
        logger.info("Received grant request:\n{}", grantRequest);

        // currently only supporting synchronous mode grant requests
        Grant grant = grantService.requestGrant(grantRequest);

        final ServletUriComponentsBuilder uriBuilder = ServletUriComponentsBuilder.fromCurrentContextPath();
        URI location = uriBuilder.path("/grant/v1/grants/{grantId}").buildAndExpand(grant.getId()).toUri();
        return ResponseEntity.created(location).body(grant);
    }

    @GetMapping(path = { "/{grantId}" }, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Reads a grant", notes = "Returns a previously created grant resource if a granting decision has been made.")
    public ResponseEntity<Grant> getGrant(@PathVariable String grantId) throws GrantRejectedException {
        logger.info("Received grant fetch for id [{}]", grantId);

        Grant grant = grantService.getGrant(grantId);

        return ResponseEntity.ok(grant);
    }

}
