package org.etsi.sol003.lifecyclemanagement;

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
    @ApiModelProperty(name = "Virtual Link Descriptor Id", required = true, notes = "Identifier of the related VLD in the VNFD.")
    private String virtualLinkDescId;
    @ApiModelProperty(name = "Change Type", required = true, notes = "Signals the type of change.")
    private ChangeType changeType;
    @ApiModelProperty(name = "Network Resource", required = true, notes = "Reference to the VirtualNetwork resource. Detailed information is (for new and modified resources) or has been (for removed resources) available from the VIM.")
    private ResourceHandle networkResource;

    public enum ChangeType {
        ADDED, REMOVED, MODIFIED, TEMPORARY, LINK_PORT_ADDED, LINK_PORT_REMOVED
    }

}
