package com.sove.engine.builder;

import com.sove.engine.Command;

public class CommandBuilder {

    public static Command build(String cmd) {
        return () -> cmd;
    }
}
