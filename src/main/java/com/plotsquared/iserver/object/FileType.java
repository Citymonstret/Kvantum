package com.plotsquared.iserver.object;

import com.plotsquared.iserver.util.Assert;
import com.plotsquared.iserver.util.LambdaUtil;

import java.util.Optional;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public enum FileType {
    HTML("html", Header.CONTENT_TYPE_HTML),
    CSS("css", Header.CONTENT_TYPE_CSS),
    JAVASCRIPT("js", Header.CONTENT_TYPE_JAVASCRIPT),
    LESS("less", Header.CONTENT_TYPE_CSS);

    private final String extension;

    private final String contentType;

    FileType(String extension, String contentType) {

        this.extension = extension;
        this.contentType = contentType;
    }

    public static Optional<FileType> byExtension(final String ext) {
        Assert.notNull(ext);


        final Predicate<FileType> filter = type -> type.extension.equalsIgnoreCase(ext);
        return LambdaUtil.getFirst(values(), filter);
    }

    public String getExtension() {
        return extension;
    }

    public String getContentType() {
        return contentType;
    }

}
