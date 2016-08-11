package com.plotsquared.iserver.views.staticviews;

import com.plotsquared.iserver.object.Request;
import com.plotsquared.iserver.object.Response;
import com.plotsquared.iserver.object.cache.CacheApplicable;
import com.plotsquared.iserver.views.View;
import com.plotsquared.iserver.views.decl.ResponseMethod;
import com.plotsquared.iserver.views.decl.ViewMatcher;

class CachedStaticView extends View implements CacheApplicable
{

    private final ResponseMethod method;

    CachedStaticView(final ViewMatcher matcher, final ResponseMethod method)
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

    @Override
    public boolean isApplicable(final Request r)
    {
        return true;
    }
}
