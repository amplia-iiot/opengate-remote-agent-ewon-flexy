/*
 * Copyright © 2015, Oracle and/or its affiliates.  All rights reserved. 
 *
 * This software is dual-licensed to you under the MIT License (MIT) and the
 * Universal Permissive License (UPL).  See the LICENSE file in the root directory
 * for license terms.  You may choose either license, or both.
 */
package com.oracle.jmee.samples.webserver;

import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.ServerSocketConnection;
import javax.microedition.io.SocketConnection;

/**
 * Class which represents a web server. This server implementation uses threads
 * from a pool to serve the incoming connections.<br><br>
 *
 * After creation request handlers should be registered in a server to handle
 * requests. This can be done using
 * {@link #addRequestHandler(java.lang.String, com.oracle.jmee.samples.webserver.RequestHandler)}
 * method by specifying the handler and associated with it context path. Context
 * path is a starting part of the URI request path that is used to locate the
 * specific request handler to serve the request. It is possible to add handlers
 * after the server has been started but it is advised to have all handlers
 * added in advance in order not to miss any client requests.<br><br>
 *
 * It is also possible to configure the web server by setting persistent
 * connections handling policy and a size of the transfer buffer. Since web
 * server supports both HTTP 1.0 and 1.1 versions, it supports persistent
 * connections if the client HTTP request suggests their usage. By default
 * persistent connections support is enabled. It can be changed using the
 * {@link #setPersistConnections(boolean)} method. The server is written to use
 * the optional Java ME feature of socket timeout to prevent web server from
 * hanging, but it may be not implemented on the specific platform. These
 * parameters may be set at any time of the web server work, but it is advised
 * to set them up prior to starting for a consistent user-experience of
 * clients<br><br>
 *
 * After that web server may be started using {@link #start()} method. Use
 * {@link #stop()} method in order to stop the server. It is possible to restart
 * the server after stopping by calling the same {@link #start()}
 * method.<br><br>
 *
 * This class differs from the one which is used in the full version of the
 * demo: support of multiple working threads was removed.
 */
public class WebServer {

    // Web server name for the "Server" response header
    private static final String SERVER_NAME = "Java ME Embedded Web Server/1.0";

    // Socket timeout in milliseconds
    private static final int DEFAULT_READ_WRITE_TIMEOUT_MS = 10000;

    // Port on which the server listens for incoming connections
    private int listeningPort;

    // Buffer size which is used when the buffer size is not specified
    // explicitly
    private static final int DEFAULT_TRANSFER_BUFFER_SIZE = 1024;

    // Used buffer size to perform reading of the request
    private volatile int transferBufferSize = DEFAULT_TRANSFER_BUFFER_SIZE;

    // Server socket to listen for incoming connections
    private ServerSocketConnection serverSocket;

    // Flag to check whether the server is running or not
    private volatile boolean shouldRun;

    // Flag which determines whether the connections should be persisted
    private volatile boolean persistConnections;

    // Thread which is used to accept clients
    private Thread thread;

    // Thread pool to execute connection handling tasks
    private SingleWorkerPoolExecutor threadPool;

    // Registry of the request handlers
    private final RequestHandlersRegistry requestHandlersRegistry;

    // Object which is used to synchronize access to web server state
    private final Object lockObject = new Object();
    
    public ServerSocketConnection getServerSocket() 
    {
        return serverSocket;
    }

    /**
     * Creates a new web server instance with the provided number of threads
     * which is listening on the specified port.
     *
     * @param listeningPort the port to listen for incoming connections
     * @throws IllegalArgumentException if the listening port is less than 0 or
     * more than 65535
     */
    public WebServer(int listeningPort) {
        System.out.println("WebServer(int listeningPort)-->1");
        if (listeningPort < 0 || listeningPort > 65535) {
            throw new IllegalArgumentException("Listening port must be in [0,65535] range");
        }
        this.listeningPort = listeningPort;
        requestHandlersRegistry = new SimpleRequestHandlersRegistry();
        // requestHandlersRegistry = null;
        System.out.println("WebServer(int listeningPort)-->2");
        System.out.flush();
    }

    /**
     * Starts the server. The server starts to listen for incoming connections
     * and handle requests using the registered request handlers. It is advised
     * that other server parameters are setup and needed request handlers are
     * added prior to the start of the server. Calling a start when the server
     * is started causes an exception to be thrown. Call {@link #stop()} method
     * when server should be stopped.
     *
     * @throws IOException if an I/O exception occurs during the opening of
     * server socket
     * @throwsHttpServerIllegalStateException if web server has already been started
     */
    public void start() throws Exception {
        synchronized (lockObject) {
            if (shouldRun) {
                // throw newHttpServerIllegalStateException("Web server has already been started");
                throw new Exception("Web server has already been started");
            }

            serverSocket = (ServerSocketConnection) Connector.open("socket://:" + listeningPort);

            // Initializing thread pool with working threads number
            threadPool = new SingleWorkerPoolExecutor();
            shouldRun = true;

            // Starting a server thread
            thread = new Thread(createServingRunnable());
            thread.start();
        }
    }

