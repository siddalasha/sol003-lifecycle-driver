package org.etsi.sol003.lifecyclemanagement;

import java.util.List;
import java.util.Map;

import org.etsi.sol003.common.ExtManagedVirtualLinkData;
import org.etsi.sol003.common.ExtVirtualLinkData;
import org.etsi.sol003.common.VimConnectionInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Represents request parameters for the "Change current VNF package" operation to replace the VNF package on which a VNF instance is based.
 */
@Data
@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "Represents request parameters for the \"Change current VNF package\" operation to replace the VNF package on which a VNF instance is based.")
public class ChangeCurrentVnfPkgRequest {

    @ApiModelProperty(name = "VnfdId",required = true, notes = "Identifier of the VNFD which defines the destination VNF Package for the change.")
    private String vnfdId;
    @ApiModelProperty(name = "External Virtual Link Information",  notes = "Information about external VLs to connect the VNF to. Entries in the list that are unchanged need not be supplied as part of this request.")
    private List<ExtVirtualLinkData> extVirtualLinks;
    @ApiModelProperty(name = "External Managed Virtual Link Information",  notes = "Information about internal VLs that are managed by the NFVO.")
    private List<ExtManagedVirtualLinkData> extManagedVirtualLinks;
    @ApiModelProperty(name = "VIM Connection Information", notes = "Information about VIM connections to be used for managing the resources for the VNF instance, or refer to external virtual links. This attribute shall only be supported and may be present if VNF-related resource management in direct mode is applicable.")
    private Map<String,VimConnectionInfo> vimConnectionInfo;
    @ApiModelProperty(name = "Additional Parameters", notes = "Additional parameters passed by the NFVO as input to the process, specific to the VNF of which the underlying VNF package is changed,, as declared in the VNFD as part of \" ChangeCurrentVnfPkgOpConfig\".")
    private Map<String, String> additionalParams;
    @ApiModelProperty(name = "Extensions", notes = "If present, this attribute provides modifications to the values of the \"extensions\" attribute in \"VnfInstance\".")
    private Map<String, String> extensions;
    @ApiModelProperty(name = "VNF Configurable Properties", notes = "If present, this attribute provides modifications to the values of the \"vnfConfigurableProperties\" attribute in \"VnfInstance\".")
    private Map<String, String> vnfConfigurableProperties;

}
