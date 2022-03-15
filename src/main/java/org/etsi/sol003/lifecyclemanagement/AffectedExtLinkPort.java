package org.etsi.sol003.lifecyclemanagement;

import org.etsi.sol003.common.ResourceHandle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Represents information about added and deleted external link ports.
 */
@Data
@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "Represents information about added and deleted external link ports.")
public class AffectedExtLinkPort {

    @ApiModelProperty(name = "Id", required = true, notes = "Identifier of the storage instance, identifying the applicable \"extLinkPorts\" entry in the \"ExtVirtualLinkInfo\" data type.")
    private String id;
    @ApiModelProperty(name = "Change Type", required = true, notes = "Signals the type of change.")
    private ChangeType changeType;
    @ApiModelProperty(name = "Ext CP Instance Id", required = true, notes = "Identifier of the related external CP instance.")
    private String extCpInstanceId;
    @ApiModelProperty(name = "Resource Handle", required = true, notes = "Reference to the link port resource. Detailed information is (for added resources) or has been (for removed resources) available from the VIM.")
    private ResourceHandle resourceHandle;
    @ApiModelProperty(name = "ResourceDefinitionId", notes = "The identifier of the \"ResourceDefinition\" in the granting exchange related to the LCM operation occurrence. It shall be present when an applicable GrantInfo for the granted resource exists.")
    private String resourceDefinitionId;
   
    public enum ChangeType {
        ADDED, REMOVED
    }

}
