package com.sove.engine;

@FunctionalInterface
public interface ResultParser<T> {

    T parse(String str);
}
