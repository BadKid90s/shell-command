package com.sove.command.engine.exector.sshj;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.ssh.JschRuntimeException;
import net.schmizz.keepalive.KeepAliveProvider;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SshjConnectPool {

    private final Logger log = LoggerFactory.getLogger(SshjConnectPool.class);
    private final Integer timeout;
    private final Map<String, SSHClient> sshClientMap = new ConcurrentHashMap<>();
    private final Lock lock = new ReentrantLock();

    public SshjConnectPool(Integer timeout) {
        this.timeout = timeout;
    }

    protected Session getSession(String host, Integer port, String username, String password) throws TransportException, ConnectionException {
        String key = buildKey(username, host, port);
        SSHClient sshClient;

        // 使用 computeIfAbsent 简化逻辑
        sshClient = sshClientMap.computeIfAbsent(key, k -> {
            log.debug("构建SShClient");
            return buildClient(username, host, port, password);
        });

        // 检查客户端是否连接，如果没有则重建
        if (!sshClient.isConnected()) {
            log.debug("关闭后构建SShClient");
            sshClient = buildClient(username, host, port, password);
            sshClientMap.put(key, sshClient);
        } else {
            log.debug("缓存中获取SShClient");
        }


        return sshClient.startSession();
    }

    private static String buildKey(String username, String host, Integer port) {
        return StrUtil.format("{}@{}:{}", username, host, port);
    }

    private SSHClient buildClient(String username, String host, Integer port, String password) {
        lock.lock(); // 获取锁
        try {
            DefaultConfig defaultConfig = new DefaultConfig();
            defaultConfig.setKeepAliveProvider(KeepAliveProvider.KEEP_ALIVE);
            SSHClient sshClient = new SSHClient(defaultConfig);
            sshClient.addHostKeyVerifier(new PromiscuousVerifier());
            sshClient.getConnection().getKeepAlive().setKeepAliveInterval(5);
            sshClient.connect(host, port);
            sshClient.authPassword(username, password);
            sshClient.setTimeout(timeout);
            sshClient.setConnectTimeout(timeout);
            return sshClient;
        } catch (IOException e) {
            throw new JschRuntimeException(e);
        } finally {
            lock.unlock(); // 释放锁
        }
    }

}