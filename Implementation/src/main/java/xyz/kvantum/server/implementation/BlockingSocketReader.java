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
package xyz.kvantum.server.implementation;

import lombok.Getter;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.socket.SocketContext;

import java.io.BufferedInputStream;

final class BlockingSocketReader extends SocketReader
{

    @Getter
    private final BufferedInputStream inputStream;

    BlockingSocketReader(final SocketContext socketContext, final RequestReader requestReader) throws Exception
    {
        super( socketContext, requestReader );
        this.inputStream = new BufferedInputStream( socketContext.getSocket().getInputStream(), CoreConfig.Buffer.in );
    }

    @Override
    void tick() throws Exception
    {
        this.requestReader.readByte( inputStream.read() );
    }
}
