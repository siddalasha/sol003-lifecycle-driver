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
 * Represents information about added, deleted, modified and temporary VLs.
 */
@Data
@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "Represents information about added, deleted, modified and temporary VLs.")
public class AffectedVirtualLink {

    @ApiModelProperty(name = "Id", required = true, notes = "Identifier of the virtual link instance, identifying the applicable \"vnfVirtualLinkResourceInfo\" entry in the \"VnfInstance\" data type.")
    private String id;
    @ApiModelProperty(name = "VNF Virtual Link Descriptor Id", required = true, notes = "Identifier of the related VLD in the VNFD.")
    private String vnfVirtualLinkDescId;
    @ApiModelProperty(name = "VnfdId", notes = "Identifier of the VNFD. Shall be present in case of a \"change current VNF Package\" to identify whether the affected VL instance is associated to a VLD which is referred from the source or destination VNFD.")
    private String vnfdId;
    @ApiModelProperty(name = "Change Type", required = true, notes = "Signals the type of change.")
    private ChangeType changeType;
    @ApiModelProperty(name = "Network Resource", required = true, notes = "Reference to the VirtualNetwork resource. Detailed information is (for new and modified resources) or has been (for removed resources) available from the VIM.")
    private ResourceHandle networkResource;
    @ApiModelProperty(name = "VnfLinkPortIds", notes = "Identifiers of the link ports of the affected VL related to the change. Each identifier references a \"VnfLinkPortInfo\" structure. Shall be set when changeType is equal to \"LINK_PORT_ADDED\" or \"LINK_PORT_REMOVED\", and the related \"VnfLinkPortInfo\" structures are present (case \"added\") or have been present (case \"removed\") in the \"VnfVirtualLinkResourceInfo\" or \"ExtManagedVirtualLinkInfo\" structures that are represented by the \"vnfVirtualLinkResourceInfo\" or \"extManagedVirtualLinkInfo\" attribute in the \"VnfInstance\" structure.")
    private List<String> vnfLinkPortIds;
    @ApiModelProperty(name = "ResourceDefinitionId", notes = "The identifier of the \"ResourceDefinition\" in the granting exchange related to the LCM operation occurrence. It shall be present when an applicable GrantInfo for the granted resource exists.")
    private String resourceDefinitionId;
    @ApiModelProperty(name = "ZoneId", notes = "The identifier of the resource zone, as managed by the resource management layer (typically, the VIM), where the referenced VirtualNetwork resource is placed. Shall be provided if this information is available from the VIM.")
    private String zoneId;
    @ApiModelProperty(name = "Metadata", notes = "Metadata about this resource. The content of this attribute shall be a copy of the content of the \"metadata\" attribute of the VnfcResourceInfo structure.")
    private Map<String, String> metadata;

    public enum ChangeType {
        ADDED, REMOVED, MODIFIED, TEMPORARY, LINK_PORT_ADDED, LINK_PORT_REMOVED
    }

}
