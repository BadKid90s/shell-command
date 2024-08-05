package com.sove.engine.exector.jsch;

import cn.hutool.core.io.IoUtil;
import cn.hutool.extra.ssh.JschUtil;
import com.jcraft.jsch.Session;
import com.sove.engine.Command;
import com.sove.engine.CommandExecuteException;
import com.sove.engine.Executor;
import com.sove.engine.ResultParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.concurrent.*;

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
        T exec = this.exec(session, command, parser);
        jschConnectPool.releaseSession(user, host, port, session);
        return exec;
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
