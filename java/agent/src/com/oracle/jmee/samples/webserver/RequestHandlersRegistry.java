/*
 * Copyright © 2015, Oracle and/or its affiliates.  All rights reserved. 
 *
 * This software is dual-licensed to you under the MIT License (MIT) and the
 * Universal Permissive License (UPL).  See the LICENSE file in the root directory
 * for license terms.  You may choose either license, or both.
 */
package com.oracle.jmee.samples.webserver;

/**
 * Interface for a registry of request handlers. It allows to store the handlers
 * for them to be retrieved by the request URIs or the context paths. Context
 * path is a starting part of the request by the client URI, e.g. "/books" part
 * of the "/books/1" full request path. Context paths must uniquely identify the
 * handler. Registry may contain a default handler by specifying that it handles
 * the "/" context path which can be used to serve all requests or requests,
 * which will not be handled by other handlers (depending on the
 * implementation). It is up to implementations to decide what to do if null
 * context path is specified for the methods of the interface.
 */
public interface RequestHandlersRegistry {

    /**
     * Adds a request handler for the specified context path. This registry must
     * not contain a mapping for the specified context path, however a single
     * handler may be added for multiple context paths.
     *
     * @param contextPath the starting part of the path of the URI which the
     * provided handler is to serve
     * @param handler the added request handler
     * @throws NullPointerException if the handler is null
     * @throwsHttpServerIllegalStateException if a handler has already been registered
     * for the provided context path
     */
    public void add(String contextPath, RequestHandler handler);

    /**
     * Returns the suitable context for the provided path. Returned value will
     * be a starting substring of the full path if present.
     *
     * @param fullPath the request path to find suitable context path
     * @return the suitable context path or null if it is not found
     */
    public String findSuitableContext(String fullPath);

    /**
     * Gets the suitable request handler for the specified path if present.
     *
     * @param fullPath the request path to find suitable request handler
     * @return the suitable request handler or null if there is no suitable
     * handler
     */
    public RequestHandler get(String fullPath);

    /**
     * Gets the suitable request handler for the specified context path if
     * present.
     *
     * @param contextPath the context path to find suitable request handler
     * @return the suitable request handler or null if there is no suitable
     * handler
     */
    public RequestHandler getByContextPath(String contextPath);

    /**
     * Removes the binding for the specified context path if present. Nothing
     * happens if there is no binding for the specified context path.
     *
     * @param contextPath the context path to remove request handler binding if
     * present
     */
    public void removeContextPath(String contextPath);

    /**
     * Removes all bindings for the specified request handlers. Nothing happens
     * if there is no bindings for the specified handler.
     *
     * @param handler handler to remove bindings
     * @throws NullPointerException if handler is null
     */
    public void remove(RequestHandler handler);

    /**
     * Removes all the binding from the registry.
     */
    public void clear();
}
