package com.intellectualsites.web.views;

import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.object.cache.CacheApplicable;
import com.intellectualsites.web.util.GenericViewUtil;

import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;

public class StandardView extends View implements CacheApplicable {

    public StandardView(String filter, Map<String, Object> options) {
        super(filter, "STANDARD", options);
    }

    @Override
    public boolean passes(Matcher matcher, Request request) {
        String folderName = matcher.group(1);
        String fileName = matcher.group(2);
        String extension = matcher.group(3);
        if (extension.isEmpty()) {
            if (containsOption("defaultExt")) {
                extension = getOption("defaultExt");
            }
        }
        File folder = new File(getFolder(), folderName);
        File file = new File(folder, fileName + "." + extension);
        if (!file.exists()) {
            return false;
        }
        request.addMeta("stdfile", file);
        request.addMeta("stdext", extension.toLowerCase());
        return true;
    }

    @Override
    public boolean isApplicable(Request r) {
        return false; // TODO: Turn on cache when done
    }

    @Override
    public Response generate(final Request r) {
        final File file = (File) r.getMeta("stdfile");
        final Response response = new Response(this);
        final String extension = r.getMeta("stdext").toString();
        return GenericViewUtil.getGenericResponse(file, r, response, extension, getBuffer());
    }
}
