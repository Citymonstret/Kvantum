package com.intellectualsites.web.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectionUtils {

    public static class AnnotatedMethod {
        private final Annotation annotation;
        private final Method method;

        private AnnotatedMethod(final Annotation annotation, final Method method) {
            this.annotation = annotation;
            this.method = method;
        }

        public Annotation getAnnotation() {
            return this.annotation;
        }

        public Method getMethod() {
            return this.method;
        }
    }

    public static List<AnnotatedMethod> getAnnotatedMethods(Class<? extends Annotation> a, Class<?> clazz) {
        List<AnnotatedMethod> annotatedMethods = new ArrayList<>();

        Class<?> c = clazz;
        while (c != Object.class) {
            final List<Method> allMethods = new ArrayList<>(Arrays.asList(c.getDeclaredMethods()));
            for (final Method method : allMethods) {
                if (method.isAnnotationPresent(a)) {
                    Annotation[] annotations = method.getAnnotations();
                    for (Annotation an : annotations) {
                        if (a.isInstance(an)) {
                            annotatedMethods.add(new AnnotatedMethod(an, method));
                        }
                    }
                }
            }
            c = c.getSuperclass();
        }

        return annotatedMethods;
    }
}
