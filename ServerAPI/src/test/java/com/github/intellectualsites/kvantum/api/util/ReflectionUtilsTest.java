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
