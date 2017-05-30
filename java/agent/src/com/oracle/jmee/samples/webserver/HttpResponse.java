/*
 * Copyright © 2015, Oracle and/or its affiliates.  All rights reserved. 
 *
 * This software is dual-licensed to you under the MIT License (MIT) and the
 * Universal Permissive License (UPL).  See the LICENSE file in the root directory
 * for license terms.  You may choose either license, or both.
 */
package com.oracle.jmee.samples.webserver;

import com.oracle.jmee.samples.webserver.util.DateUtils;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

/*
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
*/
import amplia.util.Objects;

/**
 * Class which represents an HTTP response. HTTP responses are sent as a result
 * of HTTP request handling.<br><br>
 *
 * In general HTTP response contains a status code and status reason, headers
 * and body. There are two general ways to create a response: <ul>
 * <li>a plain way is to call one of the constructors</li>
 * <li>a simpler way to create common simple responses with content is to use
 * helper factory methods like {@link #ok()}</li>
 * </ul>
 *
 * After the response has been created, it contains the {@value #DATE_HEADER}
 * header filled with creation date, {@value #CONTENT_LENGTH_HEADER} has zero
 * length. Other data may be filled using various setter methods. Please notice
 * that most methods of this class return the "this" instance to support method
 * chaining to provide an easier way to fill the response. However there is no
 * build() method common for the Builder pattern.<br><br>
 *
 * For more information about HTTP protocol and HTTP responses please see <a
 * href="http://tools.ietf.org/html/rfc7230">RFC 7230</a>.
 */
public class HttpResponse {

    private static final String NEW_LINE_CHARACTERS = "\r\n";
    // Some commonly used headers
    /**
     * Header name, which value denotes a length of the body inside an HTTP
     * message
     */
    public static final String CONTENT_LENGTH_HEADER = "Content-Length";

    /**
     * Header name, which value denotes media type of the body inside an HTTP
     * message
     */
    public static final String CONTENT_TYPE_HEADER = "Content-Type";

    /**
     * Header name, which value denotes the date when an HTTP message was
     * originated
     */
    public static final String DATE_HEADER = "Date";

    // Status code of the message
    private int statusCode;

    // Message which supplements status code
    private String statusReason;

    // This header field represents the date and time at which the response has
    // been originated
    private Date creationDate;

    // Map which contains headers: keys are header names, values are header
    // values
    // private final Map<String, String> headers = new HashMap<>();
    private final Hashtable headers = new Hashtable(); // String, String

    // Flag to check whether content length has been explicitly set by client
    private boolean contentLengthSetByClient = false;

    {
        // Initially setting body to null and adding zero content length
        nullifyBodyAndSetZeroLengthIfNotSet();
    }

    // List of cookies which are to be set
    // private final List<HttpCookie> cookiesToSet = new ArrayList<>();
    private final Vector cookiesToSet = new Vector(); // List<HttpCookie>

    // Response body
    private InputStream body;

    /**
     * Creates a new HTTP response with the specified status code and reason.
     * See {@link Status} for predefined status codes and reasons.
     *
     * @param statusCode status code of the HTTP response
     * @param statusReason status reason of the HTTP response
     * @throws NullPointerException if status reason is null
     * @see Status
     * @see #HttpResponse(com.oracle.jmee.samples.webserver.Status)
     */
    public HttpResponse(int statusCode, String statusReason) {
        Objects.requireNonNull(statusReason, "Status reason must not be null");

        this.statusCode = statusCode;
        this.statusReason = statusReason;
        setCreationDateInternal(DateUtils.getCurrentGmtDateTime());
    }

    /**
     * Creates a new HTTP response with the specified status code. Status reason
     * is filled with the default value for this code if present. Otherwise
     * status reason is an empty string.
     *
     * @param statusCode status code of the HTTP response
     * @see Status
     * @see #HttpResponse(com.oracle.jmee.samples.webserver.Status)
     */
    public HttpResponse(int statusCode) {
        setStatusByCode(statusCode);
        setCreationDateInternal(DateUtils.getCurrentGmtDateTime());
    }

