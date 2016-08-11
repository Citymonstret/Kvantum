package com.intellectualsites.web.object;

import com.intellectualsites.web.util.LambdaUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.function.Predicate;

@SuppressWarnings("unused")
@RequiredArgsConstructor
public enum FileType {
    HTML("html", Header.CONTENT_TYPE_HTML),
    CSS("css", Header.CONTENT_TYPE_CSS),
    JAVASCRIPT("js", Header.CONTENT_TYPE_JAVASCRIPT),
    LESS("less", Header.CONTENT_TYPE_CSS)
    ;

    @Getter
    @NonNull
    private final String extension;

    @Getter
    @NonNull
    private final String contentType;

    public static Optional<FileType> byExtension(@NonNull final String ext) {
        final Predicate<FileType> filter = type -> type.extension.equalsIgnoreCase(ext);
        return LambdaUtil.getFirst(values(), filter);
    }

}
