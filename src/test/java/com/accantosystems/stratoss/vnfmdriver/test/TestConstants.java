package com.accantosystems.stratoss.vnfmdriver.test;

import static com.accantosystems.stratoss.vnfmdriver.config.VNFMDriverConstants.BASIC_AUTHENTICATION_PASSWORD;
import static com.accantosystems.stratoss.vnfmdriver.config.VNFMDriverConstants.BASIC_AUTHENTICATION_USERNAME;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import org.assertj.core.api.Condition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.accantosystems.stratoss.vnfmdriver.model.VNFMConnectionDetails;
import com.accantosystems.stratoss.vnfmdriver.model.alm.ResourceManagerDeploymentLocation;

public abstract class TestConstants {


    public static final String TEST_SERVER_BASE_URL = "http://localhost:8080";
    public static final String SECURE_TEST_SERVER_BASE_URL = "https://localhost:8080";
    public static final String BASIC_AUTHORIZATION_HEADER = "Basic YmFzaWNfdXNlcjpiYXNpY19wYXNzd29yZA==";
    public static final String NOTIFICATIONS_ENDPOINT = "http://localhost:8080/vnflcm/v1/notifications";
    public static final String EMPTY_JSON = "{}";
    public static final String TEST_EXCEPTION_MESSAGE = "TestExceptionMessage";

    public static final String TEST_VNF_INSTANCE_ID = "cc3d9824-8267-4b1c-8456-3f1cdd94d620";
    public static final String TEST_VNF_LCM_OP_OCC_ID = "8dbe6621-f6b9-49ba-878b-26803f107f27";
    public static final String TEST_LCCN_SUBSCRIPTION_ID = "2fdffb76-5c74-44ab-a38f-40b302ba5ec9";
    public static final String TEST_VNF_PKG_ID = "1472c841-c4ac-418c-bd45-e2e7f7638336";

    public static final ResourceManagerDeploymentLocation TEST_DL = new ResourceManagerDeploymentLocation("test-location", "etsi-sol003");
    public static final HttpEntity<String> EMPTY_JSON_ENTITY;
    public static final VNFMConnectionDetails VNFM_CONNECTION_DETAILS_NO_AUTHENTICATION = new VNFMConnectionDetails(TEST_SERVER_BASE_URL, VNFMConnectionDetails.AuthenticationType.NONE);
    public static final VNFMConnectionDetails VNFM_CONNECTION_DETAILS_BASIC_AUTHENTICATION = new VNFMConnectionDetails(SECURE_TEST_SERVER_BASE_URL, VNFMConnectionDetails.AuthenticationType.BASIC);

    static {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        EMPTY_JSON_ENTITY = new HttpEntity<>(EMPTY_JSON, headers);

        VNFM_CONNECTION_DETAILS_BASIC_AUTHENTICATION.getAuthenticationProperties().put(BASIC_AUTHENTICATION_USERNAME, "basic_user");
        VNFM_CONNECTION_DETAILS_BASIC_AUTHENTICATION.getAuthenticationProperties().put(BASIC_AUTHENTICATION_PASSWORD, "basic_password");
    }

    public static final Condition<String> UUID_CONDITION = new Condition<String>("UUID Condition") {
        @Override
        public boolean matches(String value) {
            try {
                UUID.fromString(value);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    };

    public static String loadFileIntoString(final String fileName) throws IOException {
        // This appears to be the fastest way to load a file into a String (circa 2017 (JDK8))
        // http://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string

        try (
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                InputStream inputStream = TestConstants.class.getResourceAsStream(fileName.startsWith("/") ? fileName : "/" + fileName)
        ) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return result.toString(StandardCharsets.UTF_8.name());
        }
    }

    public static String loadZipIntoBase64String(final String fileName) throws IOException {
        try (
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                InputStream inputStream = TestConstants.class.getResourceAsStream(fileName.startsWith("/") ? fileName : "/" + fileName)
        ) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return Base64.getEncoder().encodeToString(result.toByteArray());
        }
    }

}