    /**
     * Creates a new HTTP response with the specified status. Status code and
     * status reason are obtained from the provided status.
     *
     * @param status status to set the status code and status reason
     * @throws NullPointerException if status is null
     */
    public HttpResponse(Status status) {
        Objects.requireNonNull(status, "Status must not be null");

        this.statusCode = status.getCode();
        this.statusReason = status.getReason();
        setCreationDateInternal(DateUtils.getCurrentGmtDateTime());
    }

    /**
     * Helper method to create an HTTP 200 OK response.
     *
     * @return the HTTP response with 200 OK status
     */
    public static HttpResponse ok() {
        return new HttpResponse(Status.OK);
    }

    /**
     * Helper method to create an HTTP 200 OK response with the specified body
     * as input stream. It is a faster equivalent to the
     * {@code new HttpResponse(Status.OK).setBody(body);}.
     *
     * @param body HTTP response body
     * @return the HTTP response with 200 OK status and the provided body
     */
    public static HttpResponse ok(InputStream body) {
        return new HttpResponse(Status.OK).setBody(body);
    }

    /**
     * Helper method to create an HTTP 200 OK response with the specified body
     * as byte array. It is a faster equivalent to the
     * {@code new HttpResponse(Status.OK).setBody(body);}.
     *
     * @param body HTTP response body
     * @return the HTTP response with 200 OK status and the provided body
     */
    public static HttpResponse ok(byte[] body) {
        return new HttpResponse(Status.OK).setBody(body);
    }

    /**
     * Helper method to create an HTTP 200 OK response with the specified body
     * as string. It is a faster equivalent to the
     * {@code new HttpResponse(Status.OK).setBody(body);}.
     *
     * @param body HTTP response body
     * @return the HTTP response with 200 OK status and the provided body
     */
    public static HttpResponse ok(String body) {
        System.out.println("HttpResponse ok()" + body);
        return new HttpResponse(Status.OK).setBody(body);
    }
    
    /**
     * Helper method to create an HTTP 200 OK response.
     *
     * @return the HTTP response with 200 OK status
     */
    public static HttpResponse created() {
        return new HttpResponse(Status.CREATED);
    }

    /**
     * Helper method to create an HTTP 200 OK response with the specified body
     * as input stream. It is a faster equivalent to the
     * {@code new HttpResponse(Status.OK).setBody(body);}.
     *
     * @param body HTTP response body
     * @return the HTTP response with 200 OK status and the provided body
     */
    public static HttpResponse created(InputStream body) {
        return new HttpResponse(Status.CREATED).setBody(body);
    }

    /**
     * Helper method to create an HTTP 200 OK response with the specified body
     * as byte array. It is a faster equivalent to the
     * {@code new HttpResponse(Status.OK).setBody(body);}.
     *
     * @param body HTTP response body
     * @return the HTTP response with 200 OK status and the provided body
     */
    public static HttpResponse created(byte[] body) {
        return new HttpResponse(Status.CREATED).setBody(body);
    }

    /**
     * Helper method to create an HTTP 200 OK response with the specified body
     * as string. It is a faster equivalent to the
     * {@code new HttpResponse(Status.OK).setBody(body);}.
     *
     * @param body HTTP response body
     * @return the HTTP response with 200 OK status and the provided body
     */
    public static HttpResponse created(String body) {
        System.out.println("HttpResponse ok()" + body);
        return new HttpResponse(Status.CREATED).setBody(body);
    }    

    /**
     * Helper method to create an HTTP 400 Bad Request response.
     *
     * @return the HTTP response with 400 Bad Request status
     */
    public static HttpResponse badRequest() {
        return new HttpResponse(Status.BAD_REQUEST);
    }

    /**
     * Helper method to create an HTTP 400 Bad Request response with the
     * specified body as input stream. It is a faster equivalent to the
     * {@code new HttpResponse(Status.BAD_REQUEST).setBody(body);}.
     *
     * @param body HTTP response body
     * @return the HTTP response with 400 Bad Request status and the provided
     * body
     */
    public static HttpResponse badRequest(InputStream body) {
        return new HttpResponse(Status.BAD_REQUEST).setBody(body);
    }

    /**
     * Helper method to create an HTTP 400 Bad Request response with the
     * specified body as byte array. It is a faster equivalent to the
     * {@code new HttpResponse(Status.BAD_REQUEST).setBody(body);}.
     *
     * @param body HTTP response body
     * @return the HTTP response with 400 Bad Request status and the provided
     * body
     */
    public static HttpResponse badRequest(byte[] body) {
        return new HttpResponse(Status.BAD_REQUEST).setBody(body);
    }

