package com.sove.command.engine;

public interface Executor {

    <T> T exec(Command command, ResultParser<T> parser) throws CommandExecuteException;

    <T> T exec(String host, Integer port, String user, String password,
               Command command, ResultParser<T> parser) throws CommandExecuteException;
}
