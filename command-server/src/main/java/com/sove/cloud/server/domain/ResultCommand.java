package com.sove.cloud.server.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class ResultCommand {

    @ApiModelProperty(value = "成功的消息")
    private String success;

    @ApiModelProperty(value = "失败的消息")
    private String error;

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
