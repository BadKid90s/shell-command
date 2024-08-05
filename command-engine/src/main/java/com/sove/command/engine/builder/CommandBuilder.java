package com.sove.command.engine.builder;

import com.sove.command.engine.Command;

public class CommandBuilder {

    public static Command build(String cmd) {
        return () -> cmd;
    }
}
