package com.sove.command.engine.exector.jsch;

import cn.hutool.core.io.IoUtil;
import cn.hutool.extra.ssh.JschUtil;
import com.jcraft.jsch.Session;
import com.sove.command.engine.Executor;
import com.sove.command.engine.Command;
import com.sove.command.engine.CommandExecuteException;
import com.sove.command.engine.ResultParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

public class JschExecutor implements Executor {

    private final Logger log = LoggerFactory.getLogger(JschExecutor.class);

    private final JschConnectPool jschConnectPool;
    private final JschProperties jschProperties;

    public JschExecutor(JschProperties jschProperties) {
        this.jschProperties = jschProperties;
        this.jschConnectPool = new JschConnectPool(jschProperties.getMaxConnNum(), jschProperties.getTimeout());
    }

    @Override
    public <T> T exec(Command command, ResultParser<T> parser) throws CommandExecuteException {
        return this.exec(jschProperties.getHost(), jschProperties.getPort(), jschProperties.getUsername(), jschProperties.getPassword(),
                command, parser);
    }

    @Override
    public <T> T exec(String host, Integer port, String user, String password, Command command, ResultParser<T> parser) throws CommandExecuteException {
        Session session = jschConnectPool.getSession(host, port, user, password);
        try {
            return this.exec(session, command, parser);
        } finally {
            jschConnectPool.releaseSession(user, host, port, session);
        }
    }

    private <T> T exec(Session session, Command command, ResultParser<T> parser) throws CommandExecuteException {

        String cmd = command.build();
        log.debug("execute command: {}", cmd);
        ByteArrayOutputStream errStream = new ByteArrayOutputStream();
        String result;
        result = JschUtil.exec(session, cmd, Charset.defaultCharset(), errStream);
        log.debug("execute result: {}", result);

        String errMessage = errStream.toString(Charset.defaultCharset());
        IoUtil.close(errStream);

        if (!errMessage.isEmpty()) {
            log.debug("execute error message: {}", errMessage);
            throw new CommandExecuteException(errMessage);
        }
        return parser.parse(result);
    }
}
