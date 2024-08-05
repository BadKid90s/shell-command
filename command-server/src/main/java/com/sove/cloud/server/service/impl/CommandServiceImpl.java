package com.sove.cloud.server.service.impl;

import com.sove.cloud.server.domain.RemoteCommand;
import com.sove.cloud.server.service.CommandService;
import com.sove.engine.Command;
import com.sove.engine.Executor;
import com.sove.engine.ResultParser;
import com.sove.engine.builder.CommandBuilder;
import com.sove.engine.builder.ResultParserBuilder;
import org.springframework.stereotype.Service;

@Service
public class CommandServiceImpl implements CommandService {

    private final Executor executor;

    public CommandServiceImpl(Executor executor) {
        this.executor = executor;
    }

    @Override
    public String execute(String cmd) {
        Command command = CommandBuilder.build(cmd);
        ResultParser<String> parser = ResultParserBuilder.build();
        return executor.exec(command, parser);
    }

    @Override
    public String execute(RemoteCommand cmd) {
        Command command = CommandBuilder.build(cmd.getCmd());
        ResultParser<String> parser = ResultParserBuilder.build();
        return executor.exec(cmd.getHost(), cmd.getPort(), cmd.getUsername(), cmd.getPassword(), command, parser);
    }

}

