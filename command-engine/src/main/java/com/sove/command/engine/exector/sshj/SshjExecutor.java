package com.sove.command.engine.exector.sshj;

import com.sove.command.engine.Command;
import com.sove.command.engine.CommandExecuteException;
import com.sove.command.engine.Executor;
import com.sove.command.engine.ResultParser;
import com.sove.command.engine.exector.SshProperties;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
    public <T> T execute(Command command, ResultParser<T> parser) {
        return this.execute(properties.getHost(), properties.getPort(), properties.getUsername(), properties.getPassword(),
                command, parser);
    }


    @Override
    public <T> T execute(String host, int port, String user, String password, Command command, ResultParser<T> parser) {
        String cmdStr = command.build();
        try {
            Session session = connectPool.getSession(host, port, user, password);
            log.debug("execute command: {}", cmdStr);
            String resultMsg, errorMsg;
            try (Session.Command cmd = session.exec(cmdStr)) {
                cmd.join(properties.getTimeout(), TimeUnit.MILLISECONDS);
                StringBuilder resultBuilder = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(cmd.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        resultBuilder.append(line).append("\n");
                    }
                }
                resultMsg = resultBuilder.toString();

                StringBuilder errorBuilder = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(cmd.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorBuilder.append(line).append("\n");
                    }
                }
                errorMsg = errorBuilder.toString();

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
