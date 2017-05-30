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

/**
 * Abstract class to handle requests which have the common way to obtain the
 * accessed resource data by getting an input stream using path. This class uses
 * the relative part of the URI to locate the resource. If it is found, then
 * "200 OK" HTTP response is returned containing data of the requested resource
 * (If possible, content length and type are determined and added as headers to
 * the response). Otherwise a simple "404 Not Found" HTTP response is created
 * which body comprises status code and status message.<br><br>
 *
 * Only "GET" and "HEAD" HTTP request methods are supported. Any other request
 * methods cause "405 Method Not Allowed" HTTP response to be created and
 * returned.<br><br>
 *
 * If any known unrecoverable exception occurs during the response handling,
 * then "500 Internal Server Error" HTTP response is created and
 * returned.<br><br>
 *
 * In order to work properly the following methods should be implemented:<ul>
 * <li> {@link #getStreamByPath(java.lang.String) } which uses the
 * implementation-specific way to get the stream of the data to send in the
 * response.</li>
 * <li> {@link #getLength(java.lang.String) } which allows to determine the
 * length of the accessed resource if possible.</li>
 * </ul>
 *
 * This class provides an ability to set welcome page which is used when empty
 * or relative root "/" resource are requested to be handled. It can be set or
 * removed using {@link #setWelcomePagePath(java.lang.String)
 * } method. By default welcome page is set to
 * {@value #DEFAULT_WELCOME_PAGE}.<br><br>
 *
 * This class differs from the one which is used in the full version of the
 * demo: caching support has been removed.
 */
public abstract class PathRequestHandler implements RequestHandler {

    // Format string to create error response bodies
    private static final String ERROR_PAGE_TEMPLATE = "<!DOCTYPE html><head><title>%d - %s</title></head><body>%1$d - %2$s</body></html>";
    
    private static final String String_format(Status _status)
    {
        StringBuffer ret = new StringBuffer();
        
        ret.append("<!DOCTYPE html><head><title>").append(_status.getCode()).append(" - ").append(_status.getReason()).append("</title></head>");
        
        ret.append("<body>").append(_status.getCode()).append(" - ").append(_status.getReason()).append("</body></html>");
        
        return ret.toString();
    }

    // Delimiter to find extension of the file
    private static final char FILE_EXTENSION_DELIMETER = '.';

    // Delimiter of the URI parts
    private static final char PATH_DELIMETER = '/';

    // Welcome page to use when root resource is requested and its default value
    private static final String DEFAULT_WELCOME_PAGE = "/index.html";
    private volatile String welcomePage = DEFAULT_WELCOME_PAGE;

    /**
     * {@inheritDoc}<br><br>
     * See class javadoc for more information about the handling process.
     *
     * @param request {@inheritDoc}
     * @param contextPath {@inheritDoc}
     * @param relativePath {@inheritDoc}
     * @throws RuntimeException {@inheritDoc}
     * @return {@inheritDoc}
     */
    // @Override
    public HttpResponse handle(HttpRequest request, String contextPath, String relativePath) {
        Objects.requireNonNull(request, "Request must not be null");
        Objects.requireNonNull(relativePath, "Relative path must not be null");

        // Since welcomePage field is volatile there is no need in
        // synchronization block. Saving a copy of welcome page so that it does
        // not change by setter method during checking for a null
        String welcomePageCopy = welcomePage;

        // Substituting path with the welcome page if it is set and root 
        // resource is requested
        if (welcomePageCopy != null && new String(""+PATH_DELIMETER).equals(relativePath) || relativePath.length() == 0) {
            relativePath = welcomePageCopy;
        }

        int requestMethod = request.getRequestMethod();
        // Treating HEAD method the same as GET, assuming server will not send 
        // the body of the response. Only GET and HEAD methods are allowed
        if (requestMethod != HttpRequest.GET_REQUEST_METHOD && requestMethod != HttpRequest.HEAD_REQUEST_METHOD) {
            return HttpResponse.methodNotAllowed(getErrorMessage(Status.METHOD_NOT_ALLOWED)).addHeader("Allow", "HEAD, GET");
        }
        // If cache is not set or it is not possible to store entry in the
        // cache, an input stream is obtained and content length and type
        // are set if possible
        try {
            InputStream stream = getStreamByPath(relativePath);
            HttpResponse response = HttpResponse.ok();

            // If it is possible to determine length, it is added as a
            // header to the response
            try {
                int length = getLength(relativePath);
                response.addHeader(HttpResponse.CONTENT_LENGTH_HEADER, String.valueOf(length));
            } catch (LengthIsUnavailableException e) {
                // Otherwise length is not added 
            }

            addContentType(relativePath, response);
            return response.setBody(stream);
        } catch (ResourceNotFoundException e) {// If there is no resource which can be opened by the
            // requested path, then "404 Not Found" HTTP response is
            // returned
            return HttpResponse.notFound(getErrorMessage(Status.NOT_FOUND));
        } catch (IOException e) {
            // Returning "500 Internal Server Error" if an IOException was
            // thrown while input stream was being opened
            System.out.println("Error while obtaining stream: " + e.getMessage());
            return HttpResponse.internalServerError(getErrorMessage(Status.INTERNAL_SERVER_ERROR));
        }

    }

