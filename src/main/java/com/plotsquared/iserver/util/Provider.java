package com.plotsquared.iserver.util;

@FunctionalInterface
public interface Provider<T> {

    T provide();

}
