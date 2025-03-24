
# 设计原理
- 通过编码方式执行操作系统命令，获取操作系统相关信息。
- 通过HTTP API的方式桥接客户端对服务端的请求，从而执行Shell命令。  
- 使用HTTP协议作为通信协议，易于实现跨平台和跨语言支持。  

# 使用

## JAVA原生
### 依赖
```xml
<dependency>
    <groupId>com.sove.cloud</groupId>
    <artifactId>command-engine</artifactId>
    <version>lastest-version</version>
</dependency>
```
### 配置连接
```java
SshProperties properties = new SshProperties("127.0.0.1", 22, "root", "123456");
properties.setTimeout(10);
properties.setMaxConnNum(32);
Executor executor = new DefaultExecutor(properties);
```

### 执行命令
```java
Command ipAddrCmd = CommandBuilder.build("ip addr");
ResultParser<String> parser = ResultParserBuilder.build();
String result = executor.exec(ipAddrCmd, parser);
```

