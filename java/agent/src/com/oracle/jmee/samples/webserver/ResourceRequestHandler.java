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
import amplia.util.Objects;
import amplia.util.StringTokenizer;

/**
 * Request handler which allows accessing the resources inside the JAR. An
 * absolute root path should be provided to this implementation to locate the
 * streams and lengths of the files. Resource absolute paths must start with "/"
 * character. For more information about the request handling process see
 * {@link PathRequestHandler} documentation.<br><br>
 *
 * Note that this implementation fully reads the resource in order to determine
 * its length.
 */
public class ResourceRequestHandler extends PathRequestHandler {

    // Delimiter of the parts of a resource path
    private static final String RESOURCE_PATH_DELIMETER = "/";

    // Exception messages
    private static final String RESOURCE_FILE_WAS_NOT_FOUND_EXCEPTION_MESSAGE = "Resource file was not found";
    private static final String ILLEGAL_PATH_EXCEPTION_MESSAGE = "Illegal path";
    private static final String READING_BUFFER_SIZE_MUST_BE_POSITIVE_EXCEPTION_MESSAGE = "Reading buffer size must be positive";
    private static final String PATH_MUST_NOT_BE_NULL_EXCEPTION_MESSAGE = "Path must not be null";

    // Size of the buffer to read the data from the resource. Reading is needed
    // to determine resource size because there is no way to obtain its length. 
    // Buffer is needed to read resource data faster than by one byte. If memory
    // consumption is an issue this can be set to the minimum value of the 1.
    private volatile int readingBufferSize;

    // Default size of the reading buffer
    private static final int DEFAULT_BUFFER_SIZE = 1024;

    // Root path to resolve relative paths inside the JAR
    private String rootPath;

