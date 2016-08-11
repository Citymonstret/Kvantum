package com.intellectualsites.web.views;

import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.object.cache.CacheApplicable;
import com.intellectualsites.web.util.GenericViewUtil;

import java.io.File;
import java.util.Map;

public class StandardView extends View implements CacheApplicable {

    public StandardView(String filter, Map<String, Object> options) {
        super(filter, "STANDARD", options);
    }

    @Override
    public boolean passes(Request request) {
        String folderName, fileName, extension;
        final Map<String, String> variables = request.getVariables();
        folderName = variables.get("folder");
        fileName = variables.get("file");
        extension = variables.get("extension").replace(".", "");

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
        return true;
    }

    @Override
    public Response generate(final Request r) {
        final File file = (File) r.getMeta("stdfile");
        final Response response = new Response(this);
        final String extension = r.getMeta("stdext").toString();
        return GenericViewUtil.getGenericResponse(file, r, response, extension, getBuffer());
    }
}
