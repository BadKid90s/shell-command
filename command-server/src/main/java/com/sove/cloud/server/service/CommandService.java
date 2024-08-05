package com.sove.cloud.server.service;

import com.sove.cloud.server.domain.RemoteCommand;
import com.sove.engine.CommandExecuteException;

public interface CommandService {

    String execute(String command) throws CommandExecuteException;

    String execute(RemoteCommand command) throws CommandExecuteException;
}