package com.sove.command.engine;

@FunctionalInterface
public interface ResultParser<T> {

    T parse(String str);
}
