package com.github.intellectualsites.kvantum.api.request.post;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum EntityType
{
    FORM_URLENCODED( "application/x-www-form-urlencoded" ),
    FORM_MULTIPART( "multipart" ),
    JSON( "application/json" ),
    NONE( "??" );

    @Getter
    private final String contentType;
}
