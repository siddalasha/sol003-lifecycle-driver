package com.accantosystems.stratoss.vnfmdriver.model.alm;

import java.util.HashMap;
import java.util.Map;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * For use when interacting with resource manager API
 */
@ApiModel
public class ResourceManagerDeploymentLocation {

    @ApiModelProperty(position = 1, example = "dev-cloud", value = "Name of the deployment location")
    private String name;
    @ApiModelProperty(position = 2, example = "default-rm::Cloud", value = "Type identifier for the deployment location")
    private String type;
    @ApiModelProperty(position = 3, example = "", value = "Properties required to gain access to the deployment location VIM", required = true)
    private Map<String, String> properties = new HashMap<>();

    public ResourceManagerDeploymentLocation() {
        super();
    }

    public ResourceManagerDeploymentLocation(String name, String type) {
        super();
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "ResourceManagerDeploymentLocation [name=" + name + ", type=" + type + ", infrastructureSpecificProperties=" + properties.toString() + "]";
    }

}
