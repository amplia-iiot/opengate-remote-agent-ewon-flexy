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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
import java.util.NoSuchElementException;
import amplia.util.Objects;
import amplia.util.StringTokenizer;

/**
 * Class which represents an HTTP request received by the web server. It is
 * possible to create an HTTP request by calling
 * {@link #parseRequest(java.lang.String, java.io.InputStream)} method.<br><br>
 *
 * The instances of this class are immutable: setters are not available. However
 * the returned by some getter methods collections may be modified. Defensive
 * copying is not performed to minimize memory consumption. <br><br>
 *
 * For more information about HTTP protocol and HTTP requests please see <a
 * href="http://tools.ietf.org/html/rfc7230">RFC 7230</a>.<br><br>
 *
 * This class differs from the one which is used in the full version of the
 * demo: JSON support has been removed.
 */
public class HttpRequest {

    // Buffer which must be of enough size to fit the request header
    private final static int REQUEST_HEADERS_BUFFER_SIZE = 1024;

    // Maximum length of the body that this server supports
    private static final int MAXIMUM_BODY_LENGTH = 1024 * 1024;

    // Address of the request issuer
    private final String remoteAddress;

    // Exception message for a bad request
    private static final String BAD_REQUEST_EXCEPTION_MESSAGE = "Bad request";

    // Minimum length of the request. This value is determined by the parsing
    // algorithm. It checks four bytes at a time to find header end
    private static final int MINIMUM_REQUEST_LENGTH = 4;

    // Content-Length HTTP Request/Response header
    private static final String CONTENT_LENGTH_HEADER_FIELD = "Content-Length";

    // Map which is used to store header values identified by header names
    // private final Map<String, String> headers = new HashMap<>();
    private final Hashtable headers = new Hashtable(); // String, String

    // List of the cookies that has been passed by the User-Agent
    // private List<HttpCookie> cookies = new ArrayList<>();
    private Vector cookies = new Vector(); // HttpCookie

    // Parameters that have been passed in the URI
    // private final Map<String, String> uriParameters = new HashMap<>();
    private final Hashtable uriParameters = new Hashtable(); // String, String

    // Parameters that have been passed in the request body
    // private final Map<String, String> postParameters = new HashMap<>();
    private final Hashtable postParameters = new Hashtable(); // String, String

    // Flag to check whether body parameters have already been passed
    private boolean postParametersParsed = false;

    // An initially empty array to hold body of the request
    private byte[] body = new byte[0];

    // Request methods which are specified in the RFC 7231
    // https://tools.ietf.org/html/rfc7231#section-4.1
    /**
     * Code for the "OPTIONS" request method.
     *
     * @see #getRequestMethod()
     */
    public static final int OPTIONS_REQUEST_METHOD = 0;

    /**
     * Code for the "GET" request method.
     *
     * @see #getRequestMethod()
     */
    public static final int GET_REQUEST_METHOD = 1;

    /**
     * Code for the "HEAD" request method.
     *
     * @see #getRequestMethod()
     */
    public static final int HEAD_REQUEST_METHOD = 2;

    /**
     * Code for the "POST" request method.
     *
     * @see #getRequestMethod()
     */
    public static final int POST_REQUEST_METHOD = 3;

    /**
     * Code for the "PUT" request method.
     *
     * @see #getRequestMethod()
     */
    public static final int PUT_REQUEST_METHOD = 4;

    /**
     * Code for the "DELETE" request method.
     *
     * @see #getRequestMethod()
     */
    public static final int DELETE_REQUEST_METHOD = 5;

    /**
     * Code for the "TRACE" request method.
     *
     * @see #getRequestMethod()
     */
    public static final int TRACE_REQUEST_METHOD = 6;

    /**
     * Code for the "CONNECT" request method.
     *
     * @see #getRequestMethod()
     */
    public static final int CONNECT_REQUEST_METHOD = 7;

    /**
     * Code for the unknown request method. Use
     * {@link #getRequestMethodAsString()} to get the specified request method.
     *
     * @see #getRequestMethod()
     */
    public static final int UNKNOWN_REQUEST_METHOD = 8;

    // Value that encodes the used request method 
    private int requestMethod = UNKNOWN_REQUEST_METHOD;

    // HTTP version request part
    private String httpVersion;

