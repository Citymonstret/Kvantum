package com.plotsquared.iserver.util;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface IConsumer<T> extends Consumer<T> {

    default void foreach(final Predicate<T> predicate, final Collection<T> collection) {
        Assert.notNull(predicate, collection);

        collection.stream().filter(predicate).forEach(this);
    }

    default void foreach(final Collection<T> collection) {
        Assert.notNull(collection);

        collection.forEach(this);
    }

}
