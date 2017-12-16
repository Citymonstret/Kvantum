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
package xyz.kvantum.files;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

@SuppressWarnings({ "WeakerAccess", "unused" })
public final class FileWatcher extends Thread
{

    private final Collection<FileWatchingContext> contexts;
    private boolean shouldStop = false;

    public FileWatcher()
    {
        this.contexts = new CopyOnWriteArrayList<>();
        this.setDaemon( true );
        this.start();
    }

    @SuppressWarnings("unchecked")
    private static WatchEvent<java.nio.file.Path> cast(WatchEvent<?> event)
    {
        return (WatchEvent<java.nio.file.Path>) event;
    }

    private void setShouldStop()
    {
        this.shouldStop = true;
    }

    public StopSignal getStopSignal()
    {
        return this::setShouldStop;
    }

    public FileWatchingContext registerPath(final Path path,
                                            final BiConsumer<Path, WatchEvent.Kind<?>> reaction)
            throws IllegalArgumentException, IOException
    {
        if ( !path.isFolder() )
        {
            throw new IllegalArgumentException( "Supplied path is not a directory" );
        }
        final WatchService watchService = FileSystems.getDefault().newWatchService();
        path.getJavaPath().register( watchService, StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_CREATE );
        final FileWatchingContext context = new FileWatchingContext( path, watchService, reaction );
        this.contexts.add( context );
        return context;
    }

    @Override
    public void run()
    {
        for ( ; ; )
        {
            if ( shouldStop )
            {
                break;
            }
            final Collection<FileWatchingContext> invalid = new ArrayList<>();
            for ( final FileWatchingContext context : contexts )
            {
                final WatchKey key = context.watchService.poll();
                if ( key == null )
                {
                    continue;
                }
                for ( final WatchEvent<?> event : key.pollEvents() )
                {
                    final WatchEvent<java.nio.file.Path> pathEvent = cast( event );
                    final java.nio.file.Path javaPath = pathEvent.context();
                    try
                    {
                        final Path path = context.path.getPath( javaPath.getFileName().toString() );
                        if ( path == null )
                        {
                            new RuntimeException( "Path could not be resolved: '" + javaPath.getFileName().toString()
                                    + "' in path '" + context.path.toString() + "'" ).printStackTrace();
                            continue;
                        }
                        context.reaction.accept( path, event.kind() );
                    } catch ( final Exception ignore )
                    {
                    }
                }
                boolean valid = key.reset();
                if ( !valid )
                {
                    invalid.add( context );
                }
            }
            this.contexts.removeAll( invalid );
        }
    }

    @FunctionalInterface
    public interface StopSignal
    {

        void stop();
    }

    public static final class FileWatchingContext
    {

        private final Path path;
        private final WatchService watchService;
        private BiConsumer<Path, WatchEvent.Kind<?>> reaction;

        private FileWatchingContext(final Path path,
                                    final WatchService service,
                                    final BiConsumer<Path, WatchEvent.Kind<?>> reaction)
                throws IllegalArgumentException
        {
            if ( path == null )
            {
                throw new IllegalArgumentException( "Supplied path was null" );
            }
            if ( service == null )
            {
                throw new IllegalArgumentException( "Supplied watch service was null" );
            }
            this.path = path;
            this.watchService = service;
            if ( reaction == null )
            {
                this.reaction = (file, kind) -> {
                };
            } else
            {
                this.reaction = reaction;
            }
        }

        @Override
        public int hashCode()
        {
            return this.path.hashCode();
        }

        @Override
        public String toString()
        {
            return this.path.toString();
        }

        @Override
        public boolean equals(final Object o)
        {
            return o != null && o instanceof FileWatchingContext &&
                    ( (FileWatchingContext) o ).path.equals( this.path );
        }

        public void setReaction(final BiConsumer<Path, WatchEvent.Kind<?>> reaction)
        {
            if ( reaction == null )
            {
                this.reaction = (file, kind) -> {
                };
            } else
            {
                this.reaction = reaction;
            }
        }
    }

}
