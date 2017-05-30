/*
 * Copyright © 2015, Oracle and/or its affiliates.  All rights reserved. 
 *
 * This software is dual-licensed to you under the MIT License (MIT) and the
 * Universal Permissive License (UPL).  See the LICENSE file in the root directory
 * for license terms.  You may choose either license, or both.
 */
package com.oracle.jmee.samples.webserver;

// import java.util.ArrayDeque;
// import java.util.Queue;
import java.util.Vector;

/**
 * Class which represents thread-safe FIFO (first-in-first-out) queue. It
 * provides the blocking methods to add and to take elements.
 *
 * @param <E> the type of elements in this queue
 */
public class ThreadSafeQueue {

    private final Vector queue = new Vector();

    /**
     * Method to add the element to the tail of this queue.
     *
     * @param element the element to add
     *
     * @throws NullPointerException if the specified element is
     * <code>null</code>
     */
    public synchronized void add(Object element) {
        queue.addElement(element);
        notifyAll();
    }

    /**
     * Method to retrieve and remove an element from the head of this queue.
     * Waiting for an element to become available, if necessary.
     *
     * @return the head of this queue
     *
     * @throws InterruptedException if interrupted while waiting
     */
    public synchronized Object take() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        Object ret= queue.firstElement();
        if(ret!=null) queue.removeElement(ret);
        return ret;
    }
}
