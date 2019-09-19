package org.etsi.sol003.lifecyclemanagement;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Represents information about a network address that has been assigned.
 */
@Data
@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "Represents information about a network address that has been assigned.")
public class NetworkAddressInfo {

    @ApiModelProperty(name = "MAC Address", required = true, notes = "Assigned MAC address.")
    private String macAddress;
    @ApiModelProperty(name = "IP Address", notes = "IP address. Present if an IP address was assigned.")
    private String ipAddresses;
    @ApiModelProperty(name = "Subnet IP Ranges", notes = "IP address ranges defining the subnet in which the IP address was assigned. May be present if the \"ipAddress\" attribute is present, and shall be absent if the \"ipAddress\" attribute is not present.")
    private List<SubnetIpRange> subnetIpRanges;

}
