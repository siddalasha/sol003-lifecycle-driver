package org.etsi.sol003.lifecyclemanagement;

import java.util.Map;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Represents information about attribute modifications that were performed on an "Individual VNF instance" resource when changing the current VNF package.
 */
@Data
@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "Represents information about attribute modifications that were performed on an \"Individual VNF instance\" resource when changing the current VNF package.")
public class ModificationsTriggeredByVnfPkgChange {
    @ApiModelProperty(name = "VNF Configurable Properties", notes = "This attribute signals the modifications of the \"vnfConfigurableProperties\" attribute in \"VnfInstance\" performed by the operation and shall be present if that  attribute was modified during the operation.")
    private Map<String, String> vnfConfigurableProperties;
    @ApiModelProperty(name = "Metadata", notes = "This attribute signals the modifications of the \"metadata\" attribute in \"VnfInstance\" performed by the operation and shall be present if that attribute was modified during the operation.")
    private Map<String, String> metadata;
    @ApiModelProperty(name = "Extensions", notes = "This attribute signals the modifications of the \"extensions\" attribute in \"VnfInstance\" performed by the operation and shall be present if that attribute was modified during the operation.")
    private Map<String, String> extensions;
    @ApiModelProperty(name = "VnfdId", notes = "If present, this attribute signals the new value of the \"vnfdId\" attribute in \"VnfInstance\".")
    private String vnfdId;
    @ApiModelProperty(name = "vnf Provider", notes = "If present, this attribute signals the new value of the \"vnfProvider\" attribute in \"VnfInstance\".")
    private String vnfProvider;
    @ApiModelProperty(name = "vnf Product Name", notes = "If present, this attribute signals the new value of the \"vnfProductName\" attribute in \"VnfInstance\".")
    private String vnfProductName;
    @ApiModelProperty(name = "vnf Software Version", notes = "If present, this attribute signals the new value of the \"vnfSoftwareVersion\" attribute in \"VnfInstance\".")
    private String vnfSoftwareVersion;
    @ApiModelProperty(name = "vnfd Version", notes = "If present, this attribute signals the new value of the \"vnfdVersion\" attribute in \"VnfInstance\".")
    private String  vnfdVersion;

   

}
