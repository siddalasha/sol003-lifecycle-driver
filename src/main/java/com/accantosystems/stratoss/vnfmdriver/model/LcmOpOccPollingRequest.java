package com.accantosystems.stratoss.vnfmdriver.model;

import com.accantosystems.stratoss.vnfmdriver.model.alm.ResourceManagerDeploymentLocation;

public class LcmOpOccPollingRequest {

    private final ResourceManagerDeploymentLocation deploymentLocation;
    private final String vnfLcmOpOccId;

    public LcmOpOccPollingRequest(ResourceManagerDeploymentLocation deploymentLocation, String vnfLcmOpOccId) {
        this.deploymentLocation = deploymentLocation;
        this.vnfLcmOpOccId = vnfLcmOpOccId;
    }

    public ResourceManagerDeploymentLocation getDeploymentLocation() {
        return deploymentLocation;
    }

    public String getVnfLcmOpOccId() {
        return vnfLcmOpOccId;
    }

}
