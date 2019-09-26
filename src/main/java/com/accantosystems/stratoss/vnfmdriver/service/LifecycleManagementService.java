package com.accantosystems.stratoss.vnfmdriver.service;

import static com.accantosystems.stratoss.vnfmdriver.config.VNFMDriverConstants.VNFM_SERVER_URL;

import org.etsi.sol003.lifecyclemanagement.CreateVnfRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.accantosystems.stratoss.vnfmdriver.driver.VNFLifecycleManagementDriver;
import com.accantosystems.stratoss.vnfmdriver.model.alm.ExecutionAcceptedResponse;
import com.accantosystems.stratoss.vnfmdriver.model.alm.ExecutionRequest;
import com.accantosystems.stratoss.vnfmdriver.model.alm.ResourceManagerDeploymentLocation;

@Service("LifecycleManagementService")
public class LifecycleManagementService {

    private final static Logger logger = LoggerFactory.getLogger(LifecycleManagementService.class);

    private final VNFLifecycleManagementDriver vnfLifecycleManagementDriver;

    @Autowired
    public LifecycleManagementService(VNFLifecycleManagementDriver vnfLifecycleManagementDriver) {
        this.vnfLifecycleManagementDriver = vnfLifecycleManagementDriver;
    }

    public ExecutionAcceptedResponse executeLifecycle(ExecutionRequest executionRequest) {
        logger.info("Processing execution request");

        final ResourceManagerDeploymentLocation deploymentLocation = new ResourceManagerDeploymentLocation("dummy-dl", "dummy");
        deploymentLocation.getProperties().put(VNFM_SERVER_URL, "https://geoffs-awesome-server:8080");
        vnfLifecycleManagementDriver.createVnfInstance(deploymentLocation, new CreateVnfRequest());

        return null;
    }

}
