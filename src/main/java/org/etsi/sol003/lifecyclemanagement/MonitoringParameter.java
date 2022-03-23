package org.etsi.sol003.lifecyclemanagement;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Represents a monitoring parameter that is tracked by the VNFM, e.g. for auto-scaling purposes.
 */
@Data
@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "Represents a monitoring parameter that is tracked by the VNFM, e.g. for auto-scaling purposes.")
public class MonitoringParameter {

    @ApiModelProperty(name = "Id", required = true, notes = "Identifier of the monitoring parameter defined in the VNFD.")
    private String id;
    @ApiModelProperty(name = "VnfdId", notes = "Identifier of the VNFD. Shall be present in case the value differs from the vnfdId attribute of the VnfInstance (e.g. during a \"Change current VNF package\" operation or due to its final failure).")
    private String vnfdId;
    @ApiModelProperty(name = "Name", notes = "Human readable name of the monitoring parameter, as defined in the VNFD.")
    private String name;
    @ApiModelProperty(name = "Performance Metric", required = true, notes = "Performance metric that is monitored. This attribute shall contain the related \"Measurement Name\" value")
     private String performanceMetric;


}
