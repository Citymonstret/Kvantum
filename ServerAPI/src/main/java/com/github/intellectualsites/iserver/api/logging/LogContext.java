package com.github.intellectualsites.iserver.api.logging;

import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LogContext
{

    private String applicationPrefix;

    private String logPrefix;

    private String thread;

    private String timeStamp;

    private String message;

    public final ImmutableMap<String, String> toMap()
    {
        return ImmutableMap.<String, String>builder()
                .put( "applicationPrefix", applicationPrefix )
                .put( "logPrefix", logPrefix )
                .put( "thread", thread )
                .put( "timeStamp", timeStamp )
                .put( "message", message ).build();
    }

}
