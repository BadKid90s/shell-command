package com.sove.command.engine;


import com.sove.command.engine.builder.CommandBuilder;
import com.sove.command.engine.builder.ResultParserBuilder;

import java.util.function.Supplier;

/**
 * 命令执行器
 *
 * @author JSZ5544
 */
public interface Executor {

    /**
     * 执行命令
     *
     * @param command 命令
     * @param parser  结果解析器
     * @return 结果解析器处理后的结果
     */
    <T> T execute(Command command, ResultParser<T> parser);

    /**
     * 执行命令
     *
     * @param sshHost 主机
     * @param sshPort 端口
     * @param sshUser 用户名
     * @param sshPass 密码
     * @param command 命令
     * @param parser  结果解析器
     * @return 结果解析器处理后的结果
     */
    <T> T execute(String sshHost, int sshPort, String sshUser, String sshPass, Command command,
                  ResultParser<T> parser);


    /**
     * 执行命令服务
     *
     * @param cmd 命令字符串
     * @return 执行结果或异常信息
     */
    default String execute(String cmd) {
        return this.execute(CommandBuilder.build(cmd), ResultParserBuilder.build());
    }


    /**
     * 执行命令
     *
     * @param sshHost 主机
     * @param sshPort 端口
     * @param sshUser 用户名
     * @param sshPass 密码
     * @param cmd     命令
     * @return 执行结果或异常信息
     */
    default String execute(String sshHost, int sshPort, String sshUser, String sshPass, String cmd) {
        return this.execute(sshHost, sshPort, sshUser, sshPass, CommandBuilder.build(cmd), ResultParserBuilder.build());
    }

    /**
     * 执行命令服务
     *
     * @param cmd 命令对象
     */
    default String execute(Command cmd) {
        return this.execute(cmd, ResultParserBuilder.build());
    }

    /**
     * 执行命令服务
     *
     * @param cmd    命令对象
     * @param parser 结果解析器函数
     * @return 结果解析器解析后的对象
     */
    default <T> T execute(Command cmd, Supplier<ResultParser<T>> parser) {
        return this.execute(cmd, parser.get());
    }

    /**
     * 执行命令服务
     *
     * @param cmd    命令对象函数
     * @param parser 结果解析器函数
     * @return 结果解析器解析后的对象
     */
    default <T> T execute(Supplier<Command> cmd, Supplier<ResultParser<T>> parser) {
        return this.execute(cmd.get(), parser.get());
    }


    /**
     * 执行命令服务
     *
     * @param sshHost 主机
     * @param sshPort 端口
     * @param sshUser 用户名
     * @param sshPass 密码
     * @param cmd     命令对象
     * @param parser  结果解析器函数
     * @return 结果解析器解析后的对象
     */
    default <T> T execute(String sshHost, int sshPort, String sshUser, String sshPass, Command cmd, Supplier<ResultParser<T>> parser) {
        return this.execute(sshHost, sshPort, sshUser, sshPass, cmd, parser.get());
    }

    /**
     * 执行命令服务
     *
     * @param sshHost 主机
     * @param sshPort 端口
     * @param sshUser 用户名
     * @param sshPass 密码
     * @param cmd     命令对象函数
     * @param parser  结果解析器函数
     * @return 结果解析器解析后的对象
     */
    default <T> T execute(String sshHost, int sshPort, String sshUser, String sshPass, Supplier<Command> cmd, Supplier<ResultParser<T>> parser) {
        return this.execute(sshHost, sshPort, sshUser, sshPass, cmd.get(), parser.get());
    }
}
