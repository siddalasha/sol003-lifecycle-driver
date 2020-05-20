package com.accantosystems.stratoss.vnfmdriver.model.alm;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;

public class LogSafeProperties {

    public static final String OBFUSCATED_VALUE = "######";

    public static Map<String, String> getLogSafeProperties(final Map<String, PropertyValue> properties) {
        return properties != null ? properties.entrySet().stream()
                                              .map(entry -> {
                                                  if (entry.getValue().getType() == PropertyType.KEY) {
                                                      final KeyPropertyValue keyPropertyValue = (KeyPropertyValue) entry.getValue();
                                                      keyPropertyValue.setPrivateKey(OBFUSCATED_VALUE);
                                                  }
                                                  return new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().toString());
                                              })
                                              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)) : null;
    }
}
