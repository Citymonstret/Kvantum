package com.plotsquared.iserver.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final public class ReflectionUtils
{

    public static List<AnnotatedMethod> getAnnotatedMethods(final Class<? extends Annotation> a, final Class<?> clazz)
    {
        Assert.notNull( a, clazz );

        final List<AnnotatedMethod> annotatedMethods = new ArrayList<>();
        Class<?> c = clazz;
        while ( c != Object.class )
        {
            final List<Method> allMethods = new ArrayList<>( Arrays.asList( c.getDeclaredMethods() ) );
            allMethods.stream().filter( method -> method.isAnnotationPresent( a ) ).forEach( method ->
                    LambdaUtil.arrayForeach( method.getAnnotations(), a::isInstance, an -> annotatedMethods.add( new
                            AnnotatedMethod( an, method ) ) ) );
            c = c.getSuperclass();
        }
        return annotatedMethods;
    }

    public static class AnnotatedMethod
    {

        private final Annotation annotation;
        private final Method method;

        private AnnotatedMethod(final Annotation annotation, final Method method)
        {
            Assert.notNull( annotation, method );

            this.annotation = annotation;
            this.method = method;
        }

        public Annotation getAnnotation()
        {
            return annotation;
        }

        public Method getMethod()
        {
            return method;
        }
    }

}