    /**
     * Helper method to create an HTTP 400 Bad Request response with the
     * specified body as string. It is a faster equivalent to the
     * {@code new HttpResponse(Status.BAD_REQUEST).setBody(body);}.
     *
     * @param body HTTP response body
     * @return the HTTP response with 400 Bad Request status and the provided
     * body
     */
    public static HttpResponse badRequest(String body) {
        return new HttpResponse(Status.BAD_REQUEST).setBody(body);
    }

    /**
     * Helper method to create an HTTP 403 Forbidden response.
     *
     * @return the HTTP response with 403 Forbidden status
     */
    public static HttpResponse forbidden() {
        return new HttpResponse(Status.FORBIDDEN);
    }

    /**
     * Helper method to create an HTTP 403 Forbidden response with the specified
     * body as input stream. It is a faster equivalent to the
     * {@code new HttpResponse(Status.FORBIDDEN).setBody(body);}.
     *
     * @param body HTTP response body
     * @return the HTTP response with 403 Forbidden status and the provided body
     */
    public static HttpResponse forbidden(InputStream body) {
        return new HttpResponse(Status.FORBIDDEN).setBody(body);
    }

    /**
     * Helper method to create an HTTP 403 Forbidden response with the specified
     * body as byte array. It is a faster equivalent to the
     * {@code new HttpResponse(Status.FORBIDDEN).setBody(body);}.
     *
     * @param body HTTP response body
     * @return the HTTP response with 403 Forbidden status and the provided body
     */
    public static HttpResponse forbidden(byte[] body) {
        return new HttpResponse(Status.FORBIDDEN).setBody(body);
    }

    /**
     * Helper method to create an HTTP 403 Forbidden response with the specified
     * body as string. It is a faster equivalent to the
     * {@code new HttpResponse(Status.FORBIDDEN).setBody(body);}.
     *
     * @param body HTTP response body
     * @return the HTTP response with 403 Forbidden status and the provided body
     */
    public static HttpResponse forbidden(String body) {
        return new HttpResponse(Status.FORBIDDEN).setBody(body);
    }

    /**
     * Helper method to create an HTTP 404 Not Found response.
     *
     * @return the HTTP response with 404 Not Found status
     */
    public static HttpResponse notFound() {
        return new HttpResponse(Status.NOT_FOUND);
    }

    /**
     * Helper method to create an HTTP 404 Not Found response with the specified
     * body as input stream. It is a faster equivalent to the
     * {@code new HttpResponse(Status.NOT_FOUND).setBody(body);}.
     *
     * @param body HTTP response body
     * @return the HTTP response with 404 Not Found status and the provided body
     */
    public static HttpResponse notFound(InputStream body) {
        return new HttpResponse(Status.NOT_FOUND).setBody(body);
    }

    /**
     * Helper method to create an HTTP 404 Not Found response with the specified
     * body as byte array. It is a faster equivalent to the
     * {@code new HttpResponse(Status.NOT_FOUND).setBody(body);}.
     *
     * @param body HTTP response body
     * @return the HTTP response with 404 Not Found status and the provided body
     */
    public static HttpResponse notFound(byte[] body) {
        return new HttpResponse(Status.NOT_FOUND).setBody(body);
    }

    /**
     * Helper method to create an HTTP 404 Not Found response with the specified
     * body as string. It is a faster equivalent to the
     * {@code new HttpResponse(Status.NOT_FOUND).setBody(body);}.
     *
     * @param body HTTP response body
     * @return the HTTP response with 404 Not Found status and the provided body
     */
    public static HttpResponse notFound(String body) {
        return new HttpResponse(Status.NOT_FOUND).setBody(body);
    }

    /**
     * Helper method to create an HTTP 405 Method Not Allowed response.
     *
     * @return the HTTP response with 405 Method Not Allowed status
     */
    public static HttpResponse methodNotAllowed() {
        return new HttpResponse(Status.METHOD_NOT_ALLOWED);
    }

