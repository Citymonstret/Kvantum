package com.intellectualsites.web.bukkit.plotsquared;

import java.io.File;
import java.util.regex.Matcher;

import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.util.FileUtils;
import com.intellectualsites.web.views.View;

public class UploadView extends View {

    final File template;

    public UploadView(final File template) {
        super("(\\/upload\\/)(\\?query=([\\S\\s]*))", "plotupload");
        this.template = template;
    }

    @Override
    public boolean passes(Matcher matcher, Request request) {
        return true;
    }

    @Override
    public Response generate(final Request in) {
        Response response = new Response(this);
        response.setContent(FileUtils.getDocument(template, getBuffer()).replace("{{results}}", "Not implemented"));
        return response;
    }
}
