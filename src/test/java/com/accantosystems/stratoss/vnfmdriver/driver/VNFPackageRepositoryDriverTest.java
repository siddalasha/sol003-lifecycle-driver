package com.accantosystems.stratoss.vnfmdriver.driver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.accantosystems.stratoss.vnfmdriver.config.VNFMDriverProperties;
import com.google.common.io.ByteStreams;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({ "test" })
public class VNFPackageRepositoryDriverTest {

    @Autowired
    private VNFPackageRepositoryDriver vnfPackageDriver;

    @Test
    public void testGetVnfPackage() throws IOException {

        Resource vnfPackage = vnfPackageDriver.getVnfPackage("vMRF");
        assertThat(ByteStreams.toByteArray(vnfPackage.getInputStream())).isNotEmpty();
    }

    @Test
    public void testGetVnfPackageNotFound() throws IOException {

        assertThatThrownBy(() -> {
            vnfPackageDriver.getVnfPackage("not-present-id");
        }).isInstanceOf(VNFPackageRepositoryException.class)
                .hasMessageStartingWith("VNF Package not found in repository at location");
    }

    @Test
    public void testGetVnfPackageNoConfiguredRepoUrl() throws IOException {

        VNFMDriverProperties properties = new VNFMDriverProperties();
        properties.getPackageManagement().setPackageRepositoryUrl(null);
        VNFPackageRepositoryDriver vnfPackageDriver = new VNFPackageRepositoryDriver(properties);

        assertThatThrownBy(() -> {
            vnfPackageDriver.getVnfPackage("vMRF");
        }).isInstanceOf(VNFPackageRepositoryException.class)
                .hasMessageStartingWith("A valid VNF Package Repository URL must be configured.");
    }

}
