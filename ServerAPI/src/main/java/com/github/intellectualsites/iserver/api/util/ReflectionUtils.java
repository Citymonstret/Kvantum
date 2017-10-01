/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
 * <p>
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.github.intellectualsites.iserver.api.util;

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
