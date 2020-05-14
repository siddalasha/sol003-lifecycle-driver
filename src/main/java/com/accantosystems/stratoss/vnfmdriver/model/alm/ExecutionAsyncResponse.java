package com.accantosystems.stratoss.vnfmdriver.model.alm;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "Details returned when an async lifecycle execution request is accepted")
public class ExecutionAsyncResponse {

    @ApiModelProperty(value = "Request ID")
    private String requestId;
    @ApiModelProperty(value = "Status")
    private ExecutionStatus status;
    @ApiModelProperty(value = "Failure Details")
    private FailureDetails failureDetails;
    @ApiModelProperty(value = "Outputs")
    private Map<String, String> outputs;
    @ApiModelProperty(value = "Timestamp")
    private Long timestamp;

    public ExecutionAsyncResponse() {}

    public ExecutionAsyncResponse(String requestId, ExecutionStatus status, FailureDetails failureDetails, Map<String, String> outputs) {
        this.requestId = requestId;
        this.status = status;
        this.failureDetails = failureDetails;
        this.outputs = outputs;
    }

    public String getRequestId() {
        return requestId;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public FailureDetails getFailureDetails() {
        return failureDetails;
    }

    public void setFailureDetails(FailureDetails failureDetails) {
        this.failureDetails = failureDetails;
    }

    public Map<String, String> getOutputs() {
        return outputs;
    }

    public void setOutputs(Map<String, String> outputs) {
        this.outputs = outputs;
    }

    public Long getTimestamp() { return timestamp; }

    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "ExecutionAsyncResponse{" +
                "requestId='" + requestId + '\'' +
                ", status=" + status +
                ", failureDetails=" + failureDetails +
                ", outputs=" + outputs +
                '}';
    }
}