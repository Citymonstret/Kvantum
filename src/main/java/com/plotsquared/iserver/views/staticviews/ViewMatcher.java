package com.plotsquared.iserver.views.staticviews;

import com.plotsquared.iserver.views.requesthandler.Middleware;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ViewMatcher
{

    String filter();

    String name();

    Class<? extends Middleware>[] middlewares() default Middleware.class;

    boolean cache() default true;

}