    /**
     * Creates a new instance of {@link ResourceRequestHandler} with the
     * specified root path and the default reading buffer size:
     * {@value #DEFAULT_BUFFER_SIZE}. See {@link #ResourceRequestHandler(java.lang.String, int)
     * } for more information.
     *
     * @param rootPath absolute root path inside the JAR to resolve relative
     * paths
     * @throws NullPointerException if rootPath is null
     * @throws IllegalArgumentException if rootPath does not start with
     * {@value #RESOURCE_PATH_DELIMETER}
     */
    public ResourceRequestHandler(String rootPath) {
        this(rootPath, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Creates a new instance of {@link ResourceRequestHandler} with the
     * specified root path and reading buffer size. Root path must be absolute,
     * i.e. start with {@value #RESOURCE_PATH_DELIMETER}. The least possible
     * reading buffer size is 1, using this value may cause slow performance of
     * {@link #getLength(java.lang.String)}, however will decrease memory
     * consumption.
     *
     * @param rootPath absolute root path inside the JAR to resolve relative
     * paths
     * @param readingBufferSize size of the buffer to read data to calculate the
     * length of the requested resources
     * @throws NullPointerException if rootPath is null
     * @throws IllegalArgumentException if readingBufferSize is not positive or
     * if root path does not start with {@value #RESOURCE_PATH_DELIMETER}
     */
    public ResourceRequestHandler(String rootPath, int readingBufferSize) {
        Objects.requireNonNull(rootPath, "Root path must not be null");
        if (!rootPath.startsWith(RESOURCE_PATH_DELIMETER)) {
            throw new IllegalArgumentException("Root path must start with \"" + RESOURCE_PATH_DELIMETER + "\"");
        }
        if (readingBufferSize <= 0) {
            throw new IllegalArgumentException(READING_BUFFER_SIZE_MUST_BE_POSITIVE_EXCEPTION_MESSAGE);
        }
        
        this.rootPath = rootPath;
        this.readingBufferSize = readingBufferSize;
    }

    /**
     * Returns the root path that is used to resolve the relative resource
     * paths.
     *
     * @return absolute root path inside the JAR
     */
    public String getRootPath() {
        return rootPath;
    }

    /**
     * Returns the size of the buffer to read data into during length measuring.
     *
     * @return size of the buffer to read data
     */
    public int getReadingBufferSize() {
        return readingBufferSize;
    }

    /**
     * Sets the size of the buffer to read data into during length measuring.
     *
     * @param readingBufferSize size of the buffer to read data, must be
     * positive
     * @throws IllegalArgumentException if readingBufferSize is not positive
     */
    public void setReadingBufferSize(int readingBufferSize) {
        if (readingBufferSize <= 0) {
            throw new IllegalArgumentException(READING_BUFFER_SIZE_MUST_BE_POSITIVE_EXCEPTION_MESSAGE);
        }
        this.readingBufferSize = readingBufferSize;
    }

    /**
     * Returns the length in bytes of the resource located by the specified
     * path. The specified during the construction root path is used to resolve
     * relative paths.
     *
     * @param path {@inheritDoc}
     * @return {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @throws LengthIsUnavailableException {@inheritDoc}
     */
    // @Override
    protected int getLength(String path) throws LengthIsUnavailableException {
        Objects.requireNonNull(path, PATH_MUST_NOT_BE_NULL_EXCEPTION_MESSAGE);
        int length = 0;
        // Resolving path: making relative path an absolute using the specified
        // during object construction root path
        String resourcePath = preparePath(path);
        if (resourcePath == null) {
            throw new LengthIsUnavailableException(ILLEGAL_PATH_EXCEPTION_MESSAGE);
        }

        // Opening a stream
        InputStream inputStream = getClass().getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new LengthIsUnavailableException(RESOURCE_FILE_WAS_NOT_FOUND_EXCEPTION_MESSAGE);
        }

        // Reading data by portions of the reading buffer size to calculate
        // length of the resource
        byte[] tempBuffer = new byte[readingBufferSize];
        int readBytes;
        try {
            while ((readBytes = inputStream.read(tempBuffer)) != -1) {
                length += readBytes;
            }
        } catch (IOException e) {
            throw new LengthIsUnavailableException("I/O error");
        }
        return length;
    }
    
    private String preparePath(String path) {
        if (checkPath(path)) {
            return rootPath + RESOURCE_PATH_DELIMETER + path;
        } else {
            return null;
        }
    }
    
    private boolean checkPath(String path) {
        int relativeDeepness = 0;
        // Checking that on each level of the path there is no passing lower 
        // than the specified root. It is needed to prevent accessing irrelevant
        // resources
        StringTokenizer tokenizer = new StringTokenizer(path, RESOURCE_PATH_DELIMETER);

        // Checking that number of parent directory traverses ".." in the
        // specified path is always bigger than the relative folder traverses
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            
            if ("..".equals(token)) {
                // Considering parent directory traverse
                relativeDeepness--;
                if (relativeDeepness < 0) {
                    return false;
                }
            } else if (token.length() > 0 && !token.equals(".")) {
                // Considering child directory traverse
                relativeDeepness++;
            }
            // Else this is doubled separator or dot both of which do not
            // require to move deeper on file system
        }
        return true;
    }

    /**
     * Returns the input stream of the resource located by the specified path.
     * The specified during the construction root path is used to resolve
     * relative paths.
     *
     * @param path {@inheritDoc}
     * @return {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @throws ResourceNotFoundException {@inheritDoc}
     * @throws IOException {@inheritDoc}
     */
    // @Override
    protected InputStream getStreamByPath(String path) throws IOException {
        Objects.requireNonNull(path, PATH_MUST_NOT_BE_NULL_EXCEPTION_MESSAGE);
        String resourcePath = preparePath(path);
        if (resourcePath == null) {
            throw new ResourceNotFoundException(ILLEGAL_PATH_EXCEPTION_MESSAGE);
        }
        InputStream resourceAsStream = getClass().getResourceAsStream(resourcePath);
        if (resourceAsStream == null) {
            throw new ResourceNotFoundException(RESOURCE_FILE_WAS_NOT_FOUND_EXCEPTION_MESSAGE);
        }
        return resourceAsStream;
    }
    
}
