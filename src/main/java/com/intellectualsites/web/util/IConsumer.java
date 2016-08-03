package com.intellectualsites.web.util;

import lombok.NonNull;

import java.util.Collection;
import java.util.function.Consumer;

public interface IConsumer<T> extends Consumer<T> {

    default void foreach(@NonNull final Collection<T> collection) {
        collection.forEach(this);
    }

}
