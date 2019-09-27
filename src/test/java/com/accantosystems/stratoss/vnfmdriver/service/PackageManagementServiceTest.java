package com.accantosystems.stratoss.vnfmdriver.service;

import static com.accantosystems.stratoss.vnfmdriver.test.TestConstants.loadFileIntoByteArray;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.etsi.sol003.packagemanagement.PackageOnboardingStateType;
import org.etsi.sol003.packagemanagement.PackageOperationalStateType;
import org.etsi.sol003.packagemanagement.VnfPkgInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.accantosystems.stratoss.vnfmdriver.driver.VNFPackageRepositoryDriver;
import com.google.common.io.ByteStreams;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({ "test" })
public class PackageManagementServiceTest {

    private static final String VNF_PACKAGE_FILENAME = "examples/VnfPackage-vMRF.zip";
    private static final String VNF_PACKAGE_ID = "vMRF";

    @MockBean
    private VNFPackageRepositoryDriver vnfPackageDriver;

    @Autowired
    private PackageManagementService packageManagementService;

    @Test
    public void testGetVnfPackageInfo() throws Exception {

        byte[] vnfPackageAsByteArray = loadFileIntoByteArray(VNF_PACKAGE_FILENAME);
        ByteArrayResource vnfdAsResource = new ByteArrayResource(vnfPackageAsByteArray);
        when(vnfPackageDriver.getVnfPackage(eq(VNF_PACKAGE_ID))).thenReturn(vnfdAsResource);

        VnfPkgInfo vnfPackageInfo = packageManagementService.getVnfPackageInfo(VNF_PACKAGE_ID);
        assertThat(vnfPackageInfo).isNotNull();
        assertThat(vnfPackageInfo.getId()).isEqualTo(VNF_PACKAGE_ID);
        assertThat(vnfPackageInfo.getVnfProductName()).isEqualTo("vMRF");
        assertThat(vnfPackageInfo.getVnfProvider()).isEqualTo("Acme");
        assertThat(vnfPackageInfo.getOnboardingState()).isEqualTo(PackageOnboardingStateType.ONBOARDED);
        assertThat(vnfPackageInfo.getOperationalState()).isEqualTo(PackageOperationalStateType.ENABLED);
        // TODO further verifications against the info contents
    }

    @Test
    public void testGetVnfPackageYamlDescriptor() throws Exception {

        byte[] vnfPackageAsByteArray = loadFileIntoByteArray(VNF_PACKAGE_FILENAME);
        ByteArrayResource vnfdAsResource = new ByteArrayResource(vnfPackageAsByteArray);
        when(vnfPackageDriver.getVnfPackage(eq(VNF_PACKAGE_ID))).thenReturn(vnfdAsResource);

        String vnfd = packageManagementService.getVnfdAsYaml(VNF_PACKAGE_ID);
        assertThat(vnfd).isNotBlank();

    }

    @Test
    public void testGetVnfPackageZipDescriptor() throws Exception {

        byte[] vnfPackageAsByteArray = loadFileIntoByteArray(VNF_PACKAGE_FILENAME);
        ByteArrayResource vnfdAsResource = new ByteArrayResource(vnfPackageAsByteArray);
        when(vnfPackageDriver.getVnfPackage(eq(VNF_PACKAGE_ID))).thenReturn(vnfdAsResource);

        Resource vnfdPackage = packageManagementService.getVnfdAsZip(VNF_PACKAGE_ID);
        assertThat(vnfdPackage).isNotNull();
        // TODO check zip contents

    }

    @Test
    public void testGetVnfPackageContent() throws Exception {

        byte[] vnfPackageAsByteArray = loadFileIntoByteArray(VNF_PACKAGE_FILENAME);
        ByteArrayResource vnfdAsResource = new ByteArrayResource(vnfPackageAsByteArray);
        when(vnfPackageDriver.getVnfPackage(eq(VNF_PACKAGE_ID))).thenReturn(vnfdAsResource);

        Resource vnfPackageResponse = packageManagementService.getVnfPackageContent(VNF_PACKAGE_ID, "");
        assertThat(ByteStreams.toByteArray(vnfPackageResponse.getInputStream())).isEqualTo(vnfPackageAsByteArray);

    }

    @Test
    public void testGetVnfPackageArtifact() throws Exception {

        byte[] vnfPackageAsByteArray = loadFileIntoByteArray(VNF_PACKAGE_FILENAME);
        ByteArrayResource vnfdAsResource = new ByteArrayResource(vnfPackageAsByteArray);
        when(vnfPackageDriver.getVnfPackage(eq(VNF_PACKAGE_ID))).thenReturn(vnfdAsResource);

        Resource vnfPackageResponse = packageManagementService.getVnfPackageArtifact(VNF_PACKAGE_ID, "Scripts/install.sh", null);
        assertThat(ByteStreams.toByteArray(vnfPackageResponse.getInputStream())).isNotNull();

    }

}
