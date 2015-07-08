package com.intellectualsites.web.views;

import com.intellectualsites.web.object.*;
import com.intellectualsites.web.util.Context;
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

    private File folder;
    private int buffer;

    public CSSView(String filter, Map<String, Object> options) {
        super(filter, options);

        if (containsOption("buffer")) {
            this.buffer = getOption("buffer");
        } else {
            this.buffer = 1024 * 64;
        }
        if (containsOption("folder")) {
            this.folder = new File(getOption("folder").toString());
        } else {
            this.folder = new File(Context.coreFolder, "/assets/css");
        }

        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                System.out.println("Couldn't create the css folder...");
            }
        }
    }

    @Override
    public boolean passes(Matcher matcher, Request request) {
        String file = matcher.group(2);
        if (!file.endsWith(".css"))
            file = file + ".css";
        request.addMeta("css_file", file);
        return matcher.matches() && (new  File(folder, file)).exists();
    }


    @Override
    public Response generate(final Request r) {
        File file = new File(folder, r.getMeta("css_file").toString());
        Response response = new Response(this);
        response.getHeader().set(Header.HEADER_CONTENT_TYPE, Header.CONTENT_TYPE_CSS);
        response.setContent(FileUtils.getDocument(file, buffer));
        return response;
    }

    @Override
    public boolean isApplicable(Request r) {
        return true;
    }
}