    /**
     * Helper method to create an HTTP 405 Method Not Allowed response with the
     * specified body as input stream. It is a faster equivalent to the
     * {@code new HttpResponse(Status.METHOD_NOT_ALLOWED).setBody(body);}.
     *
     * @param body HTTP response body
     * @return the HTTP response with 405 Method Not Allowed status and the
     * provided body
     */
    public static HttpResponse methodNotAllowed(InputStream body) {
        return new HttpResponse(Status.METHOD_NOT_ALLOWED).setBody(body);
    }

    /**
     * Helper method to create an HTTP 405 Method Not Allowed response with the
     * specified body as byte array. It is a faster equivalent to the
     * {@code new HttpResponse(Status.METHOD_NOT_ALLOWED).setBody(body);}.
     *
     * @param body HTTP response body
     * @return the HTTP response with 405 Method Not Allowed status and the
     * provided body
     */
    public static HttpResponse methodNotAllowed(byte[] body) {
        return new HttpResponse(Status.METHOD_NOT_ALLOWED).setBody(body);
    }

    /**
     * Helper method to create an HTTP 405 Method Not Allowed response with the
     * specified body as string. It is a faster equivalent to the
     * {@code new HttpResponse(Status.METHOD_NOT_ALLOWED).setBody(body);}.
     *
     * @param body HTTP response body
     * @return the HTTP response with 405 Method Not Allowed status and the
     * provided body
     */
    public static HttpResponse methodNotAllowed(String body) {
        return new HttpResponse(Status.METHOD_NOT_ALLOWED).setBody(body);
    }

    /**
     * Helper method to create an HTTP 500 Internal Server Error response.
     *
     * @return the HTTP response with 500 Internal Server Error status
     */
    public static HttpResponse internalServerError() {
        return new HttpResponse(Status.INTERNAL_SERVER_ERROR);
    }

    /**
     * Helper method to create an HTTP 500 Internal Server Error response with
     * the specified body as input stream. It is a faster equivalent to the
     * {@code new HttpResponse(Status.INTERNAL_SERVER_ERROR).setBody(body);}.
     *
     * @param body HTTP response body
     * @return the HTTP response with 500 Internal Server Error status and the
     * provided body
     */
    public static HttpResponse internalServerError(InputStream body) {
        return new HttpResponse(Status.INTERNAL_SERVER_ERROR).setBody(body);
    }

    /**
     * Helper method to create an HTTP 500 Internal Server Error response with
     * the specified body as byte array. It is a faster equivalent to the
     * {@code new HttpResponse(Status.INTERNAL_SERVER_ERROR).setBody(body);}.
     *
     * @param body HTTP response body
     * @return the HTTP response with 500 Internal Server Error status and the
     * provided body
     */
    public static HttpResponse internalServerError(byte[] body) {
        return new HttpResponse(Status.INTERNAL_SERVER_ERROR).setBody(body);
    }

    /**
     * Helper method to create an HTTP 500 Internal Server Error response with
     * the specified body as string. It is a faster equivalent to the
     * {@code new HttpResponse(Status.INTERNAL_SERVER_ERROR).setBody(body);}.
     *
     * @param body HTTP response body
     * @return the HTTP response with 500 Internal Server Error status and the
     * provided body
     */
    public static HttpResponse internalServerError(String body) {
        return new HttpResponse(Status.INTERNAL_SERVER_ERROR).setBody(body);
    }

    /**
     * Modifies the status of the HTTP response. Status code and reason are set
     * according to the values contained in the status.
     *
     * @param status status to set status code and reason
     * @return the same HTTP response instance
     * @throws NullPointerException if status is null
     * @see Status
     */
    public HttpResponse setStatus(Status status) {
        Objects.requireNonNull(status, "Status must not be null");

        this.statusCode = status.getCode();
        this.statusReason = status.getReason();
        return this;
    }

    /**
     * Modifies the status of the HTTP response by setting status code. If the
     * supplied status code is one of the predefined ones, then status reason is
     * set accordingly to the corresponding predefined value. Otherwise status
     * reason is set to an empty string.
     *
     * @param statusCode new status code of the HTTP response
     * @return the same HTTP response instance
     */
    public HttpResponse setStatusCode(int statusCode) {
        setStatusByCode(statusCode);
        return this;
    }

    private void setStatusByCode(int statusCode) {
        Status status = Status.getByCode(statusCode);
        if (status != null) {
            this.statusCode = status.getCode();
            this.statusReason = status.getReason();
        } else {
            this.statusCode = statusCode;
            this.statusReason = "";
        }
    }

