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
 * Links to resources related to this notification.
 */
@Data
@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "Links to resources related to this notification.")
public class VnfcResourceInfo {

    @ApiModelProperty(name = "Id", required = true, notes = "Identifier of this VnfcResourceInfo instance.")
    private String id;
    @ApiModelProperty(name = "VDU Id", required = true, notes = "Reference to the applicable VDU in the VNFD.")
    private String vduId;
    @ApiModelProperty(name = "VnfdId", notes = "Shall be present in case the value differs from the vnfdId attribute of the VnfInstance (e.g. during a \"Change current VNF package\" operation or due to its final failure).")
    private String vnfdId;
    @ApiModelProperty(name = "Compute Resource", required = true, notes = "Reference to the VirtualCompute resource.")
    private ResourceHandle computeResource;
    @ApiModelProperty(name = "ZoneId", notes = "The identifier of the resource zone, as managed by the resource management layer (typically, the VIM), where the referenced VirtualStorage resource is placed. Shall be provided if this information is available from the VIM.")
    private String zoneId;
    @ApiModelProperty(name = "Storage Resource Ids", notes = "References to the VirtualStorage resources. The value refers to a VirtualStorageResourceInfo item in the VnfInstance.")
    private List<String> storageResourceIds;
    @ApiModelProperty(name = "Reservation Id", notes = "The reservation identifier applicable to the resource. It shall be present when an applicable reservation exists.")
    private String reservationId;
    @ApiModelProperty(name = "VNFC Connection Point Information", notes = "CPs of the VNFC instance. Shall be present when that particular CP of the VNFC instance is associated to an external CP of the VNF instance. May be present otherwise.")
    private List<VnfcCpInfo> vnfcCpInfo;
    @ApiModelProperty(name = "Metadata", notes = "Metadata about this resource.")
    private Map<String, String> metadata;

    /**
     * Represents VNFC Connection Point Information
     */
    @Data
    @JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @ApiModel(description = "Represents VNFC Connection Point Information.")
    public static class VnfcCpInfo {

        @ApiModelProperty(name = "Id", required = true, notes = "Identifier of this VNFC CP instance and the associated array entry.")
        private String id;
        @ApiModelProperty(name = "CPD Id", required = true, notes = "Identifier of the VDU CPD, cpdId, in the VNFD.")
        private String cpdId;
        @ApiModelProperty(name = "VNF External Connection Point Id", notes = "When the VNFC CP is exposed as external CP of the VNF, the identifier of this external VNF CP.")
        private String vnfExtCpId;
        @ApiModelProperty(name = "Connection Point Protocol Information", notes = "Network protocol information for this CP.")
        private List<CpProtocolInfo> cpProtocolInfo;
        @ApiModelProperty(name = "VNF Link Port Id", notes = "Identifier of the \"vnfLinkPorts\" structure in the \"vnfVirtualLinkResourceInfo\" structure. Shall be present if the CP is associated to a link port.")
        private String vnfLinkPortId;
        @ApiModelProperty(name = "Parent Cp Id", notes = "Identifier of another VNFC CP instance that corresponds to the parent port of a trunk that the present VNFC CP instance participates in.")
        private String parentCpId;
        @ApiModelProperty(name = "Metadata", notes = "Metadata about this CP.")
        private Map<String, String> metadata;

    }

}
