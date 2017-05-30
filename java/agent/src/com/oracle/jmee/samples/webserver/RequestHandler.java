/*
 * Copyright © 2015, Oracle and/or its affiliates.  All rights reserved. 
 *
 * This software is dual-licensed to you under the MIT License (MIT) and the
 * Universal Permissive License (UPL).  See the LICENSE file in the root directory
 * for license terms.  You may choose either license, or both.
 */
package com.oracle.jmee.samples.webserver;

/**
 * Interface for a request handler. Each handler is used to handle requests to
 * resources which are identified by URIs starting with the specific parts
 * (which is a context path for a handler), like "/books/",
 * "/departments/employees/", etc. Request handlers are to be registered by
 * calling
 * {@link WebServer#addRequestHandler(java.lang.String, com.oracle.jmee.samples.webserver.RequestHandler)}
 * method of the {@link WebServer} to process HTTP requests to resources which
 * URIs start with the specified context path. The logic of request processing
 * should be located in {@link #handle(com.oracle.jmee.samples.webserver.HttpRequest, java.lang.String, java.lang.String)
 * } method. An implementation must process {@link HttpRequest} and return
 * {@link HttpResponse} for a server to send to the request origin.
 */
public interface RequestHandler {

    /**
     * Handles the provided request. Context path and relative path together
     * make the URI specified in the request. Context path is the path which has
     * been used to register this handler. Relative path describes the location
     * of the resource relatively to the context path. Other information about
     * request can be accessed via the {@link HttpRequest} API. Request handlers
     * should not throw runtime exceptions manually, instead they should form
     * the response describing the issue if possible.
     *
     * @param request HTTP request to handle
     * @param contextPath context path to which this request has been sent
     * @param relativePath path which is relative to the context path to which
     * this request has been sent
     * @return HTTP response to send to the origin of the request to handle
     * @throws RuntimeException if any unpredicted exception occurs during the
     * request processing
     */
    public HttpResponse handle(HttpRequest request, String contextPath, String relativePath);
}
