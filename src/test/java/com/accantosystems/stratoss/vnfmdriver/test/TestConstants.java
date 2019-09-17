package com.accantosystems.stratoss.vnfmdriver.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.assertj.core.api.Condition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public abstract class TestConstants {

    public static final String EMPTY_JSON = "{}";

    public static final HttpEntity<String> EMPTY_JSON_ENTITY;

    static {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        EMPTY_JSON_ENTITY = new HttpEntity<>(EMPTY_JSON, headers);
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
