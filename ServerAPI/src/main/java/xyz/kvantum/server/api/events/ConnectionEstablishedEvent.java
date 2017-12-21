/*
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
package xyz.kvantum.server.api.events;

import lombok.Getter;
import lombok.Setter;
import pw.stamina.causam.event.Cancellable;

public final class ConnectionEstablishedEvent extends Event implements Cancellable
{

    @Getter
    private final String ip;
    @Getter
    @Setter
    private boolean cancelled;

    public ConnectionEstablishedEvent(final String ip)
    {
        super( "connectionEstablishedEvent" );
        this.ip = ip;
    }

    @Override
    public void cancel()
    {
        this.setCancelled( true );
    }
}
