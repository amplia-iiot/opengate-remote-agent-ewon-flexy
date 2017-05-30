/*
 * Copyright © 2015, Oracle and/or its affiliates.  All rights reserved. 
 *
 * This software is dual-licensed to you under the MIT License (MIT) and the
 * Universal Permissive License (UPL).  See the LICENSE file in the root directory
 * for license terms.  You may choose either license, or both.
 */
package com.oracle.jmee.samples.webserver;

import amplia.util.Objects;

/**
 * Class which executes Runnable tasks using single worker thread. This
 * executor starts to process tasks after it is created and finishes after
 * {@link #stop()} method has been called. This executor cannot be restarted.
 */
/* package */ public class SingleWorkerPoolExecutor {

    // queue to hold tasks passed for execution
    private final ThreadSafeQueue taskQueue = new ThreadSafeQueue(); // Runnable

    // worker to execute Runnable objects
    private final Worker worker;

    // true if this executor has been stopped
    private boolean isStopped = false;

    /**
     * Constructor to create WorkerPoolExecutor.
     */
    public SingleWorkerPoolExecutor() {
        this.worker = new Worker(taskQueue);

        // Nothing bad as worker is blocked by an emptiness of a task
        // queue. Adding to this task queue will be possible after worker pool
        // finishes construction
        worker.start();
    }

    /**
     * Method to execute a given Runnable task asynchronously.
     *
     * @param task the task to execute
     *
     * @throws NullPointerException if the task is null
     * @throwsHttpServerIllegalStateException if this executor has already been stopped
     */
    public void execute(Runnable task) {
        Objects.requireNonNull(task, "Task must not be null");
        if (isStopped) {
            // throw new IllegalStateException("WorkerPoolExecutor has been stopped!");
            System.out.println("WorkerPoolExecutor has been stopped!");
        }
        taskQueue.add(task);
    }

    /**
     * Method to stop this SingleWorkerPoolExecutor. All subsequent invocations
     * of {@link #execute(java.lang.Runnable)} method throw
     *HttpServerIllegalStateException after stop method has been invoked. Calling this
     * method after executor has already been stopped has no effect.
     */
    public void stop() {
        if (isStopped) {
            return;
        }
        // interrupt a Worker to stop it

        worker.interrupt();

        try {
            worker.join();
        } catch (InterruptedException ex) {
            // Do nothing
        }
        isStopped = true;
    }
}
