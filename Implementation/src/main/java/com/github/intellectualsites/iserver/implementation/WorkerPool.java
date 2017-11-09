package com.github.intellectualsites.iserver.implementation;

import com.github.intellectualsites.iserver.api.config.Message;
import com.github.intellectualsites.iserver.api.util.LambdaUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.LinkedBlockingDeque;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class WorkerPool
{

    private static LinkedBlockingDeque<Worker> availableWorkers;

    /**
     * Setup the handler with a specified number of worker instances
     *
     * @param numberOfWorkers Number of worker instances (must be positive)
     */
    static void setupPool(final int numberOfWorkers)
    {
        availableWorkers = new LinkedBlockingDeque<>( numberOfWorkers );
        LambdaUtil.collectionAssign( () -> availableWorkers, Worker::new, numberOfWorkers );
        Message.WORKER_AVAILABLE.log( availableWorkers.size() );
    }

    /**
     * Poll the worker queue until a worker is available.
     * Warning: The thread will be locked until a new worker is available
     *
     * @return The next available worker
     */
    static Worker getAvailableWorker() throws InterruptedException
    {
        return availableWorkers.takeFirst();
    }

    static void addWorker(final Worker worker)
    {
        availableWorkers.add( worker );
    }

}
