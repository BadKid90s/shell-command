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
    public <T> T execute(Command command, ResultParser<T> parser) throws CommandExecuteException {
        return delegate.execute(command, parser);
    }

    @Override
    public <T> T execute(String sshHost, int sshPort, String sshUser, String sshPass, Command command, ResultParser<T> parser) {
        return delegate.execute(sshHost, sshPort, sshUser, sshPass, command, parser);
    }

}
