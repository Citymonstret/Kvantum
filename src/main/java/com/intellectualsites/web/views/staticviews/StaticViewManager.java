package com.intellectualsites.web.views.staticviews;

import com.intellectualsites.web.core.Server;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.util.IConsumer;
import com.intellectualsites.web.util.ReflectionUtils;
import com.intellectualsites.web.views.View;
import com.intellectualsites.web.views.decl.ResponseMethod;
import com.intellectualsites.web.views.decl.ViewMatcher;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@UtilityClass
final public class StaticViewManager {

    private static final Class<?>[] parameters = new Class<?>[]{Request.class};

    public static void generate(@NonNull final Object viewDeclaration) throws Exception {
        final Class<?> clazz = viewDeclaration.getClass();
        final List<ReflectionUtils.AnnotatedMethod> annotatedMethods = ReflectionUtils.getAnnotatedMethods(ViewMatcher.class, clazz);
        ((IConsumer<ReflectionUtils.AnnotatedMethod>) annotatedMethod -> {
            final Method m = annotatedMethod.getMethod();
            if (!Response.class.equals(m.getReturnType())) {
                new IllegalArgumentException("M doesn't return response").printStackTrace();
            } else {
                if (!Arrays.equals(m.getParameterTypes(), parameters)) {
                    new IllegalArgumentException("M has wrong parameter types").printStackTrace();
                } else {
                    final ViewMatcher matcher = (ViewMatcher) annotatedMethod.getAnnotation();
                    final View view;
                    if (matcher.cache()) {
                        view = new CachedStaticView(matcher, new ResponseMethod(m, viewDeclaration));
                    } else {
                        view = new StaticView(matcher, new ResponseMethod(m, viewDeclaration));
                    }
                    Server.getInstance().getRequestManager().add(view);
                }
            }
        }).foreach(annotatedMethods);
    }

}
