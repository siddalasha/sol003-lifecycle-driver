package org.etsi.sol003.common;

import java.util.List;

import org.etsi.sol003.lifecyclemanagement.SubnetIpRange;

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
public class DynamicNetworkAddressData {

    @ApiModelProperty(name = "MAC Address", notes = "MAC address. Shall not be present if numIPAddresses > 1. If it is not present, it will be chosen by the VIM.")
    private String macAddress;
    @ApiModelProperty(name = "Number of IP Addresses", required = true, notes = "Number of IP addresses to assign dynamically. Shall be greater than zero.")
    private Integer numIpAddresses;
    @ApiModelProperty(name = "Subnet Id", notes = "Subnet defined by the identifier of the subnet resource in the VIM. In case this attribute is present, an IP addresses from that subnet will be assigned; otherwise, IP addresses not bound to a subnet will be assigned.")
    private String subnetId;
    @ApiModelProperty(name = "Subnet IP Ranges", notes = "Subnet defined as one or more IP address ranges. In case this attribute is present, IP addresses from one of the ranges will be assigned; otherwise, IP addresses not bound to a subnet will be assigned.")
    private List<SubnetIpRange> subnetIpRanges;

}