/*
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2017 IntellectualSites
 *
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.github.intellectualsites.iserver.api.logging;

import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
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
