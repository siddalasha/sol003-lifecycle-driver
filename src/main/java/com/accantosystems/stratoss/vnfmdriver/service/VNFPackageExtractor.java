package com.accantosystems.stratoss.vnfmdriver.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.etsi.sol003.packagemanagement.VnfPkgInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

public abstract class VNFPackageExtractor {

    private final static Logger logger = LoggerFactory.getLogger(VNFPackageExtractor.class);

    public abstract VnfPkgInfo populateVnfPackageInfo(String vnfPkgId, Resource vnfPackageZip);

    public abstract String extractVnfdAsYaml(String vnfPkgId, Resource vnfPackageZip) throws UnexpectedPackageContentsException;

    public abstract Resource extractVnfdAsZip(String vnfPkgId, Resource vnfPackageZip);

    public Resource extractVnfPackageArtifact(String vnfPkgId, String artifactPath, Resource vnfPackageZip) throws PackageStateConflictException, ContentRangeNotSatisfiableException {

        logger.info("Extracting VNF package artifact on path [{}] from VNF package with id [{}]", artifactPath, vnfPkgId);

        Resource artifactResource = null;
        try {
            byte[] artifactByteArray = extractArtifactFromZip(vnfPackageZip, artifactPath);
            if (artifactByteArray == null) {
                throw new VNFPackageExtractionException(String.format("Unable to find artifact [%s] within VnfPackage with id [%s]", artifactPath, vnfPkgId));
            }
            artifactResource = new ByteArrayResource(artifactByteArray);
            return artifactResource;
        } catch (IOException e) {
            handleGenericExtractionException(e, vnfPkgId);
        }
        return artifactResource;
    }

    protected VNFPackageExtractionException handleGenericExtractionException(Exception e, String vnfPkgId) {
        throw new VNFPackageExtractionException(String.format("Unable to extract Vnf Package with id [%s]", vnfPkgId), e);
    }

    protected byte[] extractArtifactFromZip(Resource zipPackage, String artifactPath) throws IOException {

        try (ZipInputStream zipInputStream = new ZipInputStream(zipPackage.getInputStream())) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (zipEntry.getName().equals(artifactPath)) {
                    return extractBytesFromZipEntry(zipInputStream);
                }
            }
            return null;
        }
    }

    // return map of artifacts keyed on their path. Excludes any directories in the zip
    protected Map<String, byte[]> extractArtifactsFromZip(Resource zipPackage, String artifactPathStartsWith) throws IOException {
        Map<String, byte[]> artifactResources = new HashMap<String, byte[]>();

        try (ZipInputStream zipInputStream = new ZipInputStream(zipPackage.getInputStream())) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (!zipEntry.isDirectory() && zipEntry.getName().startsWith(artifactPathStartsWith)) {
                    artifactResources.put(zipEntry.getName(), extractBytesFromZipEntry(zipInputStream));
                }
            }
        }
        return artifactResources;
    }

    protected List<String> listPackageArtifacts(Resource zipPackage, String artifactPathStartsWith) throws IOException {
        List<String> zipContents = new ArrayList<String>();

        try (ZipInputStream zipInputStream = new ZipInputStream(zipPackage.getInputStream())) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (!zipEntry.isDirectory() && zipEntry.getName().startsWith(artifactPathStartsWith)) {
                    zipContents.add(zipEntry.getName());
                }
            }
        }
        return zipContents;
    }

    private byte[] extractBytesFromZipEntry(ZipInputStream zipInputStream) throws IOException {

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] byteBuff = new byte[1024];
            int bytesRead = 0;
            while ((bytesRead = zipInputStream.read(byteBuff)) != -1) {
                baos.write(byteBuff, 0, bytesRead);
            }
            return baos.toByteArray();
        }
    }

    protected Resource createZipFromArtifacts(Map<String, byte[]> pathToArtifactsMap) throws IOException {

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(baos)) {

                for (Entry<String, byte[]> artifact : pathToArtifactsMap.entrySet()) {
                    zipOutputStream.putNextEntry(new ZipEntry(artifact.getKey()));
                    zipOutputStream.write(artifact.getValue());
                }
            }
            return new ByteArrayResource(baos.toByteArray());
        }
    }

}
