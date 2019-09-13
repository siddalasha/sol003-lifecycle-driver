package com.accantosystems.stratoss.vnfmdriver.model.etsi;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Represents an external CP.
 */
@Data
@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "Represents an external CP.")
public class VnfExtCpData {

    @ApiModelProperty(name = "CPD Id", required = true, notes = "The identifier of the CPD in the VNFD.")
    private String cpdId;
    @ApiModelProperty(name = "Fixed IP Addresses", notes = "List of (fixed) network addresses that need to be configured on the CP. This attribute shall be present if fixed addresses need to be configured.")
    private List<FixedNetworkAddressData> fixedAddresses;
    @ApiModelProperty(name = "Dynamic IP Addresses", notes = "List of network addresses to be assigned dynamically. This attribute shall be present if dynamic addresses need to be configured.")
    private List<DynamicNetworkAddressData> dynamicAddresses;

}
