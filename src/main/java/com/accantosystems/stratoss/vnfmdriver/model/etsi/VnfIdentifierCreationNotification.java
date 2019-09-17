package com.accantosystems.stratoss.vnfmdriver.model.etsi;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Represents a VNF identifier creation notification, which informs the receiver of the creation of a new VNF
 * instance resource and the associated VNF instance identifier.
 */
@Data
@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "Represents a VNF identifier creation notification, which informs the receiver of the creation of a new VNF instance resource and the associated VNF instance identifier.")
public class VnfIdentifierCreationNotification implements SOL003Notification {

    public static final String TYPE = "VnfIdentifierCreationNotification";

    @ApiModelProperty(name = "Id", required = true, notes = "Identifier of this notification. If a notification is sent multiple times due to multiple subscriptions, the \"id\" attribute of all these notifications shall have the same value.")
    private String id;
    @ApiModelProperty(name = "Notification Type", required = true, notes = "Discriminator for the different notification types. Shall be set to \"VnfIdentifierCreationNotification\" for this notification type.")
    private final String notificationType = TYPE;
    @ApiModelProperty(name = "Subscription Id", notes = "Identifier of the subscription that this notification relates to.")
    private String subscriptionId;
    @ApiModelProperty(name = "Notification Time", required = true, notes = "Date-time of the generation of the notification.")
    private OffsetDateTime timeStamp;
    @ApiModelProperty(name = "VNF Instance Id", required = true, notes = "The created VNF instance identifier.")
    private String vnfInstanceId;
    @ApiModelProperty(name = "Links", required = true, notes = "Links to resources related to this notification.")
    @JsonProperty("_links")
    private LccnLinks links;

}
