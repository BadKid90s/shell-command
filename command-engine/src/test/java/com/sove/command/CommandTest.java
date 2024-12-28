package com.sove.command;


import com.sove.command.engine.Command;
import com.sove.command.engine.ResultParser;
import com.sove.command.engine.builder.CommandBuilder;
import com.sove.command.engine.builder.ResultParserBuilder;
import com.sove.command.engine.exector.SshProperties;
import com.sove.command.engine.exector.sshj.SshjExecutor;
import net.schmizz.keepalive.KeepAliveProvider;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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


    @Test
    public void sshjTest() {
        DefaultConfig defaultConfig = new DefaultConfig();
        defaultConfig.setKeepAliveProvider(KeepAliveProvider.KEEP_ALIVE);
        try (SSHClient ssh = new SSHClient(defaultConfig)) {
            ssh.addHostKeyVerifier(new PromiscuousVerifier());
            ssh.getConnection().getKeepAlive().setKeepAliveInterval(5);
            ssh.connect("127.0.0.1", 22);
            ssh.authPassword("root", "123456");

            // 创建线程池
            ExecutorService executor = Executors.newFixedThreadPool(5);

            // 提交多个任务
            for (int i = 0; i < 5000; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    // 为每个线程创建一个新的 Session
                    try (Session session = ssh.startSession()) {
                        String command = "echo Hello from thread " + threadId;
                        Session.Command cmd = session.exec(command);
                        cmd.join(5, TimeUnit.SECONDS);
                        String resultMsg = IOUtils.readFully(cmd.getInputStream()).toString(Charset.defaultCharset());
                        String errorMsg = IOUtils.readFully(cmd.getErrorStream()).toString(Charset.defaultCharset());

                        System.out.println("resultMsg: " + resultMsg);
                        System.out.println("errorMsg: " + errorMsg);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            // 关闭线程池，停止接收新任务
            executor.shutdown();
            try {
                // 等待所有任务完成，最多等待 60 秒
                if (!executor.awaitTermination(160, TimeUnit.SECONDS)) {
                    executor.shutdownNow(); // 超时后强制关闭
                }
            } catch (InterruptedException e) {
                executor.shutdownNow(); // 当前线程被中断，强制关闭
                Thread.currentThread().interrupt(); // 恢复中断状态
            }
            System.out.println("success! ");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void sshjExecutorTest() {
        SshProperties properties = new SshProperties("127.0.0.1", 22, "root", "123456");
        SshjExecutor sshExecutor = new SshjExecutor(properties);
        ResultParser<String> parser = ResultParserBuilder.build();

        // 创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(100);

        // 提交多个任务
        for (int i = 0; i < 10000; i++) {
            final int threadId = i;
            executor.submit(() -> {
                String command = "echo Hello from thread " + threadId;
                String msg = sshExecutor.exec(() -> command, parser);
                System.out.printf("resultMsg: " + msg);
            });
        }

        // 关闭线程池，停止接收新任务
        executor.shutdown();
        try {
            // 等待所有任务完成，最多等待 60 秒
            if (!executor.awaitTermination(160, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // 超时后强制关闭
            }
        } catch (InterruptedException e) {
            executor.shutdownNow(); // 当前线程被中断，强制关闭
            Thread.currentThread().interrupt(); // 恢复中断状态
        }
        System.out.println("success! ");
    }


    @Test
    public void sshjExecutor2Test() {
        SshProperties properties = new SshProperties("127.0.0.1", 22, "root", "123456");
        SshjExecutor sshExecutor = new SshjExecutor(properties);
        ResultParser<String> parser = ResultParserBuilder.build();
        // 提交多个任务
        for (int i = 0; i < 5000; i++) {

            String command = "echo Hello from thread ";
            String msg = sshExecutor.exec(() -> command, parser);
            System.out.printf("resultMsg: " + msg);

        }
    }
}
