/*
 * Copyright © 2015, Oracle and/or its affiliates.  All rights reserved. 
 *
 * This software is dual-licensed to you under the MIT License (MIT) and the
 * Universal Permissive License (UPL).  See the LICENSE file in the root directory
 * for license terms.  You may choose either license, or both.
 */
package com.oracle.jmee.samples.webserver;

import java.util.Vector;
import java.util.Enumeration;

/**
 * Status codes which are specified in the <a
 * href="https://tools.ietf.org/html/rfc7231">RFC 7231</a>.
 */
public class Status {
    
    private static final Vector ms_statusList = new Vector();

    // Description of codes according to the http://tools.ietf.org/html/rfc7231#section-6.1
    /**
     * Status which indicates that the initial part of a request has been
     * received and has not yet been rejected by the server. The server intends
     * to send a final response after the request has been fully received and
     * acted upon.
     */
    public static final Status CONTINUE = new Status(100, "Continue");
    /**
     * Status which indicates that the server understands and is willing to
     * comply with the client's request, via the Upgrade header field, for a
     * change in the application protocol being used on this connection.
     */
    public static final Status SWITCHING_PROTOCOLS = new Status(101, "Switching Protocols");
    /**
     * Status which indicates that the request has succeeded.
     */
    public static final Status OK = new Status(200, "OK");
    /**
     * Status which indicates that the request has been fulfilled and has
     * resulted in one or more new resources being created.
     */
    public static final Status CREATED = new Status(201, "Created");
    /**
     * Status which indicates that the request has been accepted for processing,
     * but the processing has not been completed yet.
     */
    public static final Status ACCEPTED = new Status(202, "Accepted");
    /**
     * Status which indicates that the request was successful but the enclosed
     * payload has been modified from 200 (OK) response of the origin server by
     * a transforming proxy.
     */
    public static final Status NON_AUTHORATIVE_INFORMATION = new Status(203, "Non-Authoritative Information");
    /**
     * Status which indicates that the server has successfully fulfilled the
     * request and that there is no additional content to send in the response
     * payload body.
     */
    public static final Status NO_CONTENT = new Status(204, "No Content");
    /**
     * Status which indicates that the server has fulfilled the request and
     * desires that the user agent reset the "document view", which caused the
     * request to be sent, to its original state as received from the origin
     * server.
     */
    public static final Status RESET_CONTENT = new Status(205, "Reset Content");
    /**
     * Status which indicates that the server is successfully fulfilling a range
     * request for the target resource by transferring one or more parts of the
     * selected representation that correspond to the satisfiable ranges found
     * in Range header field of the request.
     */
    public static final Status PARTIAL_CONTENT = new Status(206, "Partial Content");
    /**
     * Status which indicates that the target resource has more than one
     * representation, each with its own more specific identifier, and
     * information about the alternatives is being provided so that the user (or
     * user agent) can select a preferred representation by redirecting its
     * request to one or more of those identifiers.
     */
    public static final Status MULTIPLE_CHOICES = new Status(300, "Multiple Choices");
    /**
     * Status which indicates that the target resource has been assigned a new
     * permanent URI and any future references to this resource ought to use one
     * of the enclosed URIs.
     */
    public static final Status MOVED_PERMANENTLY = new Status(301, "Moved Permanently");
    /**
     * Status which indicates that the target resource resides temporarily under
     * a different URI.
     */
    public static final Status FOUND = new Status(302, "Found");
    /**
     * Status which indicates that the server is redirecting the user agent to a
     * different resource, as indicated by a URI in the Location header field,
     * which is intended to provide an indirect response to the original
     * request.
     */
    public static final Status SEE_OTHER = new Status(303, "See Other");
    /**
     * Status which indicates that a conditional GET or HEAD request has been
     * received and would have resulted in a 200 (OK) response if it were not
     * for the fact that the condition evaluated to false.
     */
    public static final Status NOT_MODIFIED = new Status(304, "Not Modified");
    /**
     * Status which is now deprecated.
     */
    public static final Status USE_PROXY = new Status(305, "Use Proxy");
    /**
     * Status which indicates that the target resource resides temporarily under
     * a different URI and the user agent MUST NOT change the request method if
     * it performs an automatic redirection to that URI. Since the redirection
     * can change over time, the client ought to continue using the original
     * effective request URI for future requests.
     */
    public static final Status TEMPORARY_REDIRECT = new Status(307, "Temporary Redirect");
    /**
     * Status which indicates that the server cannot or will not process the
     * request due to something that is perceived to be a client error (e.g.,
     * malformed request syntax, invalid request message framing, or deceptive
     * request routing).
     */
    public static final Status BAD_REQUEST = new Status(400, "Bad Request");
    /**
     * Status which indicates that the request has not been applied because it
     * lacks valid authentication credentials for the target resource.
     */
    public static final Status UNAUTHORIZED = new Status(401, "Unauthorized");
    /**
     * Status which is reserved for future use.
     */
    public static final Status PAYMENT_REQUIRED = new Status(402, "Payment Required");
    /**
     * Status which indicates that the server understood the request but refuses
     * to authorize it.
     */
    public static final Status FORBIDDEN = new Status(403, "Forbidden");
    /**
     * Status which indicates that the origin server did not find a current
     * representation for the target resource or is not willing to disclose that
     * one exists.
     */
    public static final Status NOT_FOUND = new Status(404, "Not Found");
    /**
     * Status which indicates that the method received in the request-line is
     * known by the origin server but not supported by the target resource.
     */
    public static final Status METHOD_NOT_ALLOWED = new Status(405, "Method Not Allowed");
    /**
     * Status indicates that the target resource does not have a current
     * representation that would be acceptable to the user agent, according to
     * the proactive negotiation header fields received in the request, and the
     * server is unwilling to supply a default representation.
     */
    public static final Status NOT_ACCEPTABLE = new Status(406, "Not Acceptable");
    /**
     * Status is similar to 401 (Unauthorized), but it indicates that the client
     * needs to authenticate itself in order to use a proxy.
     */
    public static final Status PROXY_AUTHENTICATION_REQUIRED = new Status(407, "Proxy Authentication Required");
    /**
     * Status which indicates that the server did not receive a complete request
     * message within the time that it was prepared to wait.
     */
    public static final Status REQUEST_TIMEOUT = new Status(408, "Request Timeout");
    /**
     * Status indicates that the request could not be completed due to a
     * conflict with the current state of the target resource.
     */
    public static final Status CONFLICT = new Status(409, "Conflict");
    /**
     * Status which indicates that access to the target resource is no longer
     * available at the origin server and that this condition is likely to be
     * permanent.
     */
    public static final Status GONE = new Status(410, "Gone");
    /**
     * Status indicates that the server refuses to accept the request without a
     * defined Content-Length.
     */
    public static final Status LENGTH_REQUIRED = new Status(411, "Length Required");
    /**
     * Status which indicates that one or more conditions given in the request
     * header fields evaluated to false when tested on the server.
     */
    public static final Status PRECONDITION_FAILED = new Status(412, "Precondition Failed");
    /**
     * Status which indicates that the server is refusing to process a request
     * because the request payload is larger than the server is willing or able
     * to process.
     */
    public static final Status PAYLOAD_TOO_LARGE = new Status(413, "Payload Too Large");
    /**
     * Status which indicates that the server is refusing to service the request
     * because the request-target is longer than the server is willing to
     * interpret.
     */
    public static final Status URI_TOO_LONG = new Status(414, "URI Too Long");
    /**
     * Status which indicates that the origin server is refusing to service the
     * request because the payload is in a format not supported by this method
     * on the target resource.
     */
    public static final Status UNSUPPORTED_MEDIA_TYPE = new Status(415, "Unsupported Media Type");
    /**
     * Status which indicates that none of the ranges in the request's Range
     * header field overlap the current extent of the selected resource or that
     * the set of ranges requested has been rejected due to invalid ranges or an
     * excessive request of small or overlapping ranges.
     */
    public static final Status RANGE_NOT_SATISFIABLE = new Status(416, "Range Not Satisfiable");
    /**
     * Status which indicates that the expectation given in the request's Expect
     * header field could not be met by at least one of the inbound servers.
     */
    public static final Status EXPECTATION_FAILED = new Status(417, "Expectation Failed");
    /**
     * Status which indicates that the server refuses to perform the request
     * using the current protocol but might be willing to do so after the client
     * upgrades to a different protocol.
     */
    public static final Status UPGRADE_REQUIRED = new Status(426, "Upgrade Required");
    /**
     * Status which indicates that the server encountered an unexpected
     * condition that prevented it from fulfilling the request.
     */
    public static final Status INTERNAL_SERVER_ERROR = new Status(500, "Internal Server Error");
    /**
     * Status which indicates that the server does not support the functionality
     * required to fulfill the request.
     */
    public static final Status NOT_IMPLEMENTED = new Status(501, "Not Implemented");
    /**
     * Status which indicates that the server, while acting as a gateway or
     * proxy, received an invalid response from an inbound server it accessed
     * while attempting to fulfill the request.
     */
    public static final Status BAD_GATEWAY = new Status(502, "Bad Gateway");
    /**
     * Status which indicates that the server is currently unable to handle the
     * request due to a temporary overload or scheduled maintenance, which will
     * likely be alleviated after some delay.
     */
    public static final Status SERVICE_UNAVAILABLE = new Status(503, "Service Unavailable");
    /**
     * Status which indicates that the server, while acting as a gateway or
     * proxy, did not receive a timely response from an upstream server it
     * needed to access in order to complete the request.
     */
    public static final Status GATEWAY_TIMEOUT = new Status(504, "Gateway Timeout");
    /**
     * Status which indicates that the server does not support, or refuses to
     * support, the major version of HTTP that was used in the request message.
     */
    public static final Status HTTP_VERSION_NOT_SUPPORTED = new Status(505, "HTTP Version Not Supported");

