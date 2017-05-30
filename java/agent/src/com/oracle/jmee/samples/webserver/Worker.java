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
 * Class which executes Runnable tasks which are obtained from the specified
 * {@link ThreadSafeQueue}. The queue can be shared amongst several Workers.
 * <br><br>
 * Worker runs in a separate thread and should be started like a thread. After
 * Worker has been started, it takes out a task from the queue to execute it. It
 * takes another task after the previous is completed. If there is no available
 * task, Worker waits for it to appear. Worker stops after it has been
 * interrupted by calling {@link #interrupt()} method.
 *
 * @see ThreadSafeQueue
 */
/* package */ class Worker extends Thread {

    // queue to obtain Runnable tasks
    private final ThreadSafeQueue queue;

    /**
     * Constructor to create Worker with the specified {@link ThreadSafeQueue}.
     *
     * @param queue the queue to obtain Runnable tasks
     *
     * @throws NullPointerException if queue is null
     */
    public Worker(ThreadSafeQueue queue) {
        Objects.requireNonNull(queue, "Queue must not be null!");
        this.queue = queue;
    }

    /**
     * Method to obtain Runnable tasks from the specified
     * {@link ThreadSafeQueue} and execute them. This method finishes when this
     * Worker is interrupted.
     *
     * @see Thread#run()
     */
    // @Override
    public void run() {
        try {
            // while (!this.isInterrupted()) {
            while (this.isAlive()) {
                // obtaining Runnable task;
                // this is a blocking call - it waits for at least one task is available in the queue
                Runnable currentTask = (Runnable)queue.take();
                // executing the task
                currentTask.run();
            }
        } catch (InterruptedException ex) {
            // this happens when Worker is interrupted while waiting for tasks
        }
    }
}
