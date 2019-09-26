package com.accantosystems.stratoss.vnfmdriver.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class VNFMConnectionDetails {

    private final String apiRoot;
    private final AuthenticationType authenticationType;
    private final Map<String, String> authenticationProperties = new HashMap<>();

    public enum AuthenticationType {
        NONE, BASIC, OAUTH2, SESSION;

        public static AuthenticationType valueOfIgnoreCase(String value) {
            for (AuthenticationType type : AuthenticationType.values()) {
                if (type.name().equalsIgnoreCase(value)) {
                    return type;
                }
            }
            return null;
        }

    }

}
