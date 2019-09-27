package com.accantosystems.stratoss.vnfmdriver.web.etsi;

import static com.accantosystems.stratoss.vnfmdriver.test.TestConstants.loadFileIntoByteArray;
import static com.accantosystems.stratoss.vnfmdriver.test.TestConstants.loadFileIntoString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.UUID;

import org.etsi.sol003.common.ProblemDetails;
import org.etsi.sol003.packagemanagement.VnfPkgInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.accantosystems.stratoss.vnfmdriver.service.PackageManagementService;
import com.accantosystems.stratoss.vnfmdriver.test.TestConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({ "test" })
public class PackageManagementControllerTest {

    public static final String PACKAGE_MANAGEMENT_BASE_ENDPOINT = "/vnfpkgm/v1/vnf_packages";
    public static final String PACKAGE_MANAGEMENT_VNF_PKG_INFO_ENDPOINT = PACKAGE_MANAGEMENT_BASE_ENDPOINT + "/{vnfPkgId}";
    public static final String PACKAGE_MANAGEMENT_VNFD_ENDPOINT = PACKAGE_MANAGEMENT_BASE_ENDPOINT + "/{vnfPkgId}/vnfd";
    public static final String PACKAGE_MANAGEMENT_PACKAGE_CONTENT_ENDPOINT = PACKAGE_MANAGEMENT_BASE_ENDPOINT + "/{vnfPkgId}/package_content";
    public static final String PACKAGE_MANAGEMENT_PACKAGE_ARTIFACT_ENDPOINT = PACKAGE_MANAGEMENT_BASE_ENDPOINT + "/{vnfPkgId}/artifacts/{artifactPath}";

    private static final String VNF_PACKAGE_FILENAME = "examples/VnfPackage-vMRF.zip";

    @MockBean
    private PackageManagementService packageManagementService;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testRequestPackageInfo() throws Exception {

        String vnfd = loadFileIntoString("examples/VnfPkgInfo.json");
        VnfPkgInfo vnfPkgInfo = objectMapper.readValue(vnfd, VnfPkgInfo.class);
        String vnfPkgId = TestConstants.TEST_VNF_PKG_ID;
        when(packageManagementService.getVnfPackageInfo(eq(vnfPkgId))).thenReturn(vnfPkgInfo);

        final ResponseEntity<VnfPkgInfo> responseEntity = testRestTemplate.withBasicAuth("user", "password").getForEntity(PACKAGE_MANAGEMENT_VNF_PKG_INFO_ENDPOINT, VnfPkgInfo.class, vnfPkgId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isEqualTo(vnfPkgId);
    }

    @Test
    public void testQueryPackageInfoNotImplemented() throws Exception {

        // TODO remove this test method when implementing GET /vnfpkgm/v1/vnf_packages

        final ResponseEntity<ProblemDetails> responseEntity = testRestTemplate.withBasicAuth("user", "password").getForEntity(PACKAGE_MANAGEMENT_BASE_ENDPOINT, ProblemDetails.class);

        // This method is not yet implemented so ensure all callers will receive status 501
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);
        assertThat(responseEntity.getBody().getDetail()).isEqualTo("Query VNF Packages Info API not yet implemented.");

    }

    @Test
    public void testRequestVNFDAsZip() throws Exception {

        byte[] vnfdAsByteArray = loadFileIntoByteArray(VNF_PACKAGE_FILENAME);
        ByteArrayResource vnfdAsResource = new ByteArrayResource(vnfdAsByteArray);

        String vnfPkgId = UUID.randomUUID().toString();

        when(packageManagementService.getVnfdAsZip(eq(vnfPkgId))).thenReturn(vnfdAsResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.parseMediaType("application/zip")));
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        final ResponseEntity<Resource> responseEntity = testRestTemplate.withBasicAuth("user", "password")
                .exchange(PACKAGE_MANAGEMENT_VNFD_ENDPOINT, HttpMethod.GET, httpEntity, Resource.class, vnfPkgId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(ByteStreams.toByteArray(responseEntity.getBody().getInputStream())).isEqualTo(vnfdAsByteArray);
    }

