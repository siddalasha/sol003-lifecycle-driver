package com.accantosystems.stratoss.vnfmdriver.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import org.etsi.sol003.granting.Grant;
import org.etsi.sol003.granting.GrantRequest;

@Service("GrantService")
public class GrantService {

    public Grant requestGrant(GrantRequest grantRequest) throws GrantRejectedException {

        // for now all grants requests will be accepted immediately and not persisted.
        // the created grantId will be randomly generated and will always correspond to an accepted grant
        // for now the grant response will only contain the id. Not yet sure what else it needs
        Grant grant = new Grant();
        grant.setId(UUID.randomUUID().toString());
        return grant;
    }

    public Grant getGrant(String grantId) throws GrantRejectedException {

        // for not we are not persisting grants so the only information we can return in the grant response is the provided id
        Grant grant = new Grant();
        grant.setId(grantId);
        return grant;
    }

}
