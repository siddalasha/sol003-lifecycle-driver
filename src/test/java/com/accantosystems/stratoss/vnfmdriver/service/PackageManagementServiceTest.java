package com.accantosystems.stratoss.vnfmdriver.service;

import static com.accantosystems.stratoss.vnfmdriver.test.TestConstants.loadFileIntoByteArray;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;

import org.etsi.sol003.packagemanagement.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.accantosystems.stratoss.vnfmdriver.driver.VNFPackageNotFoundException;
import com.accantosystems.stratoss.vnfmdriver.driver.VNFPackageRepositoryDriver;
import com.accantosystems.stratoss.vnfmdriver.test.TestConstants;
import com.google.common.io.ByteStreams;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({ "test" })
public class PackageManagementServiceTest {

    private static final String VNF_PACKAGE_FILENAME = "examples/VnfPackage-vMRF.zip";
    private static final String VNF_PACKAGE_FILENAME_MISSING_TOSCA_METEDATA = "examples/VnfPackage-MissingToscaMetadata-vMRF.zip";
    private static final String VNF_PACKAGE_FILENAME_MULTIPLE_DEFINITIONS = "examples/VnfPackage-MultipleDefinitions-vMRF.zip";
    private static final String VNF_PACKAGE_FILENAME_NO_DEFINITIONS = "examples/VnfPackage-NoDefinitions-vMRF.zip";
    private static final String VNF_PACKAGE_ID = "vMRF";

    @MockBean
    private VNFPackageRepositoryDriver vnfPackageDriver;

    @Autowired
    private PackageManagementService packageManagementService;

    @Test
    public void testGetVnfPackageInfo() throws Exception {

        byte[] vnfPackageAsByteArray = loadFileIntoByteArray(VNF_PACKAGE_FILENAME);
        ByteArrayResource vnfPackageAsResource = new ByteArrayResource(vnfPackageAsByteArray);
        when(vnfPackageDriver.getVnfPackage(eq(VNF_PACKAGE_ID))).thenReturn(vnfPackageAsResource);

        VnfPkgInfo vnfPackageInfo = packageManagementService.getVnfPackageInfo(VNF_PACKAGE_ID);
        assertThat(vnfPackageInfo).isNotNull();
        assertThat(vnfPackageInfo.getId()).isEqualTo(VNF_PACKAGE_ID);
        assertThat(vnfPackageInfo.getVnfProductName()).isEqualTo("vMRF");
        assertThat(vnfPackageInfo.getVnfProvider()).isEqualTo("Acme");
        assertThat(vnfPackageInfo.getOnboardingState()).isEqualTo(PackageOnboardingStateType.ONBOARDED);
        assertThat(vnfPackageInfo.getOperationalState()).isEqualTo(PackageOperationalStateType.ENABLED);
        assertThat(vnfPackageInfo.getAdditionalArtifacts()).isNotEmpty(); // TODO verify these contents better once for format of the VNF package is better understood
        assertThat(vnfPackageInfo.getSoftwareImages()).isNotEmpty(); // TODO verify these contents better once for format of the VNF package is better understood

        // TODO further verifications against the info contents
    }

    @Test
    public void testGetVnfPackageInfoPackageNotFound() throws Exception {

        String packageId = "not-existant-package-id";

        when(vnfPackageDriver.getVnfPackage(eq(packageId))).thenThrow(new VNFPackageNotFoundException("Not Found"));

        assertThatThrownBy(() -> {
            packageManagementService.getVnfPackageInfo(packageId);
        }).isInstanceOf(VNFPackageNotFoundException.class);

    }

