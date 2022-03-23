package org.etsi.sol003.lifecyclemanagement;

import java.util.List;
import java.util.Map;

import org.etsi.sol003.common.ResourceHandle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Represents the information that allows addressing a virtualised resource that is used by an internal VL instance in a VNF instance.
 */
@Data
@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "Represents the information that allows addressing a virtualised resource that is used by an internal VL instance in a VNF instance.")
public class VnfVirtualLinkResourceInfo {

    @ApiModelProperty(name = "Id", required = true, notes = "Identifier of this VnfVirtualLinkResourceInfo instance.")
    private String id;
    @ApiModelProperty(name = "Virtual Link Descriptor Id", required = true, notes = "Identifier of the VNF Virtual Link Descriptor (VLD) in the VNFD.")
    private String vnfVirtualLinkDescId;
    @ApiModelProperty(name = "VnfdId", notes = "Shall be present in case the value differs from the vnfdId attribute of the VnfInstance (e.g. during a \"Change current VNF package\" operation or due to its final failure).")
    private String vnfdId;
    @ApiModelProperty(name = "Network Resource", required = true, notes = "Reference to the VirtualNetwork resource.")
    private ResourceHandle networkResource;
    @ApiModelProperty(name = "ZoneId", notes = "The identifier of the resource zone, as managed by the resource management layer (typically, the VIM), where the referenced VirtualStorage resource is placed. Shall be provided if this information is available from the VIM.")
    private String zoneId;
    @ApiModelProperty(name = "Reservation Id", notes = "The reservation identifier applicable to the resource. It shall be present when an applicable reservation exists.")
    private String reservationId;
    @ApiModelProperty(name = "VNFC Link Ports", notes = "Links ports of this VL. Shall be present when the linkPort is used for external connectivity by the VNF (refer to VnfLinkPort). May be present otherwise.")
    private List<VnfLinkPortInfo> vnfLinkPorts;
    @ApiModelProperty(name = "Metadata", notes = "Metadata about this resource.")
    private Map<String, String> metadata;

}
