package com.intellectualsites.web.views;

import com.intellectualsites.web.object.*;
import com.intellectualsites.web.util.Context;
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

    private File folder;
    private int buffer;

    public JSView(String filter, Map<String, Object> options) {
        super(filter, options);

        if (containsOption("buffer")) {
            this.buffer = getOption("buffer");
        } else {
            this.buffer = 1024 * 64;
        }
        if (containsOption("folder")) {
            this.folder = new File(getOption("folder").toString());
        } else {
            this.folder = new File(Context.coreFolder, "/assets/js");
        }

        if (!this.folder.exists()) {
            if (!this.folder.mkdirs()) {
                System.out.println("Couldn't create the js folder...");
            }
        }
    }

    @Override
    public boolean passes(Matcher matcher, Request request) {
        String file = matcher.group(2);
        if (!file.endsWith(".js"))
            file = file + ".js";
        request.addMeta("js_file", file);
        return matcher.matches() && (new  File(folder, file)).exists();
    }


    @Override
    public Response generate(final Request r) {
        File file = new File(folder, r.getMeta("js_file").toString());
        Response response = new Response(this);
        response.getHeader().set(Header.HEADER_CONTENT_TYPE, Header.CONTENT_TYPE_JAVASCRIPT);
        response.setContent(FileUtils.getDocument(file, buffer));
        return response;
    }

    @Override
    public boolean isApplicable(Request r) {
        return true;
    }
}
