package com.github.intellectualsites.kvantum.api.session;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SessionLoad
{

    private final int sessionId;
    private final String sessionKey;
    private final long lastActive;

}
