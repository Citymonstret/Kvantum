/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
 * <p>
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.plotsquared.iserver.api.util;

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
    private static Consumer<WeakReference<AutoCloseable>> close = reference ->
    {
        //noinspection ConstantConditions
        reference.get().close();
    };

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

    private static <T> boolean exists(final WeakReference<T> reference)
    {
        return reference.get() != null;
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

}
