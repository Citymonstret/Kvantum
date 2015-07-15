package com.intellectualsites.web.views.staticviews;

import com.intellectualsites.web.core.Server;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.util.ReflectionUtils;
import com.intellectualsites.web.views.View;
import com.intellectualsites.web.views.decl.ResponseMethod;
import com.intellectualsites.web.views.decl.ViewDeclaration;
import com.intellectualsites.web.views.decl.ViewMatcher;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class StaticViewManager {

    public static void generate(final ViewDeclaration viewDeclaration) throws Exception {
        final Class<? extends ViewDeclaration> clazz = viewDeclaration.getClass();
        final List<ReflectionUtils.AnnotatedMethod> annotatedMethods = ReflectionUtils.getAnnotatedMethods(ViewMatcher.class, clazz);
        for (final ReflectionUtils.AnnotatedMethod annotatedMethod : annotatedMethods) {
            Method m = annotatedMethod.getMethod();
            if (!Response.class.equals(m.getReturnType())) {
                new IllegalArgumentException("M doesn't return response").printStackTrace();
            } else {
                if (!Arrays.equals(m.getParameterTypes(), new Class<?>[]{Request.class})) {
                    new IllegalArgumentException("M has wrong parameter types").printStackTrace();
                } else {
                    ViewMatcher matcher = (ViewMatcher) annotatedMethod.getAnnotation();
                    View view;
                    if (matcher.cache()) {
                        view = new CachedStaticView(matcher, new ResponseMethod(m, viewDeclaration));
                    } else {
                        view = new StaticView(matcher, new ResponseMethod(m, viewDeclaration));
                    }
                    Server.getInstance().getViewManager().add(view);
                }
            }
        }
    }

}