    private Runnable createServingRunnable() {
        return new Runnable() {

            // @Override
            public void run() {
                System.out.println("Started accepting clients...");
                while (shouldRun) {
                    try {
                        SocketConnection clientConnection = (SocketConnection) serverSocket.acceptAndOpen();

                        // Note that some implementations may not support
                        // timeout connection option and simply do nothing when
                        // it is being set. In such case server will be limited
                        // to the specific number of working threads and if all
                        // of them will be blocked by persistent connections
                        // then no further clients will be served until any
                        // thread becomes available.
                        // OJO Javier clientConnection.setSocketOption(SocketConnection.TIMEOUT, DEFAULT_READ_WRITE_TIMEOUT_MS);

                        System.out.println("New client has connected: " + clientConnection.getAddress() + ":" + clientConnection.getPort());

                        synchronized (lockObject) {
                            // Creating an initializing handler which will serve
                            // the opened connection
                            SimpleHttpConnectionHandler simpleHttpConnectionHandler = new SimpleHttpConnectionHandler(clientConnection, requestHandlersRegistry, SERVER_NAME, transferBufferSize);
                            simpleHttpConnectionHandler.setShouldPersistConnection(persistConnections);

                            // Adding a handler to the queue for execution in
                            // the thread pool
                            threadPool.execute(simpleHttpConnectionHandler);
                        }
                    } catch (Exception e) {
                        System.out.println("Error while accepting clients: " + e.getMessage());
                    }
                }
            }
        };
    }

    /**
     * Returns true if persistent connections support is enabled, otherwise
     * false is returned.
     *
     * @return true if connections will be persisted if HTTP request suggests it
     */
    public boolean areConnectionsPersisted() {
        return persistConnections;
    }

    /**
     * Enables or disables the persistent connections support. If enabled,
     * server will not close client connections if their requests suggest
     * continuing using the same connection for other requests.
     *
     * @param persistConnections true to enable persistent connections support;
     * false to disable support of persistent connections
     */
    public void setPersistConnections(boolean persistConnections) {
        this.persistConnections = persistConnections;
    }

    /**
     * Returns the currently used size of the transfer buffer in bytes.
     *
     * @return the size of the buffer to read requests
     */
    public int getTransferBufferSize() {
        return transferBufferSize;
    }

    /**
     * Sets the size of the transfer buffer which is used to read requests. It
     * must be big enough for an HTTP request headers part to fit in.
     *
     * @param transferBufferSize size of the buffer to read data into
     */
    public void setTransferBufferSize(int transferBufferSize) {
        this.transferBufferSize = transferBufferSize;
    }

    /**
     * Adds the request handler to serve HTTP requests which address the
     * resources by the specified context path. There may be only one handler
     * for the specific context path. If there is already a handler which serves
     * the provided context path, an exception is thrown. However a handler may
     * be used to serve multiple context paths. It may be needed to specify the
     * root handler, which serves all request in case there is no others
     * suitable handlers. It can be specified by one the context path parameter
     * value: "/", empty string or null. Server tries to find a best matching
     * context path by searching for the most specific one for the request, i.e.
     * if there are two context paths registered "/" and "/books", then for the
     * request path "/books/5" the latter option will be used to obtain the
     * handler.
     *
     * @param contextPath the context path which the handler is to serve
     * @param requestHandler the handler to serve the requests which match the
     * specified context path
     * @throws IllegalArgumentException if a handler has already been registered
     * @throws NullPointerException if the request handler is null
     */
    public void addRequestHandler(String contextPath, RequestHandler requestHandler) {
        synchronized (requestHandlersRegistry) {
            requestHandlersRegistry.add(contextPath, requestHandler);
        }
    }

    /**
     * Removes the request handler which serves the specified context path.
     * Other mappings if any for the deleted handler are retained. If there is
     * no handler registered, then nothing will happen.
     *
     * @param contextPath the context path to remove the request handler
     */
    public void removeRequestHandler(String contextPath) {
        synchronized (requestHandlersRegistry) {
            requestHandlersRegistry.removeContextPath(contextPath);
        }
    }

    /**
     * Removes the handler. If the request handler is registered to serve
     * multiple context paths, then all bindings are removed. If there are no
     * bindings for the specified request handler, then nothing will happen.
     *
     * @param requestHandler the request handler to remove all bindings to
     */
    public void removeRequestHandler(RequestHandler requestHandler) {
        synchronized (requestHandlersRegistry) {
            requestHandlersRegistry.remove(requestHandler);
        }
    }

    /**
     * Removes all context path to handler bindings.
     */
    public void clearRequestHandlers() {
        synchronized (requestHandlersRegistry) {
            requestHandlersRegistry.clear();
        }
    }

    /**
     * Stops the server. Server stops to accept clients and waits for the
     * currently served requests to be handled. If the server is already
     * stopped, nothing will happen. After the server has been stopped, it is
     * possible to restart it using {@link #start()} method.
     *
     * @throws IOException if any I/O error occurs while stopping the server
     * from accepting clients
     * @throws InterruptedException if method was interrupted while waiting for
     * the processed requests to be finished
     */
    public void stop() throws IOException, InterruptedException {
        synchronized (lockObject) {
            if (!shouldRun) {
                return;
            }
            shouldRun = false;
            serverSocket.close();
            threadPool.stop();

            thread.join();
        }
    }
}
