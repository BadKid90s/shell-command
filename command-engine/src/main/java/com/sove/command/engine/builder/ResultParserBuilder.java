package com.sove.command.engine.builder;

import com.sove.command.engine.ResultParser;

import java.util.function.Function;

public class ResultParserBuilder {

    public static ResultParser<String> build() {
        return (str) -> str;
    }

    public static <R> ResultParser<R> build(Function<String, R> function) {
        return function::apply;
    }
}
