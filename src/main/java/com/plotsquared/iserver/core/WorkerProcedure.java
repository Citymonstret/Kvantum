/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
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
package com.plotsquared.iserver.core;

import com.plotsquared.iserver.object.Request;
import com.plotsquared.iserver.util.Assert;
import com.plotsquared.iserver.views.RequestHandler;

import java.lang.ref.WeakReference;
import java.util.*;

@SuppressWarnings( "unused" )
public final class WorkerProcedure
{

    private final List<WeakReference<WorkerProcedureInstance>> instances = new ArrayList<>();
    private volatile Map<String, Handler> handlers = new LinkedHashMap<>();

    WorkerProcedure()
    {

    }

    /**
     * Add a procedure, and allow the Worker to use it
     * @param name Procedure name
     * @param handler Procedure Handler
     */
    public synchronized void addProcedure(final String name, final Handler handler)
    {
        this.handlers.put( name, handler );
        this.setChanged();
    }

    /**
     * Add a procedure before another procedure. If there is no procedure with the specified name, it will be
     * added to the last position of the queue
     * @param name Procedure Name
     * @param before Name of procedure that the current procedure should be placed before
     * @param handler Procedure handler
     */
    public synchronized void addProcedureBefore(final String name, final String before, final Handler handler)
    {
        Assert.notEmpty( name );
        Assert.notEmpty( before );
        Assert.notNull( handler );

        final Map<String, Handler> temporaryMap = new HashMap<>( );
        for ( final Map.Entry<String, Handler> entry : handlers.entrySet() )
        {
            if ( entry.getKey().equalsIgnoreCase( before ) )
            {
                temporaryMap.put( name, handler );
            }
            temporaryMap.put( entry.getKey(), entry.getValue() );
        }
        if ( !temporaryMap.containsKey( name ) )
        {
            temporaryMap.put( name, handler );
        }
        this.handlers = temporaryMap;
        this.setChanged();
    }

    /**
     * Add a procedure after another procedure. If there is no procedure with the specified name, it will be
     * added to the last position of the queue
     * @param name Procedure Name
     * @param after Name of procedure that the current procedure should be placed behind
     * @param handler Procedure handler
     */
    public synchronized void addProcedureAfter(final String name, final String after, final Handler handler)
    {
        Assert.notEmpty( name );
        Assert.notEmpty( after );
        Assert.notNull( handler );

        final Map<String, Handler> temporaryMap = new HashMap<>( );
        for ( final Map.Entry<String, Handler> entry : handlers.entrySet() )
        {
            temporaryMap.put( entry.getKey(), entry.getValue() );
            if ( entry.getKey().equalsIgnoreCase( after ) )
            {
                temporaryMap.put( name, handler );
            }
        }
        if ( !temporaryMap.containsKey( name ) )
        {
            temporaryMap.put( name, handler );
        }
        this.handlers = temporaryMap;
        this.setChanged();
    }

    private synchronized void setChanged()
    {
        final Collection<StringHandler> stringHandlers = new ArrayList<>();
        final Collection<ByteHandler> byteHandlers = new ArrayList<>();

        for ( final Handler handler : handlers.values() )
        {
            if ( handler instanceof StringHandler )
            {
                stringHandlers.add( ( StringHandler ) handler );
            } else
            {
                byteHandlers.add( ( ByteHandler ) handler );
            }
        }

        for ( final WeakReference<WorkerProcedureInstance> instanceReference : instances )
        {
            final WorkerProcedureInstance instance = instanceReference.get();
            if ( instance != null )
            {
                instance.byteHandlers = byteHandlers;
                instance.stringHandlers = stringHandlers;
            }
        }
    }

    /**
     * @return A new WorkerProcedureInstance
     */
    public final WorkerProcedureInstance getInstance()
    {
        return new WorkerProcedureInstance();
    }

    interface Handler<T>
    {

        T act(RequestHandler requestHandler, Request request, T in);

        Class<T> getType();

    }

    public static abstract class ByteHandler implements Handler<Byte[]>
    {

        @Override
        public Class<Byte[]> getType()
        {
            return Byte[].class;
        }
    }

    public static abstract class StringHandler implements Handler<String>
    {

        @Override
        public final Class<String> getType()
        {
            return String.class;
        }

    }

    /**
     * An instance containing the handlers from the WorkerProcedure, that
     * are split into Byte & String Handlers (to make them easier to use in the worker)
     *
     * The instance gets updated automatically when the WorkerProcedure is updated
     */
    class WorkerProcedureInstance
    {

        private volatile Collection<StringHandler> stringHandlers = new ArrayList<>();
        private volatile Collection<ByteHandler> byteHandlers = new ArrayList<>();

        WorkerProcedureInstance()
        {
            instances.add( new WeakReference<>( this ) );
            for ( final Handler handler : handlers.values() )
            {
                if ( handler instanceof StringHandler )
                {
                    stringHandlers.add( ( StringHandler ) handler );
                } else
                {
                    byteHandlers.add( ( ByteHandler ) handler );
                }
            }
        }

        Collection<StringHandler> getStringHandlers()
        {
            return this.stringHandlers;
        }

        Collection<ByteHandler> getByteHandlers()
        {
            return this.byteHandlers;
        }
    }

}
