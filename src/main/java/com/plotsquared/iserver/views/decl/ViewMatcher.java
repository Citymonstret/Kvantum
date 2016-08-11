package com.plotsquared.iserver.views.decl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ViewMatcher {

    String filter();

    String name();

    boolean cache() default true;

}
