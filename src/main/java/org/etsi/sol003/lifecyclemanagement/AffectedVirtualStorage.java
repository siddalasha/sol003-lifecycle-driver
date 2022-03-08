package org.etsi.sol003.lifecyclemanagement;

import java.util.Map;

import org.etsi.sol003.common.ResourceHandle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Represents information about added, deleted, modified and temporary virtual storage resources.
 */
@Data
@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "Represents information about added, deleted, modified and temporary virtual storage resources.")
public class AffectedVirtualStorage {

    @ApiModelProperty(name = "Id", required = true, notes = "Identifier of the storage instance, identifying the applicable \"virtualStorageResourceInfo\" entry in the \"VnfInstance\" data type.")
    private String id;
    @ApiModelProperty(name = "Virtual Link Descriptor Id", required = true, notes = "Identifier of the related VirtualStorage descriptor in the VNFD.")
    private String virtualStorageDescId;
    @ApiModelProperty(name = "VnfdId", notes = "Identifier of the VNFD. Shall be present in case of a \"change current VNF Package\" to identify whether the affected virtual storage is associated to a VirtualStorage descriptor which is referred from the source or destination VNFD.")
    private String vnfdId;
    @ApiModelProperty(name = "Change Type", required = true, notes = "Signals the type of change.")
    private ChangeType changeType;
    @ApiModelProperty(name = "Storage Resource", required = true, notes = "Reference to the VirtualStorage resource. Detailed information is (for new and modified resources) or has been (for removed resources) available from the VIM.")
    private ResourceHandle storageResource;
    @ApiModelProperty(name = "ResourceDefinitionId", notes = "The identifier of the \"ResourceDefinition\" in the granting exchange related to the LCM operation occurrence. It shall be present when an applicable GrantInfo for the granted resource exists.")
    private String resourceDefinitionId;
    @ApiModelProperty(name = "ZoneId", notes = "The identifier of the resource zone, as managed by the resource management layer (typically, the VIM), where the referenced VirtualStorage resource is placed. Shall be provided if this information is available from the VIM.")
    private String zoneId;
    @ApiModelProperty(name = "Metadata", notes = "Metadata about this resource. The content of this attribute shall be a copy of the content of the \"metadata\" attribute of the VnfcResourceInfo structure.")
    private Map<String, String> metadata;

    public enum ChangeType {
        ADDED, REMOVED, MODIFIED, TEMPORARY
    }

}
