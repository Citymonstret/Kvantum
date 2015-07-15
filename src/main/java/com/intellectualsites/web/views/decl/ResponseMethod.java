package com.intellectualsites.web.views.decl;

import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ResponseMethod {

    private final Method method;
    private final Object instance;

    public ResponseMethod(final Method method, final Object instance) {
        this.method = method;
        this.method.setAccessible(true);
        this.instance = instance;
    }

    public Response handle(final Request r) {
        try {
            return (Response) this.method.invoke(instance, r);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

}
