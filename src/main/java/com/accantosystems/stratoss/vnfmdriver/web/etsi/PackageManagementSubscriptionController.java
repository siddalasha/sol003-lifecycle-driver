package com.accantosystems.stratoss.vnfmdriver.web.etsi;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import org.etsi.sol003.packagemanagement.PkgmSubscription;
import org.etsi.sol003.packagemanagement.PkgmSubscriptionRequest;
import org.etsi.sol003.packagemanagement.VnfPkgInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.accantosystems.stratoss.vnfmdriver.driver.VNFPackageNotFoundException;
import com.accantosystems.stratoss.vnfmdriver.service.ContentRangeNotSatisfiableException;
import com.accantosystems.stratoss.vnfmdriver.service.PackageManagementService;
import com.accantosystems.stratoss.vnfmdriver.service.PackageStateConflictException;
import com.accantosystems.stratoss.vnfmdriver.service.UnexpectedPackageContentsException;

import io.swagger.annotations.ApiOperation;

@RestController("PackageManagementSubscriptionController")
@RequestMapping(PackageManagementSubscriptionController.API_PATH)
public class PackageManagementSubscriptionController {

    public static final String API_PATH = "/vnfpkgm/v1/subscriptions";

    private static final Logger logger = LoggerFactory.getLogger(PackageManagementSubscriptionController.class);

    private final Map<String, PkgmSubscription> localSubscriptionCache = new ConcurrentHashMap<>();

    @PostMapping()
    @ApiOperation(value = "Create New Subscription", notes = "Creates a new subscription for packages")
    public ResponseEntity<PkgmSubscription> createNewSubscription(PkgmSubscriptionRequest subscriptionRequest, HttpServletRequest servletRequest) {
        try (BufferedReader messageReader = servletRequest.getReader()) {
            String rawMessage = messageReader.lines().collect(Collectors.joining("\n"));
            logger.info("Received request to create a new package management subscription:\n{}", rawMessage);
        } catch (IOException e) {
            logger.warn(String.format("Exception caught logging PkgmSubscriptionRequest message: %s", e.getMessage()), e);
        }

        PkgmSubscription newSubscription = new PkgmSubscription();
        newSubscription.setId(UUID.randomUUID().toString());
        newSubscription.setFilter(subscriptionRequest.getFilter());
        newSubscription.setCallbackUri(subscriptionRequest.getCallbackUri());
        localSubscriptionCache.put(newSubscription.getId(), newSubscription);

        final ServletUriComponentsBuilder uriBuilder = ServletUriComponentsBuilder.fromCurrentRequest();
        URI location = uriBuilder.pathSegment("{id}").buildAndExpand(newSubscription.getId()).toUri();
        return ResponseEntity.created(location).body(newSubscription);
    }

    @GetMapping(path = "/{subscriptionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Reads the information of an individual Subscription", notes = "This resource represents an individual Subscription. The client can use this resource to read information of the Subscription.")
    public ResponseEntity<PkgmSubscription> getSubscription(@PathVariable String subscriptionId) {
        logger.info("Received request for Package Management Subscription [{}]", subscriptionId);
        return localSubscriptionCache.containsKey(subscriptionId) ? ResponseEntity.ok(localSubscriptionCache.get(subscriptionId)) : ResponseEntity.notFound().build();
    }

    @DeleteMapping(path = "/{subscriptionId}")
    @ApiOperation(value = "Removes an individual Subscription", notes = "The client can use this delete an individual Subscription.")
    public ResponseEntity<Void> deleteSubscription(@PathVariable String subscriptionId) {
        logger.info("Received request to remove Package Management Subscription [{}]", subscriptionId);
        return localSubscriptionCache.remove(subscriptionId) != null ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

}
