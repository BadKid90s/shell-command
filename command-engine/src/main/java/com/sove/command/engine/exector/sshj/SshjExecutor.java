package com.sove.command.engine.exector.sshj;

import cn.hutool.core.io.IoUtil;
import cn.hutool.extra.ssh.JschUtil;
import com.sove.command.engine.Executor;
import com.sove.command.engine.Command;
import com.sove.command.engine.CommandExecuteException;
import com.sove.command.engine.ResultParser;
import com.sove.command.engine.exector.SshProperties;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class SshjExecutor implements Executor {

    private final Logger log = LoggerFactory.getLogger(SshjExecutor.class);

    private final SshjConnectPool connectPool;
    private final SshProperties properties;

    public SshjExecutor(SshProperties properties) {
        this.properties = properties;
        this.connectPool = new SshjConnectPool(properties.getMaxConnNum(), properties.getTimeout());
    }

    @Override
    public <T> T exec(Command command, ResultParser<T> parser) throws CommandExecuteException {
        return this.exec(properties.getHost(), properties.getPort(), properties.getUsername(), properties.getPassword(),
                command, parser);
    }

    @Override
    public <T> T exec(String host, Integer port, String user, String password, Command command, ResultParser<T> parser) throws CommandExecuteException {
        SSHClient client = connectPool.getClient(host, port, user, password);
        try {
            T exec = this.exec(client, command.build(), parser);
            connectPool.release(user, host, port, client);
            return exec;
        } catch (Exception e) {
            connectPool.close(user, host, port, client);
            throw new CommandExecuteException(e.getMessage());
        }
    }

    private <T> T exec(SSHClient client, String cmdStr, ResultParser<T> parser) throws CommandExecuteException, IOException {
        log.debug("execute command: {}", cmdStr);
        String resultMsg, errorMsg;

        Session session = client.startSession();
        Session.Command cmd = session.exec(cmdStr);

        resultMsg = IOUtils.readFully(cmd.getInputStream()).toString(Charset.defaultCharset());
        errorMsg = IOUtils.readFully(cmd.getErrorStream()).toString(Charset.defaultCharset());

        if (!errorMsg.isEmpty()) {
            log.debug("execute error message: {}", errorMsg);
            throw new CommandExecuteException(errorMsg);
        }
        return parser.parse(resultMsg);
    }
}
