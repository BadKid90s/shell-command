package com.sove.engine.exector.jsch;

public class JschProperties {
    private final String host;
    private final Integer port;
    private final String username;
    private final String password;
    private Integer timeout = 60;
    private Integer maxConnNum = 32;

    public JschProperties(String host, Integer port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Integer getMaxConnNum() {
        return maxConnNum;
    }

    public void setMaxConnNum(Integer maxConnNum) {
        this.maxConnNum = maxConnNum;
    }
}