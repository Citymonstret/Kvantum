package com.plotsquared.iserver.core;

import com.plotsquared.iserver.object.Request;
import com.plotsquared.iserver.views.RequestHandler;

import java.lang.ref.WeakReference;
import java.util.*;

public class WorkerProcedure
{

    WorkerProcedure()
    {

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

    private volatile Map<String, Handler> handlers = new LinkedHashMap<>();
    private final List<WeakReference<WorkerProcedureInstance>> instances = new ArrayList<>();

    public synchronized void addProcedure(final String name, final Handler handler)
    {
        this.handlers.put( name, handler );
        this.setChanged();
    }

    public synchronized void addProcedureBefore(final String name, final String before, final Handler handler)
    {
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

    public synchronized void addProcedureAfter(final String name, final String after, final Handler handler)
    {
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

    public class WorkerProcedureInstance
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

        public Collection<StringHandler> getStringHandlers()
        {
            return this.stringHandlers;
        }

        public Collection<ByteHandler> getByteHandlers()
        {
            return this.byteHandlers;
        }
    }

    public final WorkerProcedureInstance getInstance()
    {
        return new WorkerProcedureInstance();
    }

}
