package com.intellectualsites.web.views;

import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.views.decl.ViewDeclaration;
import com.intellectualsites.web.views.decl.ViewMatcher;

public class SystemView implements ViewDeclaration {

    @ViewMatcher(filter = "(\\/system)()()", name = "system", cache = true)
    public Response generate(final Request in) {
        Response response = new Response(null);
        response.setContent("<h1>Static works</h1>");
        return response;
    }

}
