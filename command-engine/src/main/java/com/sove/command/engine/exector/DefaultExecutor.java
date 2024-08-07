package com.sove.command.engine.exector;

import com.sove.command.engine.Command;
import com.sove.command.engine.CommandExecuteException;
import com.sove.command.engine.Executor;
import com.sove.command.engine.ResultParser;
import com.sove.command.engine.exector.sshj.SshjExecutor;

public class DefaultExecutor implements Executor {
    private final Executor delegate;

    public DefaultExecutor(SshProperties properties) {
        this.delegate = new SshjExecutor(properties);
    }

    @Override
    public <T> T exec(Command command, ResultParser<T> parser) throws CommandExecuteException {
        return delegate.exec(command, parser);
    }

    @Override
    public <T> T exec(String host, Integer port, String user, String password, Command command, ResultParser<T> parser) throws CommandExecuteException {
        return delegate.exec(host, port, user, password, command, parser);
    }
}
