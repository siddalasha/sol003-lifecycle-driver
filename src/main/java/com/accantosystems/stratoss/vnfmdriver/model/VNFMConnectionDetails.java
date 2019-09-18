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
        NONE, BASIC, OAUTH2
    }

}