    /**
     * Modifies the status reason of this HTTP response. Status code is not
     * modified.
     *
     * @param statusReason new status reason of the HTTP response
     * @throws NullPointerException if status reason is null
     * @return the same HTTP response instance
     */
    public HttpResponse setStatusReason(String statusReason) {
        Objects.requireNonNull(statusReason, "Status reason must not be null");

        this.statusReason = statusReason;
        return this;
    }

    /**
     * Changes the value of {@value #DATE_HEADER} of this response. By default
     * {@value #DATE_HEADER} is initialized to the creation date of this object
     * during construction. This method allows to remove the header by providing
     * null value as a parameter.
     *
     * @param newDate new value of the {@value #DATE_HEADER} or null to remove
     * the header
     * @return the same HTTP response instance
     */
    public HttpResponse setDate(Date newDate) {
        setCreationDateInternal(newDate);
        return this;
    }

    private void setCreationDateInternal(Date newDate) {
        if (newDate == null) {
            this.headers.remove(DATE_HEADER);
        } else {
            this.headers.put(DATE_HEADER, DateUtils.httpDateToString(newDate));
        }
        this.creationDate = newDate;
    }

    /**
     * Adds a new header field with the specified field name and field value to
     * the response. If the header with the specified name already exists among
     * the headers, its value is overwritten. In order to remove header, use
     * {@link #removeHeader(java.lang.String)}.
     *
     * @param fieldName the name of the header field to add
     * @param fieldValue the value of the header field to add
     * @return the same HTTP response instance
     * @throws NullPointerException if fieldName or fieldValue is null
     * @see #removeHeader(java.lang.String)
     */
    public HttpResponse addHeader(String fieldName, String fieldValue) {
        Objects.requireNonNull(fieldName, "Field name must not be null");
        Objects.requireNonNull(fieldValue, "Field value must not be null");

        if (CONTENT_LENGTH_HEADER.equals(fieldName)) {
            contentLengthSetByClient = true;
        }

        // Setting the date 
        if (DATE_HEADER.equals(fieldName)) {
            creationDate = DateUtils.stringToHttpDate(fieldValue);
        }

        this.headers.put(fieldName, fieldValue);
        return this;
    }

    /**
     * Adds all header fields from the specified map. The map keys are treated
     * as field names and values are treated as field values. If the original
     * headers contain any of the header fields with the same name as in the
     * specified map, then its value will be overwritten.
     *
     * @param headers the map which contains header field names and values to
     * add
     * @return the same HTTP response instance
     * @throws NullPointerException if headers is null
     */
    public HttpResponse addHeaders(Hashtable headers) {
        Objects.requireNonNull(headers, "Headers map must not be null");

        // Marking that content length has been changed explicitly
        if (headers.containsKey(CONTENT_LENGTH_HEADER)) {
            contentLengthSetByClient = true;
        }

        // Saving the creation date if it is among headers to set
        String date = (String)headers.get(DATE_HEADER);
        if (date != null) {
            creationDate = DateUtils.stringToHttpDate(date);
        }
        
        // this.headers.putAll(headers);
        for (Enumeration en=headers.keys();en.hasMoreElements();)
        {
            String key = (String)en.nextElement();
            String value = (String)headers.get(key);
            
            this.headers.put(key, value);
        }
        return this;
    }

    /**
     * Removes the header field with the specified name from the headers of the
     * response.
     *
     * @param headerName the name of the header field
     * @return the same HTTP response instance
     * @throws NullPointerException if headerName is null
     */
    public HttpResponse removeHeader(String headerName) {
        Objects.requireNonNull(headerName, "Header name must not be null");
        headers.remove(headerName);
        return this;
    }

    /**
     * Adds a cookie to the response. This method allows to add cookies one per
     * method call. Consider using {@link #addCookies(java.util.Collection)}
     * method which allows to add multiple cookies at a time. Adding a cookie
     * which name is equal to one of the cookies which has already been added,
     * results in an exception.
     *
     * @param cookie the cookie to add
     * @return the same HTTP response instance
     * @throws NullPointerException if cookie is null
     * @throws IllegalArgumentException if there is a cookie with such name
     */
    public HttpResponse addCookie(HttpCookie cookie) {
        Objects.requireNonNull(cookie, "Cookie must not be null");
        addCookieWithNameCheck(cookie);
        return this;
    }

