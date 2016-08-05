package com.intellectualsites.web.util;

import lombok.Getter;
import lombok.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectionUtils {

    public static class AnnotatedMethod {
        @Getter
        private final Annotation annotation;
        @Getter
        private final Method method;

        private AnnotatedMethod(@NonNull final Annotation annotation, @NonNull final Method method) {
            this.annotation = annotation;
            this.method = method;
        }
    }

    public static List<AnnotatedMethod> getAnnotatedMethods(@NonNull final Class<? extends Annotation> a, @NonNull final Class<?> clazz) {
        final List<AnnotatedMethod> annotatedMethods = new ArrayList<>();
        Class<?> c = clazz;
        while (c != Object.class) {
            final List<Method> allMethods = new ArrayList<>(Arrays.asList(c.getDeclaredMethods()));
            allMethods.stream().filter(method -> method.isAnnotationPresent(a)).forEach(method ->
                    LambdaUtil.arrayForeach(method.getAnnotations(), a::isInstance, an -> annotatedMethods.add(new AnnotatedMethod(an, method))));
            c = c.getSuperclass();
        }
        return annotatedMethods;
    }

}
