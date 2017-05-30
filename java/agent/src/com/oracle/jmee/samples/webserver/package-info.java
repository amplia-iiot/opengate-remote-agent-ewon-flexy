/*
 * Copyright © 2015, Oracle and/or its affiliates.  All rights reserved. 
 *
 * This software is dual-licensed to you under the MIT License (MIT) and the
 * Universal Permissive License (UPL).  See the LICENSE file in the root directory
 * for license terms.  You may choose either license, or both.
 */
/**
 * Web Server API.
 * <h1>Overview</h1>
 * This package defines the web server and related classes for usage and
 * extension.<br>
 * <br>
 * This specification extensively uses terms from the HTTP 1.1 RFCs. Please make
 * sure you are familiar with the terms defined in the <a
 * href="https://tools.ietf.org/html/rfc7230">RFC 7230</a>.<br><br>
 *
 * The class to start with is the
 * {@link com.oracle.jmee.samples.webserver.WebServer} which allows to register
 * the application-specific
 * {@link com.oracle.jmee.samples.webserver.RequestHandler request handlers}.
 * Request handlers are used to process
 * {@link com.oracle.jmee.samples.webserver.HttpRequest}s and return
 * {@link com.oracle.jmee.samples.webserver.HttpResponse} instances.<br><br>
 *
 * The web server comes in bundle with one abstract request handler
 * ({@link com.oracle.jmee.samples.webserver.PathRequestHandler}) to handle
 * resources which can be represented as stream (e.g. files) and two extensions
 * of this handler to process static content. Please see
 * {@link com.oracle.jmee.samples.webserver.FileSystemRequestHandler} to handle
 * static content from the file system and
 * {@link com.oracle.jmee.samples.webserver.ResourceRequestHandler} to handle
 * static content from JAR file.
 * {@link com.oracle.jmee.samples.webserver.PathRequestHandler} and its
 * extensions support caching of the resource contents by providing an instance
 * which implements {@link com.oracle.jmee.samples.webserver.WebCache}
 * interface. See {@link com.oracle.jmee.samples.webserver.SimpleWebCache} class
 * for a sample implementation of the cache. <br><br>
 * <h2>Usage example</h2>
 * Starting a simple server is as simple as that:
 * <pre><code>
 *     WebServer webserver = new WebServer();
 *     // Perform server configuration if needed
 *     ...
 *     // Add needed request handlers
 *     ResourceRequestHandler resourceRequestHandler = new ResourceRequestHandler("/resources");
 *     webServer.addRequestHandler("", resourceRequestHandler);
 *
 *     // Start the server
 *     try {
 *         webServer.start();
 *     } catch (IOException e) {
 *         System.out.println("Failed to start server: " + e.getMessage());
 *     }</code></pre>
 */
package com.oracle.jmee.samples.webserver;
