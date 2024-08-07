package com.sove.command.engine.exector.sshj;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.ssh.JschRuntimeException;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SshjConnectPool {

    private final Integer maxConnNum;
    private final Integer timeout;

    private final Map<String, Deque<SSHClient>> queueMap = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> connNumMap = new ConcurrentHashMap<>();

    public SshjConnectPool(Integer maxConnNum, Integer timeout) {
        this.maxConnNum = maxConnNum;
        this.timeout = timeout;
    }

    protected SSHClient getClient(String host, Integer port, String username, String password) {
        String key = buildKey(username, host, port);
        Deque<SSHClient> queue = queueMap.computeIfAbsent(key, k -> new ArrayDeque<>(maxConnNum));
        AtomicInteger sessionConnNum = connNumMap.computeIfAbsent(key, k -> new AtomicInteger(0));

        SSHClient client;
        synchronized (queue) {
            if (queue.isEmpty() && sessionConnNum.get() < maxConnNum) {
                client = this.buildClient(username, host, port, password);
                queue.addLast(client);
                sessionConnNum.incrementAndGet();
            }

            while (queue.isEmpty()) {
                try {
                    queue.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for a session.", e);
                }
            }

            client = queue.pollFirst();
            if (client != null && !client.isConnected()) {
                // Reconnect if the session is not connected
                client = this.buildClient(username, host, port, password);
                queue.addLast(client);
            }
            return client;
        }
    }

    private static String buildKey(String username, String host, Integer port) {
        return StrUtil.format("{}@{}:{}", username, host, port);
    }

    private SSHClient buildClient(String username, String host, Integer port, String password) {
        try {
            SSHClient sshClient = new SSHClient();
            sshClient.addHostKeyVerifier(new PromiscuousVerifier());
            sshClient.connect(host, port); // 连接到远程主机
            sshClient.authPassword(username, password); // 使用密码进行身份验证
            sshClient.setTimeout(timeout);
            sshClient.setConnectTimeout(timeout);
            return sshClient;
        } catch (IOException e) {
            throw new JschRuntimeException(e);
        }
    }

    protected void release(String username, String host, Integer port, SSHClient client) {
        String key = buildKey(username, host, port);
        Deque<SSHClient> queue = queueMap.get(key);

        if (queue == null || !client.isConnected()) {
            return;
        }

        synchronized (queue) {
            queue.addLast(client);
            queue.notifyAll(); // Notify waiting threads that a session is available
        }
    }

    protected void close(String username, String host, Integer port, SSHClient client) {
        if (client != null && client.isConnected()) {
            IOUtils.closeQuietly(client);
        }
        String key = buildKey(username, host, port);
        AtomicInteger connMaxNum = connNumMap.get(key);
        if (connMaxNum != null) {
            connMaxNum.decrementAndGet();
        }
    }
}