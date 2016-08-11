package com.plotsquared.iserver.views.staticviews;

import com.plotsquared.iserver.object.Request;
import com.plotsquared.iserver.object.Response;
import com.plotsquared.iserver.views.View;
import com.plotsquared.iserver.views.decl.ResponseMethod;
import com.plotsquared.iserver.views.decl.ViewMatcher;

class StaticView extends View
{

    private final ResponseMethod method;

    StaticView(final ViewMatcher matcher, final ResponseMethod method)
    {
        super( matcher.filter(), matcher.name() );
        this.method = method;
    }

    @Override
    public boolean passes(final Request request)
    {
        return true;
    }

    @Override
    public final Response generate(final Request r)
    {
        return method.handle( r );
    }
}
