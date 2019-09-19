package org.etsi.sol003.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Represents a network address that is requested to be assigned.
 */
@Data
@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "Represents a network address that is requested to be assigned.")
public class FixedNetworkAddressData {

    @ApiModelProperty(name = "MAC Address", notes = "MAC address. If it is not present, it will be chosen by the VIM. NOTE: At least one of \"macAddress\" and \"ipAddress\" shall be present.")
    private String macAddress;
    @ApiModelProperty(name = "IP Address", notes = "IP address. If it is not present, no IP address will be assigned. NOTE: At least one of \"macAddress\" and \"ipAddress\" shall be present.")
    private String ipAddress;
    @ApiModelProperty(name = "Subnet Id", notes = "Identifier of the subnet in the VIM.")
    private String subnetId;

}