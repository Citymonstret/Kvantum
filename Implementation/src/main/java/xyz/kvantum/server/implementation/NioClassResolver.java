/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
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
package xyz.kvantum.server.implementation;

import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.channel.ServerChannel;
import lombok.Getter;
import xyz.kvantum.server.api.logging.Logger;

import java.util.Locale;

final class NioClassResolver
{

    @Getter
    private final ClassProvider classProvider;

    NioClassResolver()
    {
        final String osName = System.getProperty( "os.name" );
        if ( osName.toLowerCase( Locale.ENGLISH ).startsWith( "linux" ) )
        {
            Logger.info( "Using EpollClassResolver" );
            this.classProvider = new EpollClassResolver();
        } else
        {
            Logger.info( "Using DefaultClassResolver" );
            this.classProvider = new DefaultClassResolver();
        }
    }

    interface ClassProvider
    {

        MultithreadEventLoopGroup getEventLoopGroup(final int threads);

        Class<? extends ServerChannel> getServerSocketChannelClass();
    }
}
