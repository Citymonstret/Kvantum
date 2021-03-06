/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2019 Alexander Söderberg
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
package xyz.kvantum.server.api.response;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.util.TimeUtil;

@Getter @RequiredArgsConstructor @Builder public final class FinalizedResponse {

    private static final String LOG_FORMAT = "%h %l %u [%t] \"%r\" %>s %b";

    private final String address;
    private final AbstractRequest.Authorization authorization;
    private final long timeFinished;
    private final AbstractRequest.Query query;
    private final String status;
    private final int length;

    public String toLogString() {
        return LOG_FORMAT.replace("%h", this.address).replace("%l", "-")
            .replace("%u", authorization != null ? authorization.getUsername() : "-")
            .replace("%t", TimeUtil.getAccessLogTimeStamp(this.timeFinished))
            .replace("%r", query.getMethod().name() + " " + query.getResource() + " HTTP/1.1")
            .replace("%>s", this.status.substring(0, 3)).replace("%b", String.valueOf(this.length));
    }
}
