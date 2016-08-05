package com.intellectualsites.web.views;

import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.views.decl.ViewMatcher;

public class SystemView {

    @ViewMatcher(filter = "(\\/system)()()", name = "system")
    public Response systemBase(final Request in) {
        return new Response().setContent("<h1>Static works</h1>");
    }

    @ViewMatcher(filter = "(\\/system\\/)(ram)()", name = "systemram")
    public Response systemRam(final Request in) {
        return new Response().setContent("<h1>{{system.usedram}}MB/{{system.totalram}}MB ({{system.freeram}}MB used)</h1>");
    }
    
}
