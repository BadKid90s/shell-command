package com.sove.cloud.server.config;

import com.sove.engine.Executor;
import com.sove.engine.exector.jsch.JschExecutor;
import com.sove.engine.exector.jsch.JschProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableConfigurationProperties(ShellConfig.ShellConfigProperties.class)
public class ShellConfig {


    @Bean
    public Executor defaultExecutor(ShellConfigProperties properties) {
        JschProperties jschProperties = new JschProperties(properties.host, properties.port, properties.username, properties.password);
        jschProperties.setTimeout(properties.timeout);
        return new JschExecutor(jschProperties);
    }

    @ConfigurationProperties(prefix = "ssh")
    public static class ShellConfigProperties {
        private String host;
        private Integer port;
        private String username;
        private String password;
        private Integer timeout = 10;

        public Integer getTimeout() {
            return timeout;
        }

        public void setTimeout(Integer timeout) {
            this.timeout = timeout;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
