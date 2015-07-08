package com.intellectualsites.web.views;

import com.intellectualsites.web.object.*;
import com.intellectualsites.web.util.Context;
import com.intellectualsites.web.util.FileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Created 2015-04-19 for IntellectualServer
 *
 * @author Citymonstret
 */
public class HTMLView extends View implements CacheApplicable {

    private final File folder;
    private final int buffer;

    public HTMLView(String filter, Map<String, Object> options) {
        super(filter, options);

        if (containsOption("buffer")) {
            this.buffer = getOption("buffer");
        } else {
            this.buffer = 1024 * 64;
        }
        if (containsOption("folder")) {
            this.folder = new File(getOption("folder").toString());
        } else {
            this.folder = new File(Context.coreFolder, "/html");
        }

        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                System.out.println("Couldn't create the html folder...");
            }
        }
    }

    @Override
    public boolean passes(Matcher matcher, Request request) {
        String file = matcher.group(2);
        if (file == null || file.equals("")) {
            file = "index";
        }
        request.addMeta("html_file", file);
        return foundFile(file);
    }

    @Override
    public Response generate(final Request r) {
        File file = new File(folder, r.getMeta("html_file") + ".html");
        Response response = new Response(this);
        response.getHeader().set(Header.HEADER_CONTENT_TYPE, Header.CONTENT_TYPE_HTML);
        response.setContent(FileUtils.getDocument(file, buffer));
        return response;
    }

    private boolean foundFile(final String file) {
        return new File(folder, file + ".html").exists();
    }

    @Override
    public HTMLProvider getFactory(final Request r) {
        return new HTMLProvider(r);
    }

    @Override
    public boolean isApplicable(Request r) {
        return true;
    }

    public class HTMLProvider implements ProviderFactory<HTMLProvider>, VariableProvider {

        private Map<String, String> storage = new HashMap<>();
        public HTMLProvider(final Request r) {
            storage.put("name", r.getMeta("html_file") + ".html");
        }

        @Override
        public HTMLProvider get(Request r) {
            return this;
        }

        @Override
        public String providerName() {
            return "document";
        }

        @Override
        public boolean contains(String variable) {
            return storage.containsKey(variable);
        }

        @Override
        public Object get(String variable) {
            return storage.get(variable);
        }
    }
}