    @Test
    public void testGetVnfPackageInfoInvalidZip() throws Exception {

        byte[] vnfPackageAsByteArray = loadFileIntoByteArray(VNF_PACKAGE_FILENAME_MISSING_TOSCA_METEDATA);
        ByteArrayResource vnfPackageAsResource = new ByteArrayResource(vnfPackageAsByteArray);
        when(vnfPackageDriver.getVnfPackage(eq(VNF_PACKAGE_ID))).thenReturn(vnfPackageAsResource);

        assertThatThrownBy(() -> {
            packageManagementService.getVnfPackageInfo(VNF_PACKAGE_ID);
        }).isInstanceOf(VNFPackageExtractionException.class).hasMessage("Unable to locate Tosca Metadata File on path [TOSCA-Metadata/TOSCA.meta] within Vnf Package with id [vMRF]");

    }

    @Test
    public void testGetVnfPackageYamlDescriptor() throws Exception {

        byte[] vnfPackageAsByteArray = loadFileIntoByteArray(VNF_PACKAGE_FILENAME);
        ByteArrayResource vnfPackageAsResource = new ByteArrayResource(vnfPackageAsByteArray);
        when(vnfPackageDriver.getVnfPackage(eq(VNF_PACKAGE_ID))).thenReturn(vnfPackageAsResource);

        String vnfd = packageManagementService.getVnfdAsYaml(VNF_PACKAGE_ID);
        assertThat(vnfd).isNotBlank();

    }

    @Test
    public void testGetVnfPackageYamlDescriptorPackageNotFound() throws Exception {

        String packageId = "not-existant-package-id";

        when(vnfPackageDriver.getVnfPackage(eq(packageId))).thenThrow(new VNFPackageNotFoundException("Not Found"));

        assertThatThrownBy(() -> {
            packageManagementService.getVnfdAsYaml(packageId);
        }).isInstanceOf(VNFPackageNotFoundException.class);

    }

    @Test
    public void testGetVnfPackageYamlDescriptorInvalidZip() throws Exception {

        byte[] vnfPackageAsByteArray = loadFileIntoByteArray(VNF_PACKAGE_FILENAME_MISSING_TOSCA_METEDATA);
        ByteArrayResource vnfPackageAsResource = new ByteArrayResource(vnfPackageAsByteArray);
        when(vnfPackageDriver.getVnfPackage(eq(VNF_PACKAGE_ID))).thenReturn(vnfPackageAsResource);

        assertThatThrownBy(() -> {
            packageManagementService.getVnfdAsYaml(VNF_PACKAGE_ID);
        }).isInstanceOf(VNFPackageExtractionException.class).hasMessage("Unable to locate Tosca Metadata File on path [TOSCA-Metadata/TOSCA.meta] within Vnf Package with id [vMRF]");

    }

    @Test
    public void testGetVnfPackageYamlNoDefinitions() throws Exception {

        byte[] vnfPackageAsByteArray = loadFileIntoByteArray(VNF_PACKAGE_FILENAME_NO_DEFINITIONS);
        ByteArrayResource vnfPackageAsResource = new ByteArrayResource(vnfPackageAsByteArray);
        when(vnfPackageDriver.getVnfPackage(eq(VNF_PACKAGE_ID))).thenReturn(vnfPackageAsResource);

        assertThatThrownBy(() -> {
            packageManagementService.getVnfdAsYaml(VNF_PACKAGE_ID);
        }).isInstanceOf(VNFPackageExtractionException.class).hasMessage("Unable to find any Definitions within VnfPackage with id [vMRF]");

    }

    @Test
    public void testGetVnfPackageYamlMultipleDefinitions() throws Exception {

        byte[] vnfPackageAsByteArray = loadFileIntoByteArray(VNF_PACKAGE_FILENAME_MULTIPLE_DEFINITIONS);
        ByteArrayResource vnfPackageAsResource = new ByteArrayResource(vnfPackageAsByteArray);
        when(vnfPackageDriver.getVnfPackage(eq(VNF_PACKAGE_ID))).thenReturn(vnfPackageAsResource);

        assertThatThrownBy(() -> {
            packageManagementService.getVnfdAsYaml(VNF_PACKAGE_ID);
        }).isInstanceOf(UnexpectedPackageContentsException.class).hasMessage("Found multiple VNFDs when expecting only one within VnfPackage with id [vMRF]");

    }

