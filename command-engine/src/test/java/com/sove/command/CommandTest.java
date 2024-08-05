package com.sove.command;


import com.sove.command.engine.Command;
import com.sove.command.engine.ResultParser;
import com.sove.command.engine.builder.CommandBuilder;
import com.sove.command.engine.builder.ResultParserBuilder;
import com.sove.command.engine.exector.jsch.JschExecutor;
import com.sove.command.engine.exector.jsch.JschProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CommandTest {

    @Test
    public void commandTest() {
        JschProperties properties = new JschProperties("127.0.0.1", 22, "root", "123456");
        properties.setTimeout(10);
        properties.setMaxConnNum(32);
        JschExecutor executor = new JschExecutor(properties);

        Command ipAddrCmd = CommandBuilder.build("ip addr");
        ResultParser<String> parser = ResultParserBuilder.build();
        String result = executor.exec(ipAddrCmd, parser);

        Assertions.assertTrue(result.contains("127.0.0.1"));

    }
}
