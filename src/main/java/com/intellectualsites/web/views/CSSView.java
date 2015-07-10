package com.intellectualsites.web.views;

import com.intellectualsites.web.object.*;
import com.intellectualsites.web.object.cache.CacheApplicable;
import com.intellectualsites.web.util.FileUtils;

import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Created 2015-04-21 for IntellectualServer
 *
 * @author Citymonstret
 */
public class CSSView extends View implements CacheApplicable {

    public CSSView(String filter, Map<String, Object> options) {
        super(filter, "css", options);
        super.relatedFolderPath = "/assets/css";
    }

    @Override
    public boolean passes(Matcher matcher, Request request) {
        String file = matcher.group(2);
        if (!file.endsWith(".css"))
            file = file + ".css";
        request.addMeta("css_file", file);
        return matcher.matches() && (new  File(getFolder(), file)).exists();
    }


    @Override
    public Response generate(final Request r) {
        File file = new File(getFolder(), r.getMeta("css_file").toString());
        Response response = new Response(this);
        response.getHeader().set(Header.HEADER_CONTENT_TYPE, Header.CONTENT_TYPE_CSS);
        response.setContent(FileUtils.getDocument(file, getBuffer()));
        return response;
    }

    @Override
    public boolean isApplicable(Request r) {
        return true;
    }
}
