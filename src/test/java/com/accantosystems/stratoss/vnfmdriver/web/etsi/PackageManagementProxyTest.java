package com.accantosystems.stratoss.vnfmdriver.web.etsi;

import static com.accantosystems.stratoss.vnfmdriver.test.TestConstants.loadFileIntoByteArray;
import static com.accantosystems.stratoss.vnfmdriver.test.TestConstants.loadFileIntoString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.*;

import org.etsi.sol003.common.ProblemDetails;
import org.etsi.sol003.packagemanagement.VnfPkgInfo;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;

import com.accantosystems.stratoss.vnfmdriver.config.VNFMDriverProperties;
import com.accantosystems.stratoss.vnfmdriver.driver.VNFPackageNotFoundException;
import com.accantosystems.stratoss.vnfmdriver.service.ContentRangeNotSatisfiableException;
import com.accantosystems.stratoss.vnfmdriver.service.PackageManagementService;
import com.accantosystems.stratoss.vnfmdriver.service.PackageStateConflictException;
import com.accantosystems.stratoss.vnfmdriver.service.UnexpectedPackageContentsException;
import com.accantosystems.stratoss.vnfmdriver.test.TestConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({ "test-packagemgmt-proxy" })
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@Ignore
public class PackageManagementProxyTest {

    public static final String PACKAGE_MANAGEMENT_BASE_ENDPOINT = "/vnfpkgm/v1/vnf_packages";

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void testProxiedPackageRequest() throws Exception {

        String vnfPkgId = TestConstants.TEST_VNF_PKG_ID;

        final ResponseEntity<String> responseEntity = testRestTemplate.exchange(PACKAGE_MANAGEMENT_BASE_ENDPOINT + "?vnfdId={vnfPkgId}", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()),
                                                                                          String.class, vnfPkgId);

        // won't be able to verify anything more without the proxy endpoint in place, but check our own auth doesn't prevent this proxy call
        assertThat(responseEntity.getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);

    }

}
