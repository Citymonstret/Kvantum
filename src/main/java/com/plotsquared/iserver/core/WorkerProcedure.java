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

    public static abstract class StringHandler implements Handler<String>
    {

        @Override
        public abstract String act(RequestHandler requestHandler, Request request, String in);

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
        for ( final WeakReference<WorkerProcedureInstance> instanceReference : instances )
        {
            final WorkerProcedureInstance instance = instanceReference.get();
            if ( instance != null )
            {
                instance.cache = handlers.values();
            }
        }
    }

    public class WorkerProcedureInstance
    {

        private Collection<Handler> cache;

        WorkerProcedureInstance()
        {
            instances.add( new WeakReference<>( this ) );
            cache = handlers.values();
        }

        public Collection<Handler> getHandlers()
        {
            return this.cache;
        }
    }

    public final WorkerProcedureInstance getInstance()
    {
        return new WorkerProcedureInstance();
    }

}
