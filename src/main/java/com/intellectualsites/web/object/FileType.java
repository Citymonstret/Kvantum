package com.intellectualsites.web.object;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

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

    public static Optional<FileType> byExtension(String ext) {
        return Arrays.stream(values())
                .filter(fileType -> fileType.extension.equalsIgnoreCase(ext)).findFirst();
    }

}
