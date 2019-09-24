package com.accantosystems.stratoss.vnfmdriver.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.etsi.sol003.lifecyclemanagement.CreateVnfRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.accantosystems.stratoss.vnfmdriver.driver.VNFLifecycleManagementDriver;
import com.accantosystems.stratoss.vnfmdriver.model.VNFMConnectionDetails;
import com.accantosystems.stratoss.vnfmdriver.model.alm.ExecutionAcceptedResponse;
import com.accantosystems.stratoss.vnfmdriver.model.alm.ExecutionRequest;

@Service("LifecycleManagementService")
public class LifecycleManagementService {

    private final static Logger logger = LoggerFactory.getLogger(LifecycleManagementService.class);

    private final MessageConversionService messageConversionService;
    private final VNFLifecycleManagementDriver vnfLifecycleManagementDriver;

    @Autowired
    public LifecycleManagementService(MessageConversionService messageConversionService, VNFLifecycleManagementDriver vnfLifecycleManagementDriver) {
        this.messageConversionService = messageConversionService;
        this.vnfLifecycleManagementDriver = vnfLifecycleManagementDriver;
    }

    public ExecutionAcceptedResponse executeLifecycle(ExecutionRequest executionRequest) {
        logger.info("Processing execution request");

        if (!StringUtils.isEmpty(executionRequest.getLifecycleScripts())) {
            // lifecycleScripts should contain (if not empty) a Base64 encoded Zip file of all scripts concerning the VNFM driver
            byte[] decodedByteArray = Base64.getDecoder().decode(executionRequest.getLifecycleScripts());

            try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(decodedByteArray))) {
                ZipEntry entry = zis.getNextEntry();
                while (entry != null) {
                    logger.debug("Found zip entry: {}", entry);
                    if ("Instantiate.js".equalsIgnoreCase(entry.getName())) {
                        // How do we get content?
                        try {
                            final String script = executionRequest.getProperties().get("script");
                            final String message = messageConversionService.generateMessageFromRequest(executionRequest, script);
                            final VNFMConnectionDetails vnfmConnectionDetails = new VNFMConnectionDetails("https://geoffs-awesome-server:8080", VNFMConnectionDetails.AuthenticationType.BASIC);
                            vnfmConnectionDetails.getAuthenticationProperties().put("username", "bob");
                            vnfmConnectionDetails.getAuthenticationProperties().put("password", "secretpassw0rd");
                            vnfLifecycleManagementDriver.createVnfInstance(vnfmConnectionDetails, message);
                        } catch (MessageConversionException e) {
                            logger.error("Error converting message for sending", e);
                        }
                    }

                    // Get the next entry for the loop
                    entry = zis.getNextEntry();
                }
            } catch (IOException e) {
                logger.error("Exception raised reading lifecycle scripts", e);
            }
        }

        return null;
    }

}
