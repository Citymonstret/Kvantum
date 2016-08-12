package com.plotsquared.iserver.views.staticviews;

import com.plotsquared.iserver.views.decl.ResponseMethod;
import com.plotsquared.iserver.views.decl.ViewMatcher;
import com.plotsquared.iserver.views.requesthandler.SimpleRequestHandler;

class StaticView extends SimpleRequestHandler
{

    private final ResponseMethod method;

    StaticView(final ViewMatcher matcher, final ResponseMethod method)
    {
        super( matcher.filter(), method );
        this.method = method;
    }

}
