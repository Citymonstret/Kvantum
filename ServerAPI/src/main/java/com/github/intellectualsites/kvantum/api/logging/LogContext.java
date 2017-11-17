/*
 *
 *    Copyright (C) 2017 IntellectualSites
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.intellectualsites.kvantum.api.logging;

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
