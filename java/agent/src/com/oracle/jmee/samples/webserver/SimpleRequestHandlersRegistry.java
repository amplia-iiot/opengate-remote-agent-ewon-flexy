/*
 * Copyright © 2015, Oracle and/or its affiliates.  All rights reserved. 
 *
 * This software is dual-licensed to you under the MIT License (MIT) and the
 * Universal Permissive License (UPL).  See the LICENSE file in the root directory
 * for license terms.  You may choose either license, or both.
 */
package com.oracle.jmee.samples.webserver;

/*
import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
*/
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import amplia.util.Objects;

/**
 * A simple implementation of registry of the HTTP request handlers which uses a
 * map to store bindings. This implementation finds the longest context path
 * match when {@link #findSuitableContext(java.lang.String)} and
 * {@link #get(java.lang.String)} methods are called. "/", which is treated the
 * same as an empty string and null, context path allows to set default context
 * handler, which will be returned if none of the bindings match the request
 * path. Paths are normalized before they are used.
 */
public class SimpleRequestHandlersRegistry implements RequestHandlersRegistry {

    // Delimiters
    private static final String URI_PARTS_DELIMITER_STRING = "/";
    private static final char URI_PARTS_DELIMITER_CHARACTER = '/';

    // Map to store bindings
    // private final Map<String, RequestHandler> handlers = new HashMap<>();
    private final Hashtable handlers = new Hashtable();

    /**
     * Creates a new SimpleRequestHandlersRegistry.
     */
    public SimpleRequestHandlersRegistry() {
    }

    /**
     * {@inheritDoc}. Providing null or an empty string as a path is treated
     * like the root context path is provided.
     *
     * @param contextPath {@inheritDoc}
     * @param handler {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @throwsHttpServerIllegalStateException {@inheritDoc}
     */
    // @Override
    public void add(String contextPath, RequestHandler handler) {
        Objects.requireNonNull(handler, "Handler must not be null");
        contextPath = prepareContextPath(contextPath);
        if (handlers.containsKey(contextPath)) {
            // throw new IllegalStateException("Registry already contains a binding for the provided context path: " + contextPath);
            System.out.println("Registry already contains a binding for the provided context path: " + contextPath);
        } else
        {
            handlers.put(contextPath, handler);
        }
    }

    private String prepareContextPath(String contextPath) {
        // Making context path start with a dash and removing trailing dashes if
        // there are any of them
        if (contextPath == null) {
            contextPath = "";
        }

        // normalizing context path
        contextPath = normalizeIndependentFromFileSystem(contextPath);

        while (contextPath.endsWith(URI_PARTS_DELIMITER_STRING)) {
            contextPath = contextPath.substring(0, contextPath.length() - 1);
        }

        if (!contextPath.startsWith(URI_PARTS_DELIMITER_STRING)) {
            contextPath = URI_PARTS_DELIMITER_STRING + contextPath;
        }
        return contextPath;
    }

    private static String normalizeIndependentFromFileSystem(String path) {
        try {
            // String fileSeparator = FileSystems.getDefault().getSeparator();
            String fileSeparator = System.getProperty("file.separator");
            if (fileSeparator != null && fileSeparator.length() > 0) {
                char fileSeparatorChar = fileSeparator.charAt(0);
                path = path.replace(URI_PARTS_DELIMITER_CHARACTER, fileSeparatorChar);
                // OJO Está sin completar. Hya que implementarlo
                // path = Paths.get(path).normalize().toString();
                path = path.replace(fileSeparatorChar, URI_PARTS_DELIMITER_CHARACTER);
            }
        } 
        catch (Exception e) //NullPointerException | InvalidPathException
        {
            // do nothing, normalization failed
        }
        return path;
    }

    // @Override
    public String findSuitableContext(String path) {
        return getSuitableContextPath(path);
    }

    private String getSuitableContextPath(String path) {
        path = prepareContextPath(path);
        String returnedContextPath = null;
        int longestMatch = -1;
        
        for (Enumeration en=handlers.keys();en.hasMoreElements();)
        {
            String contextPath = (String)en.nextElement();
            // Checking that path starts with any of the stored context paths.
            // It is required that matched part of the path ends logically 
            // (either next part of the path starts or there are no other parts)
            if (path.startsWith(contextPath) && (path.length() == contextPath.length() || path.charAt(contextPath.length()) == URI_PARTS_DELIMITER_CHARACTER || contextPath.equals(URI_PARTS_DELIMITER_STRING))) {
                if (longestMatch < contextPath.length()) {
                    longestMatch = contextPath.length();
                    returnedContextPath = contextPath;
                }
            }            
        }
        return returnedContextPath;
    }

    // @Override
    public RequestHandler get(String path) {
        String suitableContextPath = getSuitableContextPath(path);
        if (suitableContextPath != null) {
            return (RequestHandler)handlers.get(suitableContextPath);
        }
        return null;
    }

    // @Override
    public RequestHandler getByContextPath(String contextPath) {
        contextPath = prepareContextPath(contextPath);
        return (RequestHandler)handlers.get(contextPath);
    }

    // @Override
    public void removeContextPath(String contextPath) {
        contextPath = prepareContextPath(contextPath);
        handlers.remove(contextPath);
    }

    /**
     * {@inheritDoc}
     *
     * @param handler {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    // @Override
    public void remove(RequestHandler handler) {
        Objects.requireNonNull(handler, "Handler must not be null");
        // A handler may be registered for multiple context paths. Using
        // removeAll method to remove all map entries with the handler value
        // handlers.values().removeAll(Arrays.asList(handler));

        Vector keysToDelete = new Vector();
        for (Enumeration en=handlers.keys();en.hasMoreElements();)
        {
            String key = (String)en.nextElement();
            RequestHandler rh = (RequestHandler)handlers.get(key);
            
            if(rh == handler)
            {
                keysToDelete.addElement(key);
            }
        }
        for (Enumeration en=keysToDelete.elements();en.hasMoreElements();)
        {
            String key = (String)en.nextElement();
            handlers.remove(key);
        }
    }

    // @Override
    public void clear() {
        handlers.clear();
    }
}
