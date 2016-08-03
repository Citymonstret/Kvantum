package com.intellectualsites.web.util;

@FunctionalInterface
public interface Provider<T> {

    T provide();

}
