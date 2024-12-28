package com.sove.command;


import com.sove.command.engine.Command;
import com.sove.command.engine.ResultParser;
import com.sove.command.engine.builder.CommandBuilder;
import com.sove.command.engine.builder.ResultParserBuilder;
import com.sove.command.engine.exector.sshj.SshjExecutor;
import com.sove.command.engine.exector.SshProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
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
    public void sshjExecutorTest() {
        SshProperties properties = new SshProperties("192.168.1.109", 22, "root", "123456");
        SshjExecutor sshExecutor = new SshjExecutor(properties);

        Integer sshNum = this.getSshNum(sshExecutor);

        // 创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(10);

        long start = System.currentTimeMillis();

        List<Integer> list = new CopyOnWriteArrayList<>();
        // 提交多个任务
        for (int i = 0; i < 10000; i++) {
            final int threadId = i;
            executor.submit(() -> {
                Integer msg = this.getSshNum(sshExecutor);
                System.out.println("resultMsg: " + msg + "threadId :" + threadId);

                list.add(msg);
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
        long end = System.currentTimeMillis() - start;

        Integer maxSshNum = list.stream()
                .max(Integer::compareTo)
                .orElse(0); // 如果列表为空，返回 null;
        System.out.printf("耗时：%sms, 开始ssh客户端数量：%s, 最大ssh客户端数量：%s%n", end, sshNum, maxSshNum);
    }

    private Integer getSshNum(SshjExecutor sshExecutor) {
        String command = "lsof -i :22 | wc -l";
        return sshExecutor.exec(() -> command, (str ->
                Integer.valueOf(str.replace("\n", ""))
        ));
    }

}
