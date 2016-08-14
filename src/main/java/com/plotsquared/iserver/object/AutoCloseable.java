package com.plotsquared.iserver.object;

import com.plotsquared.iserver.util.Final;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * Our own "smart" implementation of AutoCloseable.
 * Extend this class to act on server shutdown.
 *
 * Uses WeakReferences, so this will not force your objects
 * to remain loaded.
 */
public abstract class AutoCloseable implements java.lang.AutoCloseable
{

    private static final Collection<WeakReference<AutoCloseable>> closeable = new ArrayList<>();

    private boolean closed = false;

    protected AutoCloseable()
    {
        closeable.add( new WeakReference<>( this ) );
    }

    /**
     * Close all loaded AutoCloseables
     */
    public static void closeAll()
    {
        closeable.stream().filter( AutoCloseable::exists ).forEach( close );
    }

    /**
     * This is where you define what your object
     * should do, when the server requests {@link #close()}
     */
    protected abstract void handleClose();

    /**
     * Close the AutoCloseable
     * Can only be called once per instance
     */
    @Final
    final public void close()
    {
        if ( closed )
        {
            return;
        }
        this.handleClose();
        this.closed = true;
    }

    private static Consumer<WeakReference<AutoCloseable>> close = reference -> {
        //noinspection ConstantConditions
        reference.get().close();
    };

    private static <T> boolean exists(final WeakReference<T> reference)
    {
        return reference.get() != null;
    }

}
