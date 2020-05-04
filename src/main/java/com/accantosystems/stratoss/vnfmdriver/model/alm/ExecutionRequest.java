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
    @ApiModelProperty(value = "Driver files")
    private String driverFiles;
    @ApiModelProperty(value = "System Properties")
    private Map<String, PropertyValue> systemProperties = new HashMap<>();
    @ApiModelProperty(value = "Resource Properties")
    private Map<String, PropertyValue> resourceProperties = new HashMap<>();
    @ApiModelProperty(value = "Request Properties")
    private Map<String, PropertyValue> requestProperties = new HashMap<>();
    @ApiModelProperty(value = "Deployment Location")
    private ResourceManagerDeploymentLocation deploymentLocation;
    @ApiModelProperty(value = "Associated Topology")
    private Map<String, InternalResourceInstance> associatedTopology = new HashMap<>();

    public ExecutionRequest() {}

    public ExecutionRequest(String lifecycleName, String driverFiles, Map<String, PropertyValue> systemProperties,
                            Map<String, PropertyValue> resourceProperties, Map<String, PropertyValue> requestProperties, ResourceManagerDeploymentLocation deploymentLocation,
                            Map<String, InternalResourceInstance> associatedTopology) {
        this.lifecycleName = lifecycleName;
        this.driverFiles = driverFiles;
        this.systemProperties = systemProperties;
        this.requestProperties = requestProperties;
        this.resourceProperties = resourceProperties;
        this.deploymentLocation = deploymentLocation;
        this.associatedTopology = associatedTopology;
    }

    public String getLifecycleName() {
        return lifecycleName;
    }

    public void setLifecycleName(String lifecycleName) {
        this.lifecycleName = lifecycleName;
    }

    public String getDriverFiles() {
        return driverFiles;
    }

    public void setDriverFiles(String driverFiles) {
        this.driverFiles = driverFiles;
    }

    public Map<String, PropertyValue> getSystemProperties() {
        return systemProperties;
    }

    public void setSystemProperties(Map<String, PropertyValue> systemProperties) {
        this.systemProperties.putAll(systemProperties);
    }

    public Map<String, PropertyValue> getResourceProperties() {
        return resourceProperties;
    }

    public void setResourceProperties(Map<String, PropertyValue> resourceProperties) {
        this.resourceProperties.putAll(resourceProperties);
    }

    public Map<String, PropertyValue> getRequestProperties() {
        return requestProperties;
    }

    public void setRequestProperties(Map<String, PropertyValue> requestProperties) {
        this.requestProperties.putAll(requestProperties);
    }

    /**
     * Legacy support for getProperties method which may be referenced in Javascript libraries. Will return a filtered version of requestProperties with Strings values instead of StringPropertyValue values
     *
     * @return
     */
    public Map<String, String> getProperties() {
        return requestProperties.entrySet().stream().filter(entry -> entry.getValue() instanceof StringPropertyValue)
                                .collect(Collectors.toMap(Map.Entry::getKey, e -> ((StringPropertyValue) e.getValue()).getValue()));
    }

    public Map<String, InternalResourceInstance> getAssociatedTopology() {
        return associatedTopology;
    }

    public void setAssociatedTopology(Map<String, InternalResourceInstance> associatedTopology) {
        this.associatedTopology.putAll(associatedTopology);
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
                ", driverFiles='" + driverFiles + '\'' +
                ", systemProperties=" + LogSafeProperties.getLogSafeProperties(systemProperties) +
                ", resourceProperties=" + LogSafeProperties.getLogSafeProperties(resourceProperties) +
                ", requestProperties=" + LogSafeProperties.getLogSafeProperties(requestProperties) +
                ", associatedTopology=" + associatedTopology +
                ", deploymentLocation=" + deploymentLocation +
                '}';
    }
}