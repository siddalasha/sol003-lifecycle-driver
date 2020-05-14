package com.accantosystems.stratoss.vnfmdriver.model.alm;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "Request used to execute lifecycle")
public class ExecutionRequest {

    @ApiModelProperty(value = "Lifecycle Name")
    private String lifecycleName;
    @ApiModelProperty(value = "Lifecycle Scripts")
    private String lifecycleScripts;
    @ApiModelProperty(value = "System Properties")
    private Map<String, PropertyValue> systemProperties = new HashMap<>();
    @ApiModelProperty(value = "Properties")
    private Map<String, PropertyValue> properties = new HashMap<>();
    @ApiModelProperty(value = "Deployment Location")
    private ResourceManagerDeploymentLocation deploymentLocation;

    public ExecutionRequest() {}

    public ExecutionRequest(String lifecycleName, String lifecycleScripts, Map<String, PropertyValue> systemProperties,
                            Map<String, PropertyValue> properties, ResourceManagerDeploymentLocation deploymentLocation) {
        this.lifecycleName = lifecycleName;
        this.lifecycleScripts = lifecycleScripts;
        this.systemProperties = systemProperties;
        this.properties = properties;
        this.deploymentLocation = deploymentLocation;
    }

    public String getLifecycleName() {
        return lifecycleName;
    }

    public void setLifecycleName(String lifecycleName) {
        this.lifecycleName = lifecycleName;
    }

    public String getLifecycleScripts() {
        return lifecycleScripts;
    }

    public void setLifecycleScripts(String lifecycleScripts) {
        this.lifecycleScripts = lifecycleScripts;
    }

    public Map<String, PropertyValue> getSystemProperties() {
        return systemProperties;
    }

    public Map<String, PropertyValue> getPropertiesAsPropertyValues() {
        return properties;
    }

    /**
     * Legacy support for getProperties method which may be referenced in Javascript libraries. Will return a filtered version of properties with String values instead of PropertyValue values
     *
     * @return map containing property values as simple String types
     */
    public Map<String, String> getProperties() {
        return properties.entrySet().stream().filter(entry -> entry.getValue() instanceof StringPropertyValue)
                                .collect(Collectors.toMap(Map.Entry::getKey, e -> ((StringPropertyValue) e.getValue()).getValue()));
    }

    public ResourceManagerDeploymentLocation getDeploymentLocation() {
        return deploymentLocation;
    }

    public void setDeploymentLocation(ResourceManagerDeploymentLocation deploymentLocation) {
        this.deploymentLocation = deploymentLocation;
    }

    @Override
    public String toString() {
        return "ExecutionRequest{" +
                "lifecycleName='" + lifecycleName + '\'' +
                ", lifecycleScripts='" + lifecycleScripts + '\'' +
                ", systemProperties=" + systemProperties +
                ", properties=" + properties +
                ", deploymentLocation=" + deploymentLocation +
                '}';
    }
}