/*
 *
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
package com.github.intellectualsites.kvantum.implementation;

import com.github.intellectualsites.kvantum.api.config.CoreConfig;
import com.github.intellectualsites.kvantum.api.config.Message;
import com.github.intellectualsites.kvantum.api.util.LambdaUtil;
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
