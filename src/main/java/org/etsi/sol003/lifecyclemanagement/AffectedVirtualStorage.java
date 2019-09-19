package org.etsi.sol003.lifecyclemanagement;

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
    @ApiModelProperty(name = "Change Type", required = true, notes = "Signals the type of change.")
    private ChangeType changeType;
    @ApiModelProperty(name = "Storage Resource", required = true, notes = "Reference to the VirtualStorage resource. Detailed information is (for new and modified resources) or has been (for removed resources) available from the VIM.")
    private ResourceHandle storageResource;

    public enum ChangeType {
        ADDED, REMOVED, MODIFIED, TEMPORARY
    }

}
