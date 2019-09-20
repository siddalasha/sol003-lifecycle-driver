package com.accantosystems.stratoss.vnfmdriver.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.accantosystems.stratoss.vnfmdriver.model.alm.ExecutionAcceptedResponse;
import com.accantosystems.stratoss.vnfmdriver.model.alm.ExecutionRequest;

@Service("LifecycleManagementService")
public class LifecycleManagementService {

    private final static Logger logger = LoggerFactory.getLogger(LifecycleManagementService.class);

    public ExecutionAcceptedResponse executeLifecycle(ExecutionRequest executionRequest) {
        logger.info("Processing execution request");

        if (!StringUtils.isEmpty(executionRequest.getLifecycleScripts())) {
            // lifecycleScripts should contain (if not empty) a Base64 encoded Zip file of all scripts concerning the VNFM driver
            byte[] decodedByteArray = Base64.getDecoder().decode(executionRequest.getLifecycleScripts());

            try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(decodedByteArray))) {
                ZipEntry entry = zis.getNextEntry();
                while (entry != null) {
                    logger.debug("Found zip entry: {}", entry);
                    entry = zis.getNextEntry();
                }
            } catch (IOException e) {
                logger.error("Exception raised reading lifecycle scripts", e);
            }
        }

        throw new UnsupportedOperationException("Not implemented yet");
    }

}
