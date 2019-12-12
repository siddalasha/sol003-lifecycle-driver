package com.accantosystems.stratoss.vnfmdriver.web.etsi;

import java.net.URI;

import org.etsi.sol003.granting.Grant;
import org.etsi.sol003.granting.GrantRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.accantosystems.stratoss.vnfmdriver.driver.GrantProviderException;
import com.accantosystems.stratoss.vnfmdriver.model.GrantCreationResponse;
import com.accantosystems.stratoss.vnfmdriver.service.GrantRejectedException;
import com.accantosystems.stratoss.vnfmdriver.service.GrantService;

import io.swagger.annotations.ApiOperation;

@RestController("GrantController")
@RequestMapping(GrantController.GRANTS_ENDPOINT)
public class GrantController {

    private final static Logger logger = LoggerFactory.getLogger(GrantController.class);

    public static final String GRANTS_ENDPOINT = "/grant/v1/grants";
    private static final String GRANT_LOCATION = GrantController.GRANTS_ENDPOINT + "/{grantId}";

    private final GrantService grantService;

    @Autowired
    public GrantController(GrantService grantService) {
        this.grantService = grantService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Requests a grant for a particular VNF lifecycle operation.", code = 201)
    public ResponseEntity<Grant> requestGrant(@RequestBody GrantRequest grantRequest) throws GrantRejectedException, GrantProviderException {
        logger.info("Received grant request:\n{}", grantRequest);

        GrantCreationResponse grantCreationResponse = grantService.requestGrant(grantRequest);

        final ServletUriComponentsBuilder uriBuilder = ServletUriComponentsBuilder.fromCurrentContextPath();
        URI location = uriBuilder.path(GRANT_LOCATION).buildAndExpand(grantCreationResponse.getGrantId()).toUri();

        if (grantCreationResponse.getGrant() != null) {
            return ResponseEntity.created(location).body(grantCreationResponse.getGrant());
        } else {
            return ResponseEntity.accepted().location(location).build();
        }
    }

    @GetMapping(path = { "/{grantId}" }, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Reads a grant", notes = "Returns a previously created grant resource if a granting decision has been made.")
    public ResponseEntity<Grant> getGrant(@PathVariable String grantId) throws GrantRejectedException, GrantProviderException {
        logger.info("Received grant fetch for id [{}]", grantId);

        Grant grant = grantService.getGrant(grantId);

        if (grant != null) {
            return ResponseEntity.ok(grant);
        } else {
            return ResponseEntity.accepted().build();
        }
    }

}
