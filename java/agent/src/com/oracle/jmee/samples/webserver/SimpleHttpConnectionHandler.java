/*
 * Copyright © 2015, Oracle and/or its affiliates.  All rights reserved. 
 *
 * This software is dual-licensed to you under the MIT License (MIT) and the
 * Universal Permissive License (UPL).  See the LICENSE file in the root directory
 * for license terms.  You may choose either license, or both.
 */
package com.oracle.jmee.samples.webserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import amplia.util.Objects;
import javax.microedition.io.SocketConnection;

/**
 * Class which purpose is to handle a web server client connection according to
 * the HTTP 1.1. This class performs HTTP request receiving, handling it and
 * sending of the HTTP response in the {@link #run()} method, which is intended
 * to be executed by a {@link Thread}.<br><br>
 *
 * Instances of this class may be configured with a receiving buffer size and a
 * persistent connection handling policy. It is allowed to set whether it is
 * needed to persist connections if protocol suggests it or not by calling
 * {@link #setShouldPersistConnection(boolean)}. HTTP 1.1 persistent connections
 * are defined in the <a
 * href="https://tools.ietf.org/html/rfc7230#section-6.3">Persistence paragraph
 * of the RFC 7230</a>. By default connections are persisted if HTTP request
 * suggests it.<br><br>
 *
 * This class uses the provided handlers registry to locate the appropriate
 * handler.
 */
