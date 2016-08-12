package com.plotsquared.iserver.object;

import java.util.ArrayList;
import java.util.Collection;

public abstract class AutoCloseable implements java.lang.AutoCloseable
{

    private static Collection<AutoCloseable> closeable = new ArrayList<>();

    protected AutoCloseable()
    {
        closeable.add( this );
    }

    public static void closeAll()
    {
        closeable.forEach( AutoCloseable::handleClose );
    }

    protected abstract void handleClose();

    public void close()
    {
        this.handleClose();
        closeable.remove( this );
    }

}
