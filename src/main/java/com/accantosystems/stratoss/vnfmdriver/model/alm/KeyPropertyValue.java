package com.accantosystems.stratoss.vnfmdriver.model.alm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeyPropertyValue extends PropertyValue {

    private String keyName;
    private String privateKey;
    private String publicKey;

    public KeyPropertyValue() {
    }

    public KeyPropertyValue(String keyName, String privateKey) {
        this();
        this.keyName = keyName;
        this.privateKey = privateKey;
    }

    public KeyPropertyValue(String keyName, String privateKey, String publicKey) {
        this();
        this.keyName = keyName;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        KeyPropertyValue that = (KeyPropertyValue) o;

        if (!keyName.equals(that.keyName)) {
            return false;
        }
        if (!privateKey.equals(that.privateKey)) {
            return false;
        }
        return publicKey != null ? publicKey.equals(that.publicKey) : that.publicKey == null;
    }

    @Override
    public int hashCode() {
        int result = keyName.hashCode();
        result = 31 * result + privateKey.hashCode();
        result = 31 * result + (publicKey != null ? publicKey.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "KeyPropertyValue{" +
                "keyName='" + keyName + '\'' +
                ", privateKey='" + LogSafeProperties.OBFUSCATED_VALUE + "'" +
                ", publicKey='" + publicKey + '\'' +
                '}';
    }
}