package com.intellectualsites.web.views;

import com.intellectualsites.web.object.*;
import com.intellectualsites.web.object.cache.CacheApplicable;
import com.intellectualsites.web.util.FileUtils;

import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Created 2015-04-22 for IntellectualServer
 *
 * @author Citymonstret
 */
public class JSView extends View implements CacheApplicable {

    public JSView(String filter, Map<String, Object> options) {
        super(filter, "javascript", options);
        super.relatedFolderPath = "/assets/js";
    }

    @Override
    public boolean passes(Matcher matcher, Request request) {
        String file = matcher.group(2);
        if (!file.endsWith(".js"))
            file = file + ".js";
        request.addMeta("js_file", file);
        return matcher.matches() && (new  File(getFolder(), file)).exists();
    }


    @Override
    public Response generate(final Request r) {
        File file = new File(getFolder(), r.getMeta("js_file").toString());
        Response response = new Response(this);
        response.getHeader().set(Header.HEADER_CONTENT_TYPE, Header.CONTENT_TYPE_JAVASCRIPT);
        response.setContent(FileUtils.getDocument(file, getBuffer()));
        return response;
    }

    @Override
    public boolean isApplicable(Request r) {
        return true;
    }
}