    @Test
    public void testGetVnfPackageZipDescriptor() throws Exception {

        byte[] vnfPackageAsByteArray = loadFileIntoByteArray(VNF_PACKAGE_FILENAME);
        ByteArrayResource vnfPackageAsResource = new ByteArrayResource(vnfPackageAsByteArray);
        when(vnfPackageDriver.getVnfPackage(eq(VNF_PACKAGE_ID))).thenReturn(vnfPackageAsResource);

        Resource vnfdPackage = packageManagementService.getVnfdAsZip(VNF_PACKAGE_ID);
        assertThat(vnfdPackage).isNotNull();
        List<String> zipContents = TestConstants.listZipContents(vnfdPackage.getInputStream());
        assertThat(zipContents).containsExactlyInAnyOrder("Definitions/MRF.yaml", "TOSCA-Metadata/TOSCA.meta");

    }

    @Test
    public void testGetVnfPackageContent() throws Exception {

        byte[] vnfPackageAsByteArray = loadFileIntoByteArray(VNF_PACKAGE_FILENAME);
        ByteArrayResource vnfPackageAsResource = new ByteArrayResource(vnfPackageAsByteArray);
        when(vnfPackageDriver.getVnfPackage(eq(VNF_PACKAGE_ID))).thenReturn(vnfPackageAsResource);

        Resource vnfPackageResponse = packageManagementService.getVnfPackageContent(VNF_PACKAGE_ID, "");
        assertThat(ByteStreams.toByteArray(vnfPackageResponse.getInputStream())).isEqualTo(vnfPackageAsByteArray);

    }

    @Test
    public void testGetVnfPackageArtifact() throws Exception {

        byte[] vnfPackageAsByteArray = loadFileIntoByteArray(VNF_PACKAGE_FILENAME);
        ByteArrayResource vnfPackageAsResource = new ByteArrayResource(vnfPackageAsByteArray);
        when(vnfPackageDriver.getVnfPackage(eq(VNF_PACKAGE_ID))).thenReturn(vnfPackageAsResource);

        Resource vnfPackageResponse = packageManagementService.getVnfPackageArtifact(VNF_PACKAGE_ID, "Scripts/install.sh", null);
        assertThat(ByteStreams.toByteArray(vnfPackageResponse.getInputStream())).isNotNull();

    }

    @Test
    public void testGetVnfPackageInfoListValidArtifacts() throws Exception {

        byte[] vnfPackageAsByteArray = loadFileIntoByteArray(VNF_PACKAGE_FILENAME);
        ByteArrayResource vnfPackageAsResource = new ByteArrayResource(vnfPackageAsByteArray);
        when(vnfPackageDriver.getVnfPackage(eq(VNF_PACKAGE_ID))).thenReturn(vnfPackageAsResource);

        VnfPkgInfo vnfPackageInfo = packageManagementService.getVnfPackageInfo(VNF_PACKAGE_ID);
        assertThat(vnfPackageInfo.getAdditionalArtifacts()).isNotEmpty();
        assertThat(vnfPackageInfo.getSoftwareImages()).isNotEmpty();

        // verify that each additional artifact listed can be found in the package
        for (VnfPackageArtifactInfo vnfArtifact : vnfPackageInfo.getAdditionalArtifacts()) {
            assertThat(packageManagementService.getVnfPackageArtifact(VNF_PACKAGE_ID, vnfArtifact.getArtifactPath(), null)).isNotNull();
        }

        // verify that each software image listed can be found in the package
        for (VnfPackageSoftwareImageInfo softwareImage : vnfPackageInfo.getSoftwareImages()) {
            assertThat(packageManagementService.getVnfPackageArtifact(VNF_PACKAGE_ID, softwareImage.getImagePath(), null)).isNotNull();
        }

    }

}
