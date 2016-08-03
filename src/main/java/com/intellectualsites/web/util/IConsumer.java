package com.intellectualsites.web.util;

import lombok.NonNull;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface IConsumer<T> extends Consumer<T> {

    default void foreach(@NonNull final Predicate<T> predicate, @NonNull final Collection<T> collection) {
        collection.stream().filter(predicate).forEach(this);
    }

    default void foreach(@NonNull final Collection<T> collection) {
        collection.forEach(this);
    }

}