    @Test
    public void testRequestVNFDAsYaml() throws Exception {

        String vnfd = loadFileIntoString("examples/Vnfd.yaml");

        String vnfPkgId = UUID.randomUUID().toString();

        when(packageManagementService.getVnfdAsYaml(eq(vnfPkgId))).thenReturn(vnfd);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.TEXT_PLAIN));
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        final ResponseEntity<String> responseEntity = testRestTemplate.withBasicAuth("user", "password")
                .exchange(PACKAGE_MANAGEMENT_VNFD_ENDPOINT, HttpMethod.GET, httpEntity, String.class, vnfPkgId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody()).isEqualTo(vnfd);
    }

    @Test
    public void testRequestVNFDAsZipAndYaml() throws Exception {

        byte[] vnfdAsByteArray = loadFileIntoByteArray(VNF_PACKAGE_FILENAME);
        ByteArrayResource vnfdAsResource = new ByteArrayResource(vnfdAsByteArray);

        String vnfPkgId = UUID.randomUUID().toString();

        when(packageManagementService.getVnfdAsZip(eq(vnfPkgId))).thenReturn(vnfdAsResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.parseMediaType("application/zip"), MediaType.TEXT_PLAIN));
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        final ResponseEntity<Resource> responseEntity = testRestTemplate.withBasicAuth("user", "password")
                .exchange(PACKAGE_MANAGEMENT_VNFD_ENDPOINT, HttpMethod.GET, httpEntity, Resource.class, vnfPkgId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(ByteStreams.toByteArray(responseEntity.getBody().getInputStream())).isEqualTo(vnfdAsByteArray);
    }

    @Test
    public void testRequestVNFDUnexpectedContentTypes() throws Exception {

        String vnfPkgId = UUID.randomUUID().toString();

        // Check with no Accept types specified
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<Resource> responseEntity = testRestTemplate.withBasicAuth("user", "password")
                .exchange(PACKAGE_MANAGEMENT_VNFD_ENDPOINT, HttpMethod.GET, httpEntity, Resource.class, vnfPkgId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_ACCEPTABLE);

        // Check with only invalid Accept types specified
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        httpEntity = new HttpEntity<>(headers);
        responseEntity = testRestTemplate.withBasicAuth("user", "password")
                .exchange(PACKAGE_MANAGEMENT_VNFD_ENDPOINT, HttpMethod.GET, httpEntity, Resource.class, vnfPkgId);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_ACCEPTABLE);

        // Check with additional invalid Accept types specified
        headers.setAccept(Arrays.asList(MediaType.parseMediaType("application/zip"), MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON));
        httpEntity = new HttpEntity<>(headers);
        responseEntity = testRestTemplate.withBasicAuth("user", "password")
                .exchange(PACKAGE_MANAGEMENT_VNFD_ENDPOINT, HttpMethod.GET, httpEntity, Resource.class, vnfPkgId);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_ACCEPTABLE);
    }

    @Test
    public void testRequestVNFPackageContent() throws Exception {

        byte[] vnfPackageAsByteArray = loadFileIntoByteArray(VNF_PACKAGE_FILENAME);
        ByteArrayResource vnfPackageAsResource = new ByteArrayResource(vnfPackageAsByteArray);

        String vnfPkgId = UUID.randomUUID().toString();

        when(packageManagementService.getVnfPackageContent(eq(vnfPkgId), nullable(String.class))).thenReturn(vnfPackageAsResource);

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        final ResponseEntity<Resource> responseEntity = testRestTemplate.withBasicAuth("user", "password")
                .exchange(PACKAGE_MANAGEMENT_PACKAGE_CONTENT_ENDPOINT, HttpMethod.GET, httpEntity, Resource.class, vnfPkgId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.parseMediaType("application/zip"));
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(ByteStreams.toByteArray(responseEntity.getBody().getInputStream())).isEqualTo(vnfPackageAsByteArray);
    }

    @Test
    public void testRequestVNFPackageArtifact() throws Exception {

        String vnfPackageArtifact = loadFileIntoString("examples/Vnfd.yaml");
        ByteArrayResource vnfPackageArtifactAsResource = new ByteArrayResource(vnfPackageArtifact.getBytes());

        String vnfPkgId = UUID.randomUUID().toString();
        String artifactPath = "Vnfd.yaml";

        when(packageManagementService.getVnfPackageArtifact(eq(vnfPkgId), eq(artifactPath), nullable(String.class))).thenReturn(vnfPackageArtifactAsResource);

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        final ResponseEntity<Resource> responseEntity = testRestTemplate.withBasicAuth("user", "password")
                .exchange(PACKAGE_MANAGEMENT_PACKAGE_ARTIFACT_ENDPOINT, HttpMethod.GET, httpEntity, Resource.class, vnfPkgId, artifactPath);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(ByteStreams.toByteArray(responseEntity.getBody().getInputStream())).isEqualTo(vnfPackageArtifact.getBytes());
    }

    // TODO - further testing
    // - partial context requests
    // - conflicted requests
    // - unsuitable range requests
}
