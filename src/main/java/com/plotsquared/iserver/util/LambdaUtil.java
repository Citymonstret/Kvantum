package com.plotsquared.iserver.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class LambdaUtil
{

    public static <T> Optional<T> getFirst(final Collection<T> collection, final Predicate<T> predicate)
    {
        Assert.notNull( collection, predicate );

        return collection.stream().filter( predicate ).findFirst();
    }

    public static <T> Optional<T> getFirst(final T[] collection, final Predicate<T> predicate)
    {
        Assert.notNull( collection, predicate );

        return Arrays.stream( collection ).filter( predicate ).findFirst();
    }

    public static <T> T[] arrayAssign(final T[] array, final Provider<T> provider)
    {
        Assert.notNull( array, provider );

        for ( int i = 0; i < array.length; i++ )
        {
            array[ i ] = provider.provide();
        }
        return array;
    }

    public static <T> void arrayForeach(final T[] array, final Consumer<T> consumer)
    {
        Assert.notNull( array, consumer );

        Arrays.stream( array ).forEach( consumer );

    }

    @SafeVarargs
    public static <T> void arrayForeach(final Consumer<T> consumer, final T... array)
    {
        Assert.notNull( array, consumer );

        Arrays.stream( array ).forEach( consumer );
    }


    public static <T> void arrayForeach(final T[] array, final Predicate<T> filter, final Consumer<T> consumer)
    {
        Assert.notNull( array, filter, consumer );

        Arrays.stream( array ).filter( filter ).forEach( consumer );
    }

}
