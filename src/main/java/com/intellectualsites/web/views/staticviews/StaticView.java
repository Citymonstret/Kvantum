package com.intellectualsites.web.views.staticviews;

import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.views.View;
import com.intellectualsites.web.views.decl.ResponseMethod;
import com.intellectualsites.web.views.decl.ViewMatcher;
import lombok.NonNull;

import java.util.regex.Matcher;

class StaticView extends View {

    private final ResponseMethod method;

    StaticView(final ViewMatcher matcher, final ResponseMethod method) {
        super(matcher.filter(), matcher.name());
        this.method = method;
    }

    @Override
    public boolean passes(@NonNull final Matcher matcher, @NonNull final Request request) {
        request.addMeta("matcher", matcher);
        return true;
    }

    @Override
    public final Response generate(@NonNull final Request r) {
        return method.handle(r);
    }
}
