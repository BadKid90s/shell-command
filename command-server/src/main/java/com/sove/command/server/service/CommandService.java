package com.sove.command.server.service;

import com.sove.command.server.domain.RemoteCommand;
import com.sove.command.engine.CommandExecuteException;

public interface CommandService {

    String execute(String command) throws CommandExecuteException;

    String execute(RemoteCommand command) throws CommandExecuteException;
}