    // String version of the HTTP request method 
    private String httpMethod;

    // The requested URI
    private String requestUri;

    // The path that has been requested
    private String requestPath;

    // Parsed Content-Length header value
    private int contentLength;

    // Constructor is made private because there is nothing to override since
    // all object creation logic is contained in the static parsing methods
    private HttpRequest(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    /**
     * Returns the address of the request sender. It has the &lt;IP address or
     * domain name&gt;:&lt;port&gt; format.
     *
     * @return string containing IP address or domain name and a port of the
     * request sender.
     */
    public String getRemoteAddress() {
        return remoteAddress;
    }

    /**
     * Returns the code of the request method. See constants defined by this
     * class for available values.
     *
     * @return code of the request method
     * @see #getRequestMethodAsString()
     */
    public int getRequestMethod() {
        return requestMethod;
    }

    /**
     * Returns the request method as it has been specified in the request. This
     * can be useful in case {@link #getRequestMethod()} returns
     * {@link #UNKNOWN_REQUEST_METHOD}.
     *
     * @return string with request method as it has been specified in the
     * request
     * @see #getRequestMethod()
     */
    public String getRequestMethodAsString() {
        return httpMethod;
    }

    /**
     * Returns the URI reference as a string that has been specified in the
     * request. It can be both absolute or relative. For more information about
     * possible URI reference formats see <a
     * href="http://tools.ietf.org/html/rfc7230#section-2.7">RFC 7230</a>.
     *
     * @return URI reference specified in the request
     */
    public String getRequestUri() {
        return requestUri;
    }

    /**
     * Returns path component of the URI reference that has been specified in
     * the request.
     *
     * @return the path component of the requested URI reference
     */
    public String getRequestPath() {
        return requestPath;
    }

    /**
     * Returns the set of names of the headers which were specified in the
     * request. The removal of set elements is reflected in all header related
     * methods.
     *
     * @return the set which contains the names of the headers
     */
    public Enumeration getHeadersNames() {
        return headers.keys();
    }

    /**
     * Returns the value of the header identified by the name.
     *
     * @param headerName name of the header to get the value
     * @return value of the header or null if it is absent
     */
    public String getHeaderValue(String headerName) {
        return (String)headers.get(headerName);
    }

    /**
     * Returns the map which contains values of all headers identified by header
     * names. Changes made to this map are reflected in other headers-related
     * methods.
     *
     * @return the map with header name and value pairs
     */
    public Hashtable getHeaders() {
        return headers;
    }

    /**
     * Returns the set of URI parameter names which were specified in the
     * request. The removal of set elements is reflected in all URI parameter-
     * related methods.
     *
     * @return the set which contains the names of the URI parameters
     */
    public Enumeration getUriParametersNames() {
        return uriParameters.keys();
    }

    /**
     * Returns the map which contains values of all URI parameters identified by
     * parameter names. Changes made to this map are reflected in other URI
     * parameter-related methods.
     *
     * @return the map with URI parameter name and value pairs
     */
    public Hashtable getUriParameters() {
        return uriParameters;
    }

    /**
     * Returns the value of the URI parameter identified by the name.
     *
     * @param parameterName name of the URI parameter to get the value
     * @return value of the URI parameter or null if it is absent
     */
    public String getUriParameter(String parameterName) {
        return (String)uriParameters.get(parameterName);
    }

    /**
     * Returns the HTTP version string that has been specified in the request,
     * e.g. "HTTP/1.1".
     *
     * @return the string containing used in the request HTTP version
     */
    public String getHttpVersion() {
        return httpVersion;
    }

    /**
     * Returns a list of the HTTP cookies provided in the "Cookie" header of the
     * request. Changes made to either list or to the cookies contained in it
     * are reflected in other cookie-related methods.
     *
     * @return list with the cookies that were provided in the request
     */
    public Vector getHttpCookies() {
        return cookies;
    }

    /**
     * Returns the HTTP cookie with the specified name provided in the "Cookie"
     * header of the request.
     *
     * @param cookieName name of the HTTP cookie, must not be null
     * @return the cookie with the specified name or null if it is absent
     * @throws NullPointerException if name of the cookie is null
     */
    public HttpCookie getHttpCookie(String cookieName) {
        Objects.requireNonNull(cookieName, "Cookie name must not be null");
        for(Enumeration e = cookies.elements();e.hasMoreElements();)
        {
            HttpCookie cookie = (HttpCookie)e.nextElement();
            if (cookie.getName().equalsIgnoreCase(cookieName)) {
                return cookie;
            }            
        }
        return null;
    }

    /**
     * Returns the value of the HTTP cookie with the specified name provided in
     * the "Cookie" header of the request.
     *
     * @param cookieName name of the HTTP cookie, must not be null
     * @return value of the HTTP cookie, or null if there is no cookie with the
     * specified name
     * @throws NullPointerException if name of the cookie is null
     */
    public String getHttpCookieValue(String cookieName) {
        Objects.requireNonNull(cookieName, "Cookie name must not be null");
        for(Enumeration e = cookies.elements();e.hasMoreElements();)
        {
            HttpCookie cookie = (HttpCookie)e.nextElement();
            if (cookie.getName().equalsIgnoreCase(cookieName)) {
                return cookie.getValue();
            }          
        }
        return null;
    }

    /**
     * Returns the set of body parameter names which were specified in the
     * request. The removal of set elements is reflected in all body parameter-
     * related methods.
     *
     * @return the set which contains the names of the body parameters
     */
    public Enumeration getPostParametersNames() {
        if (!postParametersParsed) {
            parsePostParameters(new String(body), postParameters);
            postParametersParsed = true;
        }
        return postParameters.keys();
    }

    /**
     * Returns the value of the body parameter identified by the name.
     *
     * @param parameterName name of the body parameter to get the value
     * @return value of the body parameter or null if it is absent
     * @throws NullPointerException if parameter name is null
     */
    public String getPostParameter(String parameterName) {
        Objects.requireNonNull(parameterName, "Parameter name must not be null");

        if (!postParametersParsed) {
            parsePostParameters(new String(body), postParameters);
            postParametersParsed = true;
        }
        return (String)postParameters.get(parameterName);
    }

    /**
     * Returns the map which contains values of all body parameters identified
     * by parameter names. Changes made to this map are reflected in other body
     * parameter-related methods.
     *
     * @return the map with body parameter name and value pairs
     */
    public Hashtable getPostParameters() {
        if (!postParametersParsed) {
            parsePostParameters(new String(body), postParameters);
            postParametersParsed = true;
        }
        return postParameters;
    }

    /**
     * Returns request body as an array of bytes. An empty array (zero length)
     * is returned if the request does not contain a body.
     *
     * @return array of bytes which contains request body
     */
    public byte[] getRequestBody() {
        return body;
    }

    /**
     * Returns request body as a string. An empty string is returned if the
     * request does not contain a body.
     *
     * @return string which contains request body
     */
    public String getRequestBodyAsString() {
        return new String(body);
    }

    /**
     * Returns true if this request proposes server to persist the connection
     * which was used to deliver the request. The process of deciding is stated
     * in the <a
     * href="https://tools.ietf.org/html/rfc7230#section-6.3">Persistence
     * paragraph of the RFC 7230</a>.
     *
     * @return true if the request suggests to persist the connection, false if
     * user agent suggests to close the connection after the request has been
     * served and response has been sent
     */
    public boolean shouldPersistConnection() {
        // Deciding whether to persist this connection according to the
        // algorithm which is described in the RFC 7230 6.3 Persistence
        // paragraph
        String connectionOptions = (String)headers.get("Connection");
        if (connectionOptions == null) {
            connectionOptions = "";
        } else {
            connectionOptions = connectionOptions.toLowerCase();
        }
        // In any case if "close" connection option is specified, connection
        // should not be persisted
        if (connectionOptions.indexOf("close")>=0) {
            return false;
        } else {
            // Connections are persisted by default when HTTP/1.1 is used. Also
            // connection should be persisted if HTTP/1.0 request has
            // "keep-alive" connection option set
            return "HTTP/1.1".equals(httpVersion) || ("HTTP/1.0".equals(httpVersion) && (connectionOptions.indexOf("keep-alive")>=0));
        }
    }

    /**
     * Parses the HTTP request contained in the provided input stream and
     * returns its object representation. A default size of the receiving buffer
     * is used.
     *
     * @param remoteAddress an address of the request sender. It should contain
     * IP address or domain name and a port delimited with ':', e.g
     * "127.0.0.1:56789".
     * @param inputStream an input stream containing request to parse
     * @return an object which represents the received request
     * @throws IOException if any I/O error occurs during request parsing
     * @throws NullPointerException if remote address or input stream is null
     * @throws IllegalArgumentException if the input stream contains malformed
     * request
     */
    public static HttpRequest parseRequest(String remoteAddress, InputStream inputStream) throws IOException {
        return parseRequest(remoteAddress, inputStream, REQUEST_HEADERS_BUFFER_SIZE);
    }

    /**
     * Parses the HTTP request contained in the provided input stream using the
     * specified length of the buffer and returns its object representation.
     * This method allows to configure the size of the buffer to read all
     * request headers. Size must be big enough to fit request body. This method
     * will allocate buffer of an appropriate length specially for request body.
     *
     * @param remoteAddress an address of the request sender. It should contain
     * IP address or domain name and a port delimited with ':', e.g
     * "127.0.0.1:56789".
     * @param inputStream an input stream containing request to parse
     * @param receivingBufferSize the size of buffer. Must be positive. Make
     * sure that it is big enough to fit all request headers.
     * @return an object which represents the received request
     * @throws IOException if any I/O error occurs during request parsing
     * @throws NullPointerException if remote address or input stream is null
     * @throws IllegalArgumentException <ul><li>
     * if the input stream contains malformed request</li>
     * <li>if the receiving buffer size was not big enough to parse the
     * request</li>
     * <li>if the receiving buffer size is non-positive</li></ul>
     */
    public static HttpRequest parseRequest(String remoteAddress, InputStream inputStream, int receivingBufferSize) throws IOException {
        Objects.requireNonNull(remoteAddress, "Remote address must not be null");
        Objects.requireNonNull(inputStream, "Input stream must not be null");
        if (receivingBufferSize <= 0) {
            throw new IllegalArgumentException("Illegal buffer size. Must be positive. Specified: " + receivingBufferSize);
        }

        byte[] buffer = new byte[receivingBufferSize];
        int bufferOffset = 0;
        int bytesRead;
        int bodyStartIndex = -1;
        int lineNumber = 0;
        int lineStartIndex = 0;
        int startParsingIndex = 0;

        HttpRequest request = new HttpRequest(remoteAddress);
        // Reading either full request or the part that should ideally contain
        // all headers. If headers do not fit the provided buffer an exception
        // it thrown further
        // -1 is returned when socket is closed
        while ((bytesRead = inputStream.read(buffer, bufferOffset, buffer.length - bufferOffset)) != -1) {

            if (bytesRead == 0) {
                // End of buffer has been reached. This request cannot be
                // handled: it is too long for such server buffer size to parse.
                // This implementation does not use any buffer cycling strategy.
                break;
            }

            bufferOffset += bytesRead;
            // Too little bytes were read to parse request
            if (bufferOffset <= MINIMUM_REQUEST_LENGTH) {
                continue;
            }

            // It is possible that next read is going to block (full request has
            // been read, but connection is still alive), because -1 will
            // be returned by read only when the client socket is closed. 
            // By now bufferOffset contains number of data bytes that were read
            for (int byteIndex = startParsingIndex; byteIndex < bufferOffset; byteIndex++) {

                // Note: this implementation only parses correctly requests with
                // \r\n end of line characters.
                // Checking that we are on the end of line and that it is not the
                // last CRLF which delimits header from body
                if (buffer[byteIndex] == '\n' && buffer[byteIndex - 1] == '\r' && (buffer[byteIndex - 2] != '\n' && buffer[byteIndex - 3] != '\r')) {

                    // Extracting line from the buffer. Line starts where prevoius
                    // line ended and ends before new line characters.
                    int endOfLineIndex = byteIndex - 2;

                    String line = new String(buffer, lineStartIndex, endOfLineIndex - lineStartIndex + 1);
                    // Changing index which points to the start of the next line
                    lineStartIndex = byteIndex + 1;
                    if (lineNumber == 0) {
                        // Parsing http method, uri, version
                        parseRequestLine(request, line);
                    } else {
                        // Parsing line as header
                        parseRequestHeader(request, line);
                    }
                    lineNumber++;
                }

                // Trying to find end of headers
                if (buffer[byteIndex] == '\n' && buffer[byteIndex - 1] == '\r'
                    && buffer[byteIndex - 2] == '\n' && buffer[byteIndex - 3] == '\r') {
                    bodyStartIndex = byteIndex + 1;
                    break;
                }
            }

            // Marking that next time parsing should be started from the current
            // position
            startParsingIndex = bufferOffset;

            if (bodyStartIndex != -1) {
                // End of headers has been found
                break;
            }
        }

        if (bufferOffset <= MINIMUM_REQUEST_LENGTH) {
            throw new IllegalArgumentException(BAD_REQUEST_EXCEPTION_MESSAGE + ": request is too short. Length is " + bufferOffset);
        }

        // This means that end of headers has not been found due to malformed
        // request or end of headers exceeds the server buffer size
        if (bodyStartIndex == -1) {
            throw new IllegalArgumentException(BAD_REQUEST_EXCEPTION_MESSAGE + ": failed to find end of headers");
        }

        if (request.contentLength > MAXIMUM_BODY_LENGTH) {
            throw new IllegalArgumentException("Entity is too large: " + request.contentLength);
        }

        if (request.contentLength > 0) {
            // Filling body from the request
            request.body = new byte[request.contentLength];
            fillBody(request, bufferOffset, bodyStartIndex, buffer, inputStream);
        }

        return request;
    }

    private static void fillBody(HttpRequest request, int bufferOffset, int bodyStartIndex, byte[] buffer, InputStream inputStream) throws IOException, IllegalArgumentException {
        int bytesRead;
        int totalRead;

        // bufferOffset points to the position inside an array to start next
        // reading, so the last element to copy is contained in the position
        // bufferOffset - 1. The first element to copy is contained at the
        // bodyStartIndex position. Then it is required to copy
        // <end> - <start> + 1 elements. Expression is not optimized for
        // clarity
        int firstPartOfBodyLength = (bufferOffset - 1) - bodyStartIndex + 1;
        // Filling part that has been read during headers reading
        System.arraycopy(buffer, bodyStartIndex, request.body, 0, firstPartOfBodyLength);
        totalRead = firstPartOfBodyLength;
        // Checking that there is some data inside the stream which has not
        // been read
        // Filling part that is currently inside the stream (if any)
        while (totalRead != request.contentLength && (bytesRead = inputStream.read(request.body, totalRead, request.contentLength - totalRead)) != -1) {
            totalRead += bytesRead;
        }

        // Currently totalRead contains number of bytes that were read
        // totally
        // Checking that needed amount of data has been read
        if (totalRead != request.contentLength) {
            throw new IllegalArgumentException(BAD_REQUEST_EXCEPTION_MESSAGE + "Wrong amount of data: content length: " + request.contentLength + " read: " + totalRead);
        }
    }

    private static void parseRequestLine(HttpRequest request, String line) {
        StringTokenizer tokenizer = new StringTokenizer(line);
        try {
            // Extracting and parsing needed information from request line
            request.httpMethod = tokenizer.nextToken();
            request.requestMethod = parseHttpMethod(request.httpMethod);

            request.requestUri = decodeUri(tokenizer.nextToken());
            parseRequestUri(request);

            request.httpVersion = tokenizer.nextToken();
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException(BAD_REQUEST_EXCEPTION_MESSAGE + ": Failed to parse request line: " + line);
        }
    }

    private static int parseHttpMethod(String method) {
        if(method.equals("GET")) return GET_REQUEST_METHOD;
        else if(method.equals("POST")) return POST_REQUEST_METHOD;
        else if(method.equals("PUT")) return PUT_REQUEST_METHOD;
        else if(method.equals("DELETE")) return DELETE_REQUEST_METHOD;
        else if(method.equals("HEAD")) return HEAD_REQUEST_METHOD;
        else if(method.equals("OPTIONS")) return OPTIONS_REQUEST_METHOD;
        else if(method.equals("TRACE")) return TRACE_REQUEST_METHOD;
        else if(method.equals("CONNECT")) return CONNECT_REQUEST_METHOD;
        else return UNKNOWN_REQUEST_METHOD;
    }

    private static void parseRequestUri(HttpRequest request) {
        // Extracting parameters from the query part of the URI if they are 
        // present. Also extracting relative request path 
        int parametersStartIndex = request.requestUri.indexOf('?');
        if (parametersStartIndex >= 0) {
            int parametersEndIndex = request.requestUri.indexOf('#', parametersStartIndex);
            String parameters;
            if (parametersEndIndex >= 0) {
                parameters = request.requestUri.substring(parametersStartIndex + 1, parametersEndIndex);
            } else {
                parameters = request.requestUri.substring(parametersStartIndex + 1);
            }
            parseGetParameters(parameters, request.uriParameters);
            request.requestPath = request.requestUri.substring(0, parametersStartIndex);
        } else {
            request.requestPath = request.requestUri;
        }

        // Removing schema and authority part if they are present
        if (request.requestPath.startsWith("http://")) {
            int endOfAuthority = request.requestPath.indexOf('/', "http://".length());
            if (endOfAuthority >= 0) {
                request.requestPath = request.requestUri.substring(endOfAuthority);
            }

            // In case URI do not have a relative path or ends with '/'
            // character, default "/" path is used
            if (endOfAuthority < 0) {
                request.requestPath = "/";
            }
        }
    }

    private static String decodeUri(String s) {
        StringBuffer builder = new StringBuffer();
        // Converting encoded characters to their original representation
        for (int charIndex = 0; charIndex < s.length(); charIndex++) {
            char character = s.charAt(charIndex);
            if (character == '+') {
                builder.append(' ');
            } else if (character == '%') {
                // Need to substitute hexadecimals with a character
                if (charIndex + 2 < s.length()) {
                    builder.append((char) Integer.parseInt(s.substring(charIndex + 1, charIndex + 3), 16));
                    charIndex += 2;
                } else {
                    // does not contain enough chars
                    builder.append(character);
                }
            } else {
                builder.append(character);
            }
        }
        return builder.toString();
    }

    private static void parseRequestHeader(HttpRequest request, String line) {
        int delimiter = line.indexOf(':');

        if (delimiter > 0) {
            String key = line.substring(0, delimiter).trim();
            String value = line.substring(delimiter + 1, line.length()).trim();

            // If there is a "Cookie" header, then this header value is parsed
            // to get cookies
            if (key.equalsIgnoreCase("cookie")) {
                request.cookies = HttpCookie.fromCookieHeader(value);
            } else {
                // Checking whether the value of a header has a special meaning
                checkForPredefinedHeaders(request, key, value);
                request.headers.put(key, value);
            }
        } else {
            throw new IllegalArgumentException("Header name and value are not separated with a colon: " + line);
        }
    }

    private static void checkForPredefinedHeaders(HttpRequest request, String key, String value) {
        
        if(key.equals(CONTENT_LENGTH_HEADER_FIELD))
        {
            // Parsing Content-Length header to get length of the body
            try {
                request.contentLength = Integer.parseInt(value);
                if (request.contentLength < 0) {
                    throw new IllegalArgumentException(BAD_REQUEST_EXCEPTION_MESSAGE + ": Content length is negative: " + value);
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(BAD_REQUEST_EXCEPTION_MESSAGE + ": Content length is not a number: " + value);
            }            
        } else
        {
            // It is not the predefined one. skipping it
        }
    }

    private static void parsePostParameters(String postData, Hashtable postParameters) {
        parseParametersToMap(postData, postParameters);
    }

    private static void parseGetParameters(String parametersString, Hashtable uriParameters) {
        parseParametersToMap(parametersString, uriParameters);
    }

    private static void parseParametersToMap(String parametersString, Hashtable parsedParameters) {
        // Do nothing if parameters string is empty
        if (parametersString == null || parametersString.length() == 0) {
            return;
        }

        String tokenAssign = "=";
        String tokenAnd = "&";

        StringTokenizer parameterTokenizer = new StringTokenizer(parametersString, tokenAnd);
        while (parameterTokenizer.hasMoreTokens()) {
            String parameterValue = parameterTokenizer.nextToken();
            int parameterAssignSignPosition = parameterValue.indexOf(tokenAssign);
            if (parameterAssignSignPosition > 0) {
                String parameter = parameterValue.substring(0, parameterAssignSignPosition);
                String value = parameterValue.substring(parameterAssignSignPosition + 1, parameterValue.length());
                parsedParameters.put(parameter, value);
            }
            // Otherwise skipping the token
        }
    }
}
