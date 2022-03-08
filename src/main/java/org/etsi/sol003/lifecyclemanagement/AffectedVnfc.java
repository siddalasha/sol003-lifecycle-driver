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
 * Represents information about added, deleted, modified and temporary VNFCs.
 */
@Data
@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "Represents information about added, deleted, modified and temporary VNFCs.")
public class AffectedVnfc {

    @ApiModelProperty(name = "Id", required = true, notes = "Identifier of the Vnfc instance, identifying the applicable \"vnfcResourceInfo\" entry in the \"VnfInstance\" data type.")
    private String id;
    @ApiModelProperty(name = "VDU Id", required = true, notes = "Identifier of the related VDU in the VNFD.")
    private String vduId;
    @ApiModelProperty(name = "VnfdId", notes = "Identifier of the VNFD. Shall be present in case of a \"change current VNF Package\" to identify whether the affected VNFC instance is associated to a VDU which is referred from the source or destination VNFD.")
    private String vnfdId;
    @ApiModelProperty(name = "Change Type", required = true, notes = "Signals the type of change.")
    private ChangeType changeType;
    @ApiModelProperty(name = "Compute Resource", required = true, notes = "Reference to the VirtualCompute resource. Detailed information is (for new and modified resources) or has been (for removed resources) available from the VIM.")
    private ResourceHandle computeResource;
    @ApiModelProperty(name = "ResourceDefinitionId", notes = "The identifier of the \"ResourceDefinition\" in the granting exchange related to the LCM operation occurrence. It shall be present when an applicable GrantInfo for the granted resource exists.")
    private String resourceDefinitionId;
    @ApiModelProperty(name = "ZoneId", notes = "The identifier of the resource zone, as managed by the resource management layer (typically, the VIM), where the referenced VirtualCompute resource is placed. Shall be provided if this information is available from the VIM.")
    private String zoneId;
    @ApiModelProperty(name = "Metadata", notes = "Metadata about this resource. The content of this attribute shall be a copy of the content of the \"metadata\" attribute of the VnfcResourceInfo structure.")
    private Map<String, String> metadata;
    @ApiModelProperty(name = "Affected VNFC CP Ids", notes = "Identifiers of CP(s) of the VNFC instance that were affected by the change. Shall be present for those affected CPs of the VNFC instance that are associated to an external CP of the VNF instance. May be present for further affected CPs of the VNFC instance.")
    private List<String> affectedVnfcCpIds;
    @ApiModelProperty(name = "Added Storage Resource Ids", notes = "References to VirtualStorage resources that have been added. Each value refers to a VirtualStorageResourceInfo item in the VnfInstance that was added to the VNFC. It shall be provided if at least one storage resource was added to the VNFC.")
    private List<String> addedStorageResourceIds;
    @ApiModelProperty(name = "Removed Storage Resource Ids", notes = "References to VirtualStorage resources that have been removed. The value contains the identifier of a VirtualStorageResourceInfo item that has been removed from the VNFC, and might no longer exist in the VnfInstance. It shall be provided if at least one storage resource was removed from the VNFC.")
    private List<String> removedStorageResourceIds;

    public enum ChangeType {
        ADDED, REMOVED, MODIFIED, TEMPORARY
    }

}
