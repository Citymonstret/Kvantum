package com.intellectualsites.web.views.staticviews;

import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.object.cache.CacheApplicable;
import com.intellectualsites.web.views.View;
import com.intellectualsites.web.views.decl.ResponseMethod;
import com.intellectualsites.web.views.decl.ViewMatcher;
import lombok.NonNull;

class CachedStaticView extends View implements CacheApplicable {

    private final ResponseMethod method;

    CachedStaticView(@NonNull final ViewMatcher matcher, @NonNull final ResponseMethod method) {
        super(matcher.filter(), matcher.name());
        this.method = method;
    }

    @Override
    public boolean passes(@NonNull final Request request) {
        return true;
    }

    @Override
    public final Response generate(@NonNull final Request r) {
        return method.handle(r);
    }

    @Override
    public boolean isApplicable(@NonNull final Request r) {
        return true;
    }
}
