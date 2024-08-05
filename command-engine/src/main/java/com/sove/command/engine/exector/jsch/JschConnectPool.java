package com.sove.command.engine.exector.jsch;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.ssh.JschRuntimeException;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class JschConnectPool {

    private final Integer maxConnNum;
    private final Integer timeout;

    private final Map<String, Deque<Session>> sessionQueueMap = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> sessionConnNumMap = new ConcurrentHashMap<>();

    public JschConnectPool(Integer maxConnNum, Integer timeout) {
        this.maxConnNum = maxConnNum;
        this.timeout = timeout;
    }

    protected Session getSession(String host, Integer port, String username, String password) {
        String key = buildKey(username, host, port);
        Deque<Session> sessionQueue = sessionQueueMap.computeIfAbsent(key, k -> new ArrayDeque<>(maxConnNum));
        AtomicInteger sessionConnNum = sessionConnNumMap.computeIfAbsent(key, k -> new AtomicInteger(0));

        synchronized (sessionQueue) {

            if (sessionQueue.isEmpty() && sessionConnNum.get() < maxConnNum) {
                Session session = this.buildSession(username, host, port, password);
                sessionQueue.addLast(session);
                sessionConnNum.incrementAndGet();
            }

            while (sessionQueue.isEmpty()) {
                try {
                    sessionQueue.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for a session.", e);
                }
            }

            Session session = sessionQueue.pollFirst();
            if (session != null && !session.isConnected()) {
                // Reconnect if the session is not connected
                session = this.buildSession(username, host, port, password);
                sessionQueue.addLast(session);
            }
            return session;
        }
    }

    private static String buildKey(String username, String host, Integer port) {
        return StrUtil.format("{}@{}:{}", username, host, port);
    }

    private Session buildSession(String username, String host, Integer port, String password) {
        final JSch jsch = new JSch();
        Session session;
        try {
            session = jsch.getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            // Enable server alive mechanism
            session.setServerAliveInterval(10 * 1000); // Send a heartbeat every 10 seconds
            session.setServerAliveCountMax(10); // Limit the number of heartbeats before disconnecting

            session.connect(timeout * 1000);
            return session;
        } catch (JSchException e) {
            throw new JschRuntimeException(e);
        }
    }

    protected void releaseSession(String username, String host, Integer port, Session session) {
        String key = buildKey(username, host, port);
        Deque<Session> sessionQueue = sessionQueueMap.get(key);

        if (sessionQueue == null || !session.isConnected()) {
            return;
        }

        synchronized (sessionQueue) {
            sessionQueue.addLast(session);
            sessionQueue.notifyAll(); // Notify waiting threads that a session is available
        }
    }
}