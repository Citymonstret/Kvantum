package com.intellectualsites.web.views.decl;

import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import lombok.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final public class ResponseMethod {

    private final Method method;
    private final Object instance;

    public ResponseMethod(@NonNull final Method method, @NonNull final Object instance) {
        this.method = method;
        this.method.setAccessible(true);
        this.instance = instance;
    }

    public Response handle(@NonNull final Request r) {
        try {
            return (Response) this.method.invoke(instance, r);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

}
