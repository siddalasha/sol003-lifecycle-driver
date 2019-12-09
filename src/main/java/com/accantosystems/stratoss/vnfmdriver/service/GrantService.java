package com.accantosystems.stratoss.vnfmdriver.service;

import java.util.UUID;

import org.etsi.sol003.granting.Grant;
import org.etsi.sol003.granting.GrantRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.accantosystems.stratoss.vnfmdriver.config.VNFMDriverProperties;
import com.accantosystems.stratoss.vnfmdriver.driver.GrantDriver;

@Service("GrantService")
public class GrantService {

    private final static Logger logger = LoggerFactory.getLogger(GrantService.class);

    private final VNFMDriverProperties vnfmDriverProperties;
    private final GrantDriver grantDriver;

    public GrantService(VNFMDriverProperties vnfmDriverProperties, GrantDriver grantDriver) {
        this.vnfmDriverProperties = vnfmDriverProperties;
        this.grantDriver = grantDriver;
    }

    public Grant requestGrant(GrantRequest grantRequest) throws GrantRejectedException {

        Grant grant;
        if (vnfmDriverProperties.getGrant().isAutomatic()) {
            // when grants are automatic, all grants requests will be accepted immediately and not persisted.
            // the created grantId will be randomly generated and will always correspond to an accepted grant
            // for now the grant response will only contain the id. Not yet sure what else it needs
            grant = new Grant();
            grant.setId(UUID.randomUUID().toString());
            logger.info("Auto-authorised grant for operation to {} VNF instance [{}]", grantRequest.getOperation(), grantRequest.getVnfInstanceId());
        } else {
            logger.info("Requesting grant operation to {} VNF instance [{}]", grantRequest.getOperation(), grantRequest.getVnfInstanceId());
            grant = grantDriver.requestGrant(grantRequest);
            if (grant == null) {
                // asynchronous grant flow. Must poll location header for granting decision
                logger.info("Grant request for operation to {} VNF instance [{}] has no immediate response and should be polled", grantRequest.getOperation(), grantRequest.getVnfInstanceId());
            } else {
                // synchronous grant flow. Response immediately available and grant resource can be returned
                logger.info("Grant request for operation to {} VNF instance [{}] was immediately granted", grantRequest.getOperation(), grantRequest.getVnfInstanceId());
            }
        }
        return grant;
    }

    public Grant getGrant(String grantId) throws GrantRejectedException {

        Grant grant;
        if (vnfmDriverProperties.getGrant().isAutomatic()) {
            // automatic granting mode
            // for now we are not persisting grants so the only information we can return in the grant response is the provided id
            logger.info("Auto-authorised grant for grantId [{}]. Returning empty grant resource", grantId);
            grant = new Grant();
            grant.setId(grantId);
            return grant;
        } else {
            logger.info("Getting grant with grantId [{}]", grantId);
            grant = grantDriver.getGrant(grantId);
            if (grant == null) {
                // grant decision not yet available
                logger.info("No grant decision yet available for request with grantId [{}]", grantId);
            } else {
                // grant decision available and grant resource can be returned
                logger.info("Grant decision available for request with grantId [{}]. Returning resource", grantId);
            }
        }
        return grant;
    }

}
