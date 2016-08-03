package com.intellectualsites.web.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

@UtilityClass
public class LambdaUtil {

    public static <T> Optional<T> getFirst(@NonNull final Collection<T> collection, @NonNull final Predicate<T> predicate) {
        return collection.stream().filter(predicate).findFirst();
    }

    public static <T> Optional<T> getFirst(@NonNull final T[] collection, @NonNull final Predicate<T> predicate) {
        return Arrays.stream(collection).filter(predicate).findFirst();
    }

    public static <T> T[] arrayAssign(@NonNull final T[] array, @NonNull final Provider<T> provider) {
         for (int i = 0; i < array.length; i++) {
             array[i] = provider.provide();
         }
         return array;
    }

    public static <T> void arrayForeach(@NonNull final T[] array, @NonNull final Consumer<T> consumer) {
        Arrays.stream(array).forEach(consumer);
    }

    public static <T> void arrayForeach(@NonNull final T[] array, @NonNull final Predicate<T> filter, @NonNull final Consumer<T> consumer) {
        Arrays.stream(array).filter(filter).forEach(consumer);
    }

}
