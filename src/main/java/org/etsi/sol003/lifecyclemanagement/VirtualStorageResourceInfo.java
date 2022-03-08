package org.etsi.sol003.lifecyclemanagement;

import java.util.Map;

import org.etsi.sol003.common.ResourceHandle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Represents the information that allows addressing a virtualised resource that is used by a VNF instance.
 */
@Data
@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "Represents the information that allows addressing a virtualised resource that is used by a VNF instance.")
public class VirtualStorageResourceInfo {

    @ApiModelProperty(name = "Id", required = true, notes = "Identifier of this VirtualStorageResourceInfo instance.")
    private String id;
    @ApiModelProperty(name = "Virtual Storage Descriptor Id", required = true, notes = "Identifier of the VirtualStorageDesc in the VNFD.")
    private String virtualStorageDescId;
    @ApiModelProperty(name = "VnfdId", notes = "Shall be present in case the value differs from the vnfdId attribute of the VnfInstance (e.g. during a \"Change current VNF package\" operation or due to its final failure).")
    private String vnfdId;
    @ApiModelProperty(name = "Storage Resource", required = true, notes = "Reference to the VirtualStorage resource.")
    private ResourceHandle storageResource;
    @ApiModelProperty(name = "ZoneId", notes = "The identifier of the resource zone, as managed by the resource management layer (typically, the VIM), where the referenced VirtualStorage resource is placed. Shall be provided if this information is available from the VIM.")
    private String zoneId;
    @ApiModelProperty(name = "Reservation Id", notes = "The reservation identifier applicable to the resource. It shall be present when an applicable reservation exists.")
    private String reservationId;
    @ApiModelProperty(name = "Metadata", notes = "Metadata about this resource.")
    private Map<String, String> metadata;

}
