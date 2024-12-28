package com.sove.command.engine.exector.sshj;

import com.sove.command.engine.Command;
import com.sove.command.engine.CommandExecuteException;
import com.sove.command.engine.Executor;
import com.sove.command.engine.ResultParser;
import com.sove.command.engine.exector.SshProperties;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SshjExecutor implements Executor {

    private final Logger log = LoggerFactory.getLogger(SshjExecutor.class);

    private final SshjConnectPool connectPool;
    private final SshProperties properties;

    public SshjExecutor(SshProperties properties) {
        this.properties = properties;
        this.connectPool = new SshjConnectPool(properties.getTimeout());
    }

    @Override
    public <T> T exec(Command command, ResultParser<T> parser) throws CommandExecuteException {
        return this.exec(properties.getHost(), properties.getPort(), properties.getUsername(), properties.getPassword(),
                command, parser);
    }

    @Override
    public <T> T exec(String host, Integer port, String user, String password, Command command, ResultParser<T> parser) throws CommandExecuteException {
        String cmdStr = command.build();
        try {
            Session session = connectPool.getSession(host, port, user, password);
            log.debug("execute command: {}", cmdStr);
            String resultMsg, errorMsg;
            try (Session.Command cmd = session.exec(cmdStr)) {
                cmd.join(properties.getTimeout(), TimeUnit.SECONDS);
                resultMsg = IOUtils.readFully(cmd.getInputStream()).toString("UTF-8");
                errorMsg = IOUtils.readFully(cmd.getErrorStream()).toString("UTF-8");

                if (!errorMsg.isEmpty()) {
                    throw new CommandExecuteException(String.format("execute command error, command: %s, message: %s", cmdStr, errorMsg));
                }
                return parser.parse(resultMsg);
            }
        } catch (ConnectionException e) {
            throw new CommandExecuteException(String.format("connection ssh error, host: %s,username: %s, password: %s", host, user, password));
        } catch (IOException e) {
            throw new CommandExecuteException(String.format("read exec result error, command: %s, message: %s", cmdStr, e.getMessage()));
        }
    }
}
