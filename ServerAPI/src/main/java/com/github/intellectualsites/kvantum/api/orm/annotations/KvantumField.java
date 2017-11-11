package com.github.intellectualsites.kvantum.api.orm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.FIELD )
public @interface KvantumField
{

    String kvantumName() default "";

    String defaultValue() default "null";

    boolean isRequired() default false;

}
