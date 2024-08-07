package com.sove.command;


import com.sove.command.engine.Command;
import com.sove.command.engine.ResultParser;
import com.sove.command.engine.builder.CommandBuilder;
import com.sove.command.engine.builder.ResultParserBuilder;
import com.sove.command.engine.exector.sshj.SshjExecutor;
import com.sove.command.engine.exector.SshProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CommandTest {

    @Test
    public void commandTest() {
        SshProperties properties = new SshProperties("127.0.0.1", 22, "root", "123456");
        properties.setTimeout(10);
        properties.setMaxConnNum(32);
        SshjExecutor executor = new SshjExecutor(properties);

        Command ipAddrCmd = CommandBuilder.build("ip addr");
        ResultParser<String> parser = ResultParserBuilder.build();
        String result = executor.exec(ipAddrCmd, parser);

        Assertions.assertTrue(result.contains("127.0.0.1"));

    }
}
