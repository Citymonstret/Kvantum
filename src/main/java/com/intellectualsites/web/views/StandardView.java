package com.intellectualsites.web.views;

import com.intellectualsites.web.object.FileType;
import com.intellectualsites.web.object.Header;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.object.cache.CacheApplicable;
import com.intellectualsites.web.util.FileUtils;
import com.intellectualsites.web.views.errors.View404;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
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
        if (image.contains(extension)) {
            byte[] bytes = FileUtils.getBytes(file, getBuffer());
            response.getHeader().set(Header.HEADER_CONTENT_TYPE, "image/" + r.getMeta("img_type") + "; charset=utf-8");
            response.setBytes(bytes);
        } else {
            final Optional<FileType> type = FileType.byExtension(extension);
            if (!type.isPresent()) {
                return View404.construct(r.getResourceRequest().getResource()).generate(r);
            }
            response.getHeader().set(Header.HEADER_CONTENT_TYPE, type.get().getContentType());
            if (type.get() != FileType.LESS) {
                response.setContent(FileUtils.getDocument(file, getBuffer()));
            } else {
                response.setContent(LessView.getLess(file, getBuffer()));
            }
        }
        return response;
    }
}
