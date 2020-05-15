package com.accantosystems.stratoss.vnfmdriver.model.alm;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = StringPropertyValue.class, name = "string"),
        @JsonSubTypes.Type(value = KeyPropertyValue.class, name = "key")
})
public abstract class PropertyValue {
    private PropertyType type;

    public PropertyValue() {
    }

    public PropertyValue(PropertyType type) {
        this.type = type;
    }

    public PropertyType getType() {
        return type;
    }

    public void setType(PropertyType type) {
        this.type = type;
    }

}