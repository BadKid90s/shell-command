package com.sove.cloud.server.controller;

import com.sove.cloud.server.domain.RemoteCommand;
import com.sove.cloud.server.domain.ResultCommand;
import com.sove.cloud.server.service.CommandService;
import com.sove.engine.CommandExecuteException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;

@Api(tags = "命令行")
@RestController
@RequestMapping("/cmd")
public class CommandController {

    private final CommandService commandService;

    public CommandController(CommandService commandService) {
        this.commandService = commandService;
    }

    @ApiOperation("执行本机命令")
    @PostMapping(value = "/local", consumes = MediaType.TEXT_PLAIN_VALUE)
    public String localCommand(
            @ApiParam(value = "命令", readOnly = true)
            @NotBlank(message = "命令不能为空")
            @RequestBody String cmd
    ) {
        try {
            return commandService.execute(cmd);
        } catch (CommandExecuteException exception) {
            return exception.getMessage();
        }
    }

    @ApiOperation("执行远程命令")
    @PostMapping(value = "/remote", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String remoteCommand(
            @ApiParam(value = "命令", readOnly = true)
            @NotBlank(message = "命令不能为空")
            @RequestBody RemoteCommand cmd
    ) {
        try {
            return commandService.execute(cmd);
        } catch (CommandExecuteException exception) {
            return exception.getMessage();
        }
    }

    @ApiOperation("执行本机命令JSON")
    @PostMapping(value = "/local/json", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResultCommand localCommandJson(
            @ApiParam(value = "命令", readOnly = true)
            @NotBlank(message = "命令不能为空")
            @RequestBody String cmd
    ) {
        ResultCommand result = new ResultCommand();
        try {
            String execute = commandService.execute(cmd);
            result.setSuccess(execute);
        } catch (CommandExecuteException exception) {
            result.setError(exception.getMessage());
        }
        return result;
    }

    @ApiOperation("执行远程命令JSON")
    @PostMapping(value = "/remote/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResultCommand remoteCommandJson(
            @ApiParam(value = "命令", readOnly = true)
            @NotBlank(message = "命令不能为空")
            @RequestBody RemoteCommand cmd
    ) {
        ResultCommand result = new ResultCommand();
        try {
            String execute = commandService.execute(cmd);
            result.setSuccess(execute);
        } catch (CommandExecuteException exception) {
            result.setError(exception.getMessage());
        }
        return result;
    }
}