    private void addCookieWithNameCheck(HttpCookie cookie) /* throws IllegalArgumentException*/ {
        HttpCookie cookieWithTheSameName = getCookieByName(cookie.getName());
        if (cookieWithTheSameName != null) {
            // throw new IllegalArgumentException("There is already a cookie with such name: " + cookieWithTheSameName);
            System.out.println("There is already a cookie with such name: " + cookieWithTheSameName);
        }
        cookiesToSet.addElement(cookie);
    }

    /**
     * Adds multiple cookies to the response. Adding a cookie which name is
     * equal to one of the cookies which has already been added, results in an
     * exception.
     *
     * @param cookies collection of the cookies to add
     * @return the same HTTP response instance
     * @throws NullPointerException if cookies is null
     * @throws IllegalArgumentException if one of the added cookies has the same
     * name with one of the currently present cookies. In case this exception is
     * thrown, the response will have all cookies added which appeared prior to
     * the cookie with conflicting name during collection traversal
     */
    public HttpResponse addCookies(Vector cookies) {
        Objects.requireNonNull(cookies, "Cookies collection must not be null");

        for (Enumeration en=cookies.elements();en.hasMoreElements();)
        {
            HttpCookie addedCookie = (HttpCookie)en.nextElement();
            addCookieWithNameCheck(addedCookie);            
        }
        return this;
    }

    /**
     * Removes the cookie with the specified name from the cookies which are
     * appended to the response. Nothing will happen if there is no cookie with
     * such name.
     *
     * @param cookieName name of the cookie to remove
     * @return the same HTTP response instance
     * @throws NullPointerException if cookie name is null
     */
    public HttpResponse removeCookieByName(String cookieName) {
        Objects.requireNonNull(cookieName, "Cookie name must not be null");
        
        for (Enumeration en=cookiesToSet.elements();en.hasMoreElements();)
        {
            HttpCookie cookie = (HttpCookie)en.nextElement();
            if (cookie.getName().equals(cookieName)) {
                cookiesToSet.removeElement(cookie);
                break;
            }            
        }
        return this;
    }

    private HttpCookie getCookieByName(String cookieName) {
        for (Enumeration en=cookiesToSet.elements();en.hasMoreElements();)
        {
            HttpCookie cookie = (HttpCookie)en.nextElement();
            if (cookie.getName().equals(cookieName)) {
                return cookie;
            }            
        }
        return null;
    }

    /**
     * Sets the body part of the response as an input stream. This method sets
     * an input stream as a body which may be useful, when the resource is too
     * long to be buffered. This method does not try to determine the content
     * length of the body as other setBody(...) methods do.
     *
     * @param body the input stream which contains the data to write to the
     * response or null to remove the body
     * @return the same HTTP response instance
     */
    public HttpResponse setBody(InputStream body) {
        this.body = body;

        // Removing default zero content length if it is not set by client
        if (!contentLengthSetByClient) {
            headers.remove(CONTENT_LENGTH_HEADER);
        }
        return this;
    }

    /**
     * Sets the body part of the response as a byte array. If the content length
     * of this response instance has not ever been set explicitly (either as a
     * header or via {@link #setContentLength(int)} method), then the size of
     * the specified body is set implicitly by this method using the length of
     * the byte array.
     *
     * @param body the array which contains data to be written to the response
     * or null to remove the body
     * @return the same HTTP response instance
     */
    public HttpResponse setBody(byte[] body) {
        if (body == null) {
            nullifyBodyAndSetZeroLengthIfNotSet();
            return this;
        }
        setByteArrayBody(body);
        return this;
    }

    private void setByteArrayBody(byte[] body) {
        this.body = new ByteArrayInputStream(body);
        if (!contentLengthSetByClient) {
            headers.put(CONTENT_LENGTH_HEADER, String.valueOf(body.length));
        }
    }

