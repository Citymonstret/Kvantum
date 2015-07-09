package com.intellectualsites.web.views.errors;

import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.views.View;

import java.util.regex.Matcher;

/**
 * Created 2015-04-19 for IntellectualServer
 *
 * @author Citymonstret
 */
public class Error extends View {

    private final int code;
    private final String desc;

    public Error(final int code, final String desc) {
        super("", "error");
        this.code = code;
        this.desc = desc;
    }

    @Override
    public boolean passes(Matcher matcher, Request request) {
        return true;
    }

    @Override
    public Response generate(final Request r) {
        Response response = new Response(this);
        response.setHeader(response.getHeader().setStatus(this.code + " " + this.desc));
        response.setContent(String.format("<p><b>Error:</b> %d %s</p>", this.code, this.desc));
        return response;
    }

}
