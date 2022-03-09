package org.etsi.sol003.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Represents a link to a resource
 */
@Data
@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "Represents a link to a resource in a notification")
public class NotificationLink {

    @ApiModelProperty(name = "HREF", required = true, dataType = "URI", notes = "URI of a resource referenced from a notification. Should be an absolute URI")
    private String href;

}
