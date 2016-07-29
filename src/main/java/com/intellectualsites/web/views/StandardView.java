package com.intellectualsites.web.views;

import com.intellectualsites.web.object.Header;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.object.cache.CacheApplicable;
import com.intellectualsites.web.util.FileUtils;
import com.intellectualsites.web.views.errors.View404;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;

public class StandardView extends View implements CacheApplicable {

    private static final Collection<String> image = new ArrayList<>();

    static {
        image.add("png");
        image.add("ico");
        image.add("gif");
        image.add("jpg");
        image.add("jpeg");
    }

    public StandardView(String filter, Map<String, Object> options) {
        super(filter, "STANDARD", options);
    }

    @Override
    public boolean passes(Matcher matcher, Request request) {
        String folderName = matcher.group(1);
        String fileName = matcher.group(2);
        String extension = matcher.group(3);
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
        File file = (File) r.getMeta("stdfile");
        Response response = new Response(this);

        final String extension = r.getMeta("stdext").toString();

        if (image.contains(extension)) {
            byte[] bytes = new byte[0];
            try {
                BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file), getBuffer());
                bytes = IOUtils.toByteArray(stream);
                stream.close();
            } catch(final Exception e) {
                e.printStackTrace();
            }
            response.getHeader().set(Header.HEADER_CONTENT_TYPE, "image/" + r.getMeta("img_type") + "; charset=utf-8");
            response.setBytes(bytes);
        } else {
            switch (extension) {
                case "html": {
                    response.getHeader().set(Header.HEADER_CONTENT_TYPE, Header.CONTENT_TYPE_HTML);
                }
                break;
                case "js": {
                    response.getHeader().set(Header.HEADER_CONTENT_TYPE, Header.CONTENT_TYPE_JAVASCRIPT);
                }
                break;
                case "less":
                case "css": {
                    response.getHeader().set(Header.HEADER_CONTENT_TYPE, Header.CONTENT_TYPE_CSS);
                }
                break;
                default: {
                    return new View404("TODO").generate(r);
                }
            }
            if (!extension.equals("less")) {
                response.setContent(FileUtils.getDocument(file, getBuffer()));
            } else {
                response.setContent(LessView.getLess(file, getBuffer()));
            }
        }

        return response;
    }
}
