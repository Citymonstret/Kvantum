package com.intellectualsites.web.util;

import com.intellectualsites.web.object.FileType;
import com.intellectualsites.web.object.Header;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.views.LessView;
import com.intellectualsites.web.views.errors.View404;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@UtilityClass
public class GenericViewUtil {

    private static final Collection<String> image = new ArrayList<>();

    static {
        image.add("png");
        image.add("ico");
        image.add("gif");
        image.add("jpg");
        image.add("jpeg");
    }

    public static Response getGenericResponse(@NonNull final File file, @NonNull final Request request,
                                              @NonNull final Response response, @NonNull final String extension, final int buffer) {
        final boolean isImage = image.contains(extension);
        if (isImage) {
            byte[] imageBytes = FileUtils.getBytes(file, buffer);
            final String imageType = extension.equalsIgnoreCase("ico") ?
                    "x-icon" : (extension.equalsIgnoreCase("jpg") ? "jpeg" : extension);
            response.getHeader().set(Header.HEADER_CONTENT_TYPE, "image/" + imageType + "; charset=utf-8");
            response.setBytes(imageBytes);
        } else {
            final Optional<FileType> type = FileType.byExtension(extension);
            if (!type.isPresent()) {
                return View404.construct(request.getResourceRequest().getResource()).generate(request);
            }
            response.getHeader().set(Header.HEADER_CONTENT_TYPE, type.get().getContentType());
            if (type.get() != FileType.LESS) {
                response.setContent(FileUtils.getDocument(file, buffer));
            } else {
                response.setContent(LessView.getLess(file, buffer));
            }
        }
        return response;
    }

}
