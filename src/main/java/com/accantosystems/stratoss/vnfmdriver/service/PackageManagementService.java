package com.accantosystems.stratoss.vnfmdriver.service;

import org.etsi.sol003.packagemanagement.VnfPkgInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.accantosystems.stratoss.vnfmdriver.driver.VnfPackageDriver;
import com.accantosystems.stratoss.vnfmdriver.web.etsi.ResponseTypeNotAcceptableException;

@Service("PackageManagementService")
public class PackageManagementService {

    final private VnfPackageDriver vnfPackageDriver;

    @Autowired
    public PackageManagementService(VnfPackageDriver vnfPackageDriver) {
        this.vnfPackageDriver = vnfPackageDriver;
    }

    public VnfPkgInfo getVnfPackageInfo(String vnfPkgId) {
        // TODO implement
        return null;
    }

    public String getVnfdAsYaml(String vnfPkgId) throws ResponseTypeNotAcceptableException {

        // TODO implement
        // grab the package
        // extract the zip
        // find the descriptor
        // return

        return "";

    }

    public Resource getVnfdAsZip(String vnfPkgId) throws ResponseTypeNotAcceptableException {

        // TODO implement
        // grab the package
        // extract the zip
        // find the descriptors
        // zip
        // return

        return null;

    }

    public Resource getVnfPackageContent(String vnfPkgId, String contentRange) throws PackageStateConflictException, ContentRangeNotSatisfiableException {
        // TODO implement
        return null;
    }

    public Resource getVnfPackageArtifact(String vnfPkgId, String artifactPath, String contentRange) throws PackageStateConflictException, ContentRangeNotSatisfiableException {
        // TODO implement
        return null;
    }

}
