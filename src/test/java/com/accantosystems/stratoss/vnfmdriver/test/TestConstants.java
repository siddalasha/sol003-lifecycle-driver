package com.accantosystems.stratoss.vnfmdriver.test;

import static com.accantosystems.stratoss.vnfmdriver.config.VNFMDriverConstants.BASIC_AUTHENTICATION_PASSWORD;
import static com.accantosystems.stratoss.vnfmdriver.config.VNFMDriverConstants.BASIC_AUTHENTICATION_USERNAME;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.assertj.core.api.Condition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.accantosystems.stratoss.vnfmdriver.model.VNFMConnectionDetails;

public abstract class TestConstants {

    public static final String TEST_SERVER_BASE_URL = "http://localhost:8080";
    public static final String SECURE_TEST_SERVER_BASE_URL = "https://localhost:8080";
    public static final String EMPTY_JSON = "{}";
    public static final String BASIC_AUTHORIZATION_HEADER = "Basic YmFzaWNfdXNlcjpiYXNpY19wYXNzd29yZA==";

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

}
