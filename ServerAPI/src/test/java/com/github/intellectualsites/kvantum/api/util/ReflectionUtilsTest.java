/*
 * Kvantum is a web server, written entirely in the Java language.
 * Copyright (C) 2017 IntellectualSites
 *
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.github.intellectualsites.kvantum.api.util;

import com.google.common.annotations.Beta;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

class ReflectionUtilsTest
{

    @Test
    void getAnnotatedMethods()
    {
        final List<ReflectionUtils.AnnotatedMethod<SampleAnnotation>> methodList = ReflectionUtils
                .getAnnotatedMethods(  SampleAnnotation.class, ReflectionUtilsTest.class );
        Assertions.assertEquals( 1, methodList.size() );
        final ReflectionUtils.AnnotatedMethod<SampleAnnotation> method = methodList.get( 0 );
        Assertions.assertNotNull( method );
        final SampleAnnotation annotation = method.getAnnotation();
        Assertions.assertEquals( "Hello Schweeden!", annotation.content() );
    }

    @Beta
    @SampleAnnotation( content = "Hello Schweeden!" )
    void sampleMethod()
    {
    }

    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.METHOD )
    private @interface SampleAnnotation {

        String content() default "Hello World!";
    }

}