    /**
     * Sets the body part of the response as a string. If the content length of
     * this response instance has not ever been set explicitly (either as a
     * header or via {@link #setContentLength(int)} method), then the size of
     * the specified body is set implicitly by this method using the length of
     * the string in bytes. Platform default encoding is used to convert the
     * provided string to the bytes which are sent.
     *
     * @param body the string which contains data to be written to the response
     * or null to remove the body
     * @return the same HTTP response instance
     */
    public HttpResponse setBody(String body) {
        if (body == null) {
            nullifyBodyAndSetZeroLengthIfNotSet();
            return this;
        }
        byte[] bytes = body.getBytes();
        setByteArrayBody(bytes);
        return this;
    }

    /**
     * Sets the {@value #CONTENT_LENGTH_HEADER} header value. After the explicit
     * setting of the content length, setBody(...) methods stop determining
     * length of the content by the passed arguments.
     *
     * @param contentLength the length of the body in the response
     * @return the same HTTP response instance
     * @throws IllegalArgumentException if the content length is negative
     */
    public HttpResponse setContentLength(int contentLength) {
        if (contentLength < 0) {
            // throw new IllegalArgumentException("Content length must be nonnegative");
            System.out.println("Content length must be nonnegative");
        } else
        {
            headers.put(CONTENT_LENGTH_HEADER, String.valueOf(contentLength));
            contentLengthSetByClient = true;
        }
        return this;
    }

    private void nullifyBodyAndSetZeroLengthIfNotSet() {
        body = null;
        if (!contentLengthSetByClient) {
            // if content length is not set excplicitly, then content is zero
            // bytes long
            headers.put(CONTENT_LENGTH_HEADER, "0");
        }
    }

    /**
     * Sets the content type for this response. It is possible to remove the
     * content type header by specifying null as an argument.
     *
     * @param contentType content type to set or null to remove the content type
     * @return the same HTTP response instance
     */
    public HttpResponse setContentType(String contentType) {
        if (contentType == null) {
            headers.remove(CONTENT_TYPE_HEADER);
        } else {
            headers.put(CONTENT_TYPE_HEADER, contentType);
        }
        return this;
    }

    /**
     * Returns the status code of the response.
     *
     * @return the status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Returns the status reason of the response.
     *
     * @return the status reason
     */
    public String getStatusReason() {
        return statusReason;
    }

    /**
     * Return the value of the {@value #DATE_HEADER} header of this response.
     *
     * @return date of the response origination or null if it is not set
     */
    public Date getDate() {
        return creationDate;
    }

    /**
     * Returns all headers that this request contains. All modifications are
     * reflected on the contained in the response headers and therefore are
     * discouraged.
     *
     * @return the map which contains header name to header value bindings
     */
    public Hashtable getHeaders() {
        return headers;
    }

    /**
     * Returns the list of HTTP cookies which are contained in the response.
     *
     * @return list of HTTP cookies
     */
    public Vector getCookies() {
        return cookiesToSet;
    }

    /**
     * Returns the string which contains the status line and headers part of the
     * HTTP response formatted appropriate to send to the receiving party. In
     * order to complete the response sending a "CRLF" byte sequence should be
     * sent followed by optional message body part.
     *
     * @return the string formatted as the HTTP response part
     */
    public String getHeadersAsString() {
        StringBuffer builder = new StringBuffer();

        for (Enumeration en=headers.keys();en.hasMoreElements();)
        {
            String key = (String)en.nextElement();
            String value = (String)headers.get(key);

            builder.append(key).append(": ").append(value);
            builder.append(NEW_LINE_CHARACTERS);        
        }  
        
        for (Enumeration en=cookiesToSet.elements();en.hasMoreElements();)
        {
            HttpCookie cookie = (HttpCookie)en.nextElement();
            builder.append("Set-Cookie: ").append(cookie.toString()).append(NEW_LINE_CHARACTERS);
            
        }
        return builder.toString();
    }

    /**
     * Returns the content length of the body if it is specified.
     *
     * @return the content length of the body or -1 if it is not specified
     */
    public int getContentLength() {
        String contentLengthString = (String)headers.get(CONTENT_LENGTH_HEADER);
        if (contentLengthString != null) {
            try {
                return Integer.parseInt(contentLengthString);
            } catch (NumberFormatException e) {
                return -1;
            }
        } else {
            return -1;
        }
    }

    /**
     * Returns the body of the response as an input stream.
     *
     * @return the input stream which contains the body or null if there is not
     * body in this response
     */
    public InputStream getBody() {
        return body;
    }
}