    private String getErrorMessage(Status status) {
        // return String.format(ERROR_PAGE_TEMPLATE, status.getCode(), status.getReason());
        return String_format(status);
    }

    private void addContentType(String path, HttpResponse response) {
        // Trying to extract extension part and determine content type by it
        int lastPartStartPosition = path.lastIndexOf(PATH_DELIMETER);
        int fileExtensionStartPosition = path.lastIndexOf(FILE_EXTENSION_DELIMETER);
        if (fileExtensionStartPosition > lastPartStartPosition) {
            String contentType = getContentType(path.substring(fileExtensionStartPosition + 1));
            response.addHeader(HttpResponse.CONTENT_TYPE_HEADER, contentType);
        }
    }

    private static String getContentType(String fileExtension) {
        if(fileExtension.equals("html")) return "text/html";
        else if(fileExtension.equals("htm")) return "text/html";
        else if(fileExtension.equals("xml")) return "text/xml";
        else if(fileExtension.equals("txt")) return "text/plain";
        else if(fileExtension.equals("png")) return "image/png";
        else if(fileExtension.equals("jpg")) return "image/jpeg";
        else if(fileExtension.equals("gif")) return "image/gif";
        else if(fileExtension.equals("jad")) return "text/vnd.sun.j2me.app-descriptor";
        else if(fileExtension.equals("jar")) return "application/java-archive";
        else if(fileExtension.equals("exe")) return "application/octet-stream";
        else if(fileExtension.equals("zip")) return "application/zip";
        else if(fileExtension.equals("wav")) return "audio/x-wav";
        else if(fileExtension.equals("jpeg")) return "image/jpeg";
        else if(fileExtension.equals("text")) return "text/plain";
        else if(fileExtension.equals("java")) return "text/plain";
        else if(fileExtension.equals("js")) return "text/javascript";
        else if(fileExtension.equals("pdf")) return "application/pdf";
        else if(fileExtension.equals("bmp")) return "image/bmp";
        else if(fileExtension.equals("css")) return "text/css";
        else if(fileExtension.equals("class")) return "application/java-vm";
        else return "content/unknown";
    }

    /**
     * Sets the relative URI path to the welcome page, which will be used when
     * empty or relative root "/" resource are requested to be handled. To
     * prevent welcome page usage, specify null as a parameter. It is safe to
     * change welcome page at any time by any thread.
     *
     * @param welcomePagePath relative URI path (without context path) to the
     * welcome page or null to prevent welcome page usage
     */
    public void setWelcomePagePath(String welcomePagePath) {
        this.welcomePage = welcomePagePath;
    }

    /**
     * Returns relative path to the currently used welcome page. If welcome page
     * is not used, then null is returned.
     *
     * @return relative URI (without context path) to the welcome page or null
     * if welcome page is not used
     */
    public String getWelcomePagePath() {
        return welcomePage;
    }

    /**
     * Returns the length in bytes of the resource located by the specified
     * path. The provided path is relative, so it is up to an implementation to
     * decide what to consider as a root directory.
     *
     * @param path relative path to the resource
     * @return nonnegative length of the resource in bytes
     * @throws NullPointerException if path is null
     * @throws LengthIsUnavailableException if length cannot be obtained or any
     * error occurs during length obtaining
     */
    protected abstract int getLength(String path) throws LengthIsUnavailableException;

    /**
     * Returns the input stream of the resource located by the specified path.
     * The provided path is relative, so it is up to an implementation to decide
     * what to consider as a root directory.
     *
     * @param path relative path to the resource
     * @return input stream of the resource
     * @throws NullPointerException if path is null
     * @throws ResourceNotFoundException if there is no resource located by the
     * specified path
     * @throws IOException if an I/O error occurs during stream opening
     */
    protected abstract InputStream getStreamByPath(String path) throws ResourceNotFoundException, IOException;

}