/* package */ class SimpleHttpConnectionHandler implements Runnable {

    // New line characters bytes
    private static final byte[] NEW_LINE_BYTES = "\r\n".getBytes();

    // Size of the buffer to read data from the client connection
    private int bufferSize;

    // Socket connection with the client
    private final SocketConnection clientConnection;

    // Registry of handlers to located the needed one
    private final RequestHandlersRegistry requestHandlersRegistry;

    // Server-specific HTTP response headers to append to all responses, may be
    // null
    private final String serverName;

    // Flag which determines whether it connection will be persisted if HTTP
    // request suggests it
    private boolean persistConnectionIfSuggested = true;

    /**
     * Creates a new SimpleHttpConnectionHandler to handle the client at the
     * specified connection. Connection handler will use the provided registry
     * to locate the suitable request handler to serve the HTTP request. The
     * provided server headers will be appended to each HTTP response. The
     * specified buffer size is used to allocate buffers. Buffer size must be
     * big enough to fit all HTTP request headers. It is not possible to
     * reinitialize the handler, so it becomes useless after it has performed
     * client connection handling in the {@link #run()} method.
     *
     * @param clientConnection connection to read the request from
     * @param handlersRegistry registry to locate the suitable request handler
     * @param serverName name of the server
     * @param bufferSize size of the buffer to read data from the client
     * @throws NullPointerException if client connection or handlers registry is
     * null
     * @throws IllegalArgumentException if buffer size is zero or negative
     */
    public SimpleHttpConnectionHandler(SocketConnection clientConnection, RequestHandlersRegistry handlersRegistry, String serverName, int bufferSize) {
        Objects.requireNonNull(clientConnection, "Client connection must not be null");
        Objects.requireNonNull(handlersRegistry, "Handlers registry must not be null");
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("Buffer size must be bigger than 0");
        }

        this.clientConnection = clientConnection;
        this.requestHandlersRegistry = handlersRegistry;
        this.serverName = serverName;
        this.bufferSize = bufferSize;
    }

    /**
     * Method which performs connection handling. It is assumed to be run by a
     * dedicated {@link Thread}. It receives the HTTP request from the client,
     * searches for the suitable request handler, in case of success it passes
     * the request to the handler and retrieves the HTTP response, which in turn
     * is sent subsequently. After that depending on the persistence connection
     * handling policy and the information contained in the request, it is
     * decided whether connection should be closed or not. If the request
     * handler is not found a "404 Not Found" HTTP response is sent.
     */
    // @Override
    public void run() {
        boolean persistConnection = true;
        String clientAddress = null;

        try 
        {
            InputStream clientInput = clientConnection.openInputStream();
            OutputStream clientOutput = clientConnection.openOutputStream();
            
            while (persistConnection) {
                clientAddress = clientConnection.getAddress() + ":" + clientConnection.getPort();
                HttpRequest request = null;
                System.out.println("Started parsing new request");
                HttpResponse response;
                try {
                    request = HttpRequest.parseRequest(clientAddress, clientInput, bufferSize);
                } catch (IllegalArgumentException e) {
                    // IllegalArgumentException is treated as a bad request
                    System.out.println("Error while serving client: " + e.getMessage());
                    response = HttpResponse.badRequest();
                    response.addHeader("Connection", "close");
                    // Errors are sent using HTTP/1.0 because it is unknown here
                    // which version the request has used
                    sendResponse(clientOutput, "HTTP/1.0", response, request);
                    return;
                } catch (RuntimeException e) {
                    System.out.println("Error while serving client: " + e.getMessage());
                    response = HttpResponse.internalServerError();
                    response.addHeader("Connection", "close");
                    sendResponse(clientOutput, "HTTP/1.0", response, request);
                    return;
                } catch (IOException e) {
                    throw e;
                }

                // Checking wheter it is needed to persist the client connection
                persistConnection = persistConnectionIfSuggested && request.shouldPersistConnection();

                String protocol = request.getHttpVersion();
                String path = request.getRequestPath();

                String contextPath;
                RequestHandler requestHandler;

                // Obtaining a context path and a handler for this request
                synchronized (requestHandlersRegistry) {
                    contextPath = requestHandlersRegistry.findSuitableContext(path);
                    requestHandler = requestHandlersRegistry.getByContextPath(contextPath);
                }

                if (requestHandler != null) {
                    System.out.println("Found request handler for context: " + contextPath);

                    String relativePath = path.substring(contextPath.length());
                    System.out.println("Relative path is: " + relativePath);

                    try {
                        response = requestHandler.handle(request, contextPath, relativePath);
                        
                        System.out.println("Response-->"+response.toString());
                         
                    } catch (RuntimeException e) {
                        System.out.println("Error while serving client: " + e.getMessage());
                        response = HttpResponse.internalServerError();
                    }

                } else {
                    System.out.println("No suitable request handler found for " + path);
                    response = HttpResponse.notFound();
                }
                if (!persistConnection) {
                    // Appending a header to signal that user agent must close
                    // connection
                    response.addHeader("Connection", "close");
                }

                // Sending the response
                sendResponse(clientOutput, protocol, response, request);
                System.out.println("Response to " + clientAddress + " has been sent");
            }

        } catch (IOException e) {
            System.out.println("Error while handling client: " + e.getMessage());
        }

        try {
            clientConnection.close();
            if (clientAddress != null) {
                System.out.println("Connection with client " + clientAddress + " has been closed");
            }
        } catch (IOException e) {
            System.out.println("Failed to close connection: " + e.getMessage());
        }
    }

    /**
     * Returns true if this handler will persist connection with the client if
     * HTTP request suggests it.
     *
     * @return true if client connection is persisted if suggested by HTTP
     * request
     */
    public boolean isShouldPersistConnection() {
        return persistConnectionIfSuggested;
    }

    /**
     * Modifies the persistent connections handling policy.
     *
     * @param shouldPersistConnection specify true value to have the connection
     * persisted if the handled HTTP request suggests it. Specifying false value
     * causes all connections to be closed after a single request handling
     */
    public void setShouldPersistConnection(boolean shouldPersistConnection) {
        this.persistConnectionIfSuggested = shouldPersistConnection;
    }

    private void sendResponse(final OutputStream clientOutput, String protocol, HttpResponse response, HttpRequest request) throws IOException {
        // Sending status line
        clientOutput.write((protocol + " " + response.getStatusCode() + " " + response.getStatusReason() + "\r\n").getBytes());

        // Sending the headers part
        clientOutput.write(response.getHeadersAsString().getBytes());
        clientOutput.write("Server: ".getBytes());
        clientOutput.write(serverName.getBytes());
        clientOutput.write(NEW_LINE_BYTES);

        clientOutput.write(NEW_LINE_BYTES);

        // Sending the headers first
        clientOutput.flush();

        if (request != null && request.getRequestMethod() != HttpRequest.HEAD_REQUEST_METHOD) {
            // Sending the body if it is present. Also body must not be sent if
            // the request method is HEAD
            InputStream body = response.getBody();
            if (body != null) {
                // Using the size of the buffer which has been set up during
                // the connection handler construction
                byte[] buffer = new byte[bufferSize];
                int readBytes;
                while ((readBytes = body.read(buffer)) != -1) {
                    clientOutput.write(buffer, 0, readBytes);
                }
                clientOutput.flush();
            }
        }
    }
}
