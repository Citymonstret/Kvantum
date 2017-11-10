/*
 * Kvantum is a web server, written entirely in the Java language.
 * Copyright (C) 2017 IntellectualSites
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
package com.github.intellectualsites.kvantum.implementation;

import com.github.intellectualsites.kvantum.api.config.Message;
import com.github.intellectualsites.kvantum.api.util.LambdaUtil;
import com.github.intellectualsites.kvantum.api.config.CoreConfig;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * A pool of {@link Worker workers} to be used in {@link SocketHandler}
 * using a {@link LinkedBlockingDeque} implementation.
 * <p>
 * {@link Worker workers}
 * add themselves back to the pool after they have completed the socket handling, and
 * thus the WorkerPool has a fixed size ({@link CoreConfig#workers})
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class WorkerPool
{

    private static LinkedBlockingDeque<Worker> availableWorkers;

    /**
     * Setup the handler with a specified number of worker instances
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
     * @return The next available worker
     * @throws InterruptedException If the polling of the queue is interrupted
     */
    static Worker getAvailableWorker() throws InterruptedException
    {
        return availableWorkers.takeFirst();
    }

    /**
     * Add a worker back into the pool (after it has finished everything)
     * @param worker Worker
     */
    static void addWorker(final Worker worker)
    {
        availableWorkers.add( worker );
    }

}
