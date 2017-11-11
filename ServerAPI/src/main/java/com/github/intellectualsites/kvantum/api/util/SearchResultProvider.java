package com.github.intellectualsites.kvantum.api.util;

import java.util.Collection;

@FunctionalInterface
public interface SearchResultProvider<T>
{

    Collection<? extends T> getResults(T query);

}