    // Status code
    private final int code;
    // Status reason
    private final String reason;

    /**
     * Creates a status instance by specified status code and status reason.
     *
     * @param code status code
     * @param reason status reason
     */
    Status(int code, String reason) {
        this.code = code;
        this.reason = reason;
        
        ms_statusList.addElement(this);
        
    }

    /**
     * Returns the status code of the status
     *
     * @return the status code
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns the status reason of the status
     *
     * @return the status reason
     */
    public String getReason() {
        return reason;
    }

    /**
     * Gets the status by the specified status code.
     *
     * @param code status code
     * @return the status with the specified code or null if there is no such
     * predefined status
     */
    public static Status getByCode(int code) {        
        for (Enumeration en=ms_statusList.elements();en.hasMoreElements();)
        {
            Status status = (Status)en.nextElement();
            if(status.getCode() == code)
            {
                return status;
            }
        }
        return null;
    }

    /**
     * Gets the status by the specified status reason.
     *
     * @param reason status reason
     * @return the status with the specified reason or null if there is no such
     * predefined status
     */
    public static Status getByReason(String reason) {
        for (Enumeration en=ms_statusList.elements();en.hasMoreElements();)
        {
            Status status = (Status)en.nextElement();
            if(status.getReason().equals(reason))
            {
                return status;
            }
        }
        return null;
    }
}
