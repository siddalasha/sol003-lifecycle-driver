package com.accantosystems.stratoss.vnfmdriver.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.assertj.core.api.Condition;

public interface TestConstants {

    String EMPTY_JSON = "{}";

    Condition<String> UUID_CONDITION = new Condition<String>("UUID Condition") {
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

    static String loadFileIntoString(final String fileName) throws IOException {
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
