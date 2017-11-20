package com.github.intellectualsites.kvantum.implementation;

import com.github.intellectualsites.kvantum.api.util.LambdaUtil;
import com.github.intellectualsites.kvantum.api.util.Provider;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * An abstract pool of objects using a {@link LinkedBlockingDeque} backend
 *
 * @param <T> Pooled object type
 */
class AbstractPool<T>
{

    private final LinkedBlockingDeque<T> availableObjects;

    /**
     * Setup the handler with a specified number of instances
     *
     * @param objects  Number of  instances (must be positive)
     * @param supplier Supplier of objects
     */
    AbstractPool(final int objects, final Provider<T> supplier)
    {
        this.availableObjects = new LinkedBlockingDeque<>( objects );
        LambdaUtil.collectionAssign( () -> availableObjects, supplier, objects );
    }

    /**
     * Poll the worker queue until an object is available.
     * Warning: The thread will be locked until a new object is available
     *
     * @return The next available object
     * @throws InterruptedException If the polling of the queue is interrupted
     */
    T getAvailable() throws InterruptedException
    {
        return this.availableObjects.takeFirst();
    }

    T getNullable()
    {
        try
        {
            return getAvailable();
        } catch ( final InterruptedException e )
        {
            return null;
        }
    }

    /**
     * Add an object back into the pool (after it has finished everything)
     *
     * @param t object
     */
    void add(final T t)
    {
        this.availableObjects.add( t );
    }

}
