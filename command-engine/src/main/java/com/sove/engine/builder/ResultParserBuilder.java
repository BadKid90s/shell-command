package com.sove.engine.builder;

import com.sove.engine.ResultParser;

import java.util.function.Function;

public class ResultParserBuilder {

    public static ResultParser<String> build() {
        return (str) -> str;
    }

    public static <R> ResultParser<R> build(Function<String, R> function) {
        return function::apply;
    }
}
