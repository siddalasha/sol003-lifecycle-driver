package com.accantosystems.stratoss.vnfmdriver.driver;

import java.net.MalformedURLException;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import com.accantosystems.stratoss.vnfmdriver.config.VNFMDriverProperties;

@Service("VNFPackageRepositoryDriver")
public class VNFPackageRepositoryDriver {

    private final static Logger logger = LoggerFactory.getLogger(VNFPackageRepositoryDriver.class);

    @Autowired
    private final VNFMDriverProperties vnfmDriverProperties;

    public VNFPackageRepositoryDriver(VNFMDriverProperties vnfmDriverProperties) {
        this.vnfmDriverProperties = vnfmDriverProperties;
    }

    public Resource getVnfPackage(String vnfPackageId) throws VNFPackageNotFoundException {

        // TODO some local caching would be nice

        String vnfRepositoryUrl = vnfmDriverProperties.getPackageManagement().getPackageRepositoryUrl();
        if (Strings.isEmpty(vnfRepositoryUrl)) {
            throw new VNFPackageRepositoryException("A valid VNF Package Repository URL must be configured.");
        }

        String vnfDownloadPath = vnfRepositoryUrl.replaceAll("\\{vnfPackageId\\}", vnfPackageId);

        logger.info("Attempting to load VNF Package from location {}", vnfDownloadPath);

        try {
            UrlResource vnfPackage = new UrlResource(vnfDownloadPath);

            if (!vnfPackage.exists()) {
                throw new VNFPackageNotFoundException(String.format("VNF Package not found in repository at location [%s].", vnfDownloadPath));
            }

            logger.info(" VNF Package found at location {}", vnfDownloadPath);

            return vnfPackage;
        } catch (MalformedURLException e) {
            throw new VNFPackageRepositoryException(String.format("The configured VNF Package Repository location was invalid [%s].", vnfDownloadPath), e);
        }

    }

}
