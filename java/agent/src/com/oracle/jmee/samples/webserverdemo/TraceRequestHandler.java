/*
 * Copyright © 2015, Oracle and/or its affiliates.  All rights reserved. 
 *
 * This software is dual-licensed to you under the MIT License (MIT) and the
 * Universal Permissive License (UPL).  See the LICENSE file in the root directory
 * for license terms.  You may choose either license, or both.
 */
package com.oracle.jmee.samples.webserverdemo;

import com.oracle.jmee.samples.webserver.HttpCookie;
import com.oracle.jmee.samples.webserver.HttpRequest;
import com.oracle.jmee.samples.webserver.HttpResponse;
import com.oracle.jmee.samples.webserver.RequestHandler;

import java.util.Vector;
import java.util.Enumeration;

/*
import java.util.List;
import java.util.Set;
*/

/**
 * Handler to show the information available in the request. It serves all the
 * requests by printing information about them. It also outputs the gathered
 * information to the standard output log. This handler can be tested by
 * accessing any path starting with the registered context path.
 */
public class TraceRequestHandler implements RequestHandler {

    private static final String NEW_LINES_CHARACTERS = "\r\n";

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
        // Gathering information about request in the string builder
        StringBuffer info = new StringBuffer();
        info.append("Handling request from ").append(request.getRemoteAddress()).append(": ").append(NEW_LINES_CHARACTERS);
        info.append("\tVersion: ").append(request.getHttpVersion()).append(NEW_LINES_CHARACTERS);
        info.append("\tRequested URI: ").append(request.getRequestUri()).append(NEW_LINES_CHARACTERS);
        info.append("\tRequested path: ").append(request.getRequestPath()).append(NEW_LINES_CHARACTERS);
        info.append("\tContext path: ").append(contextPath).append(NEW_LINES_CHARACTERS);
        info.append("\tRelative to the context path: ").append(relativePath).append(NEW_LINES_CHARACTERS);
        info.append("\tUsed method: ").append(request.getRequestMethodAsString()).append(NEW_LINES_CHARACTERS);

        // Gathering information about headers
        Enumeration headersNamesEnumeration = request.getHeadersNames();
        if (headersNamesEnumeration.hasMoreElements()) {
            info.append(NEW_LINES_CHARACTERS);
            info.append("Headers: ").append(NEW_LINES_CHARACTERS);
            
            for (;headersNamesEnumeration.hasMoreElements();) 
            {
                String headerName = (String)headersNamesEnumeration.nextElement();
                info.append('\t').append(headerName).append(": ").append(request.getHeaderValue(headerName)).append(NEW_LINES_CHARACTERS);
            }
        }

        // Gathering information about URI parameters
        Enumeration uriParametersNamesEnumeration = request.getUriParametersNames();
        if (uriParametersNamesEnumeration.hasMoreElements()) {
            info.append(NEW_LINES_CHARACTERS);
            info.append("GET parameters: ").append(NEW_LINES_CHARACTERS);
            for (;uriParametersNamesEnumeration.hasMoreElements();) 
            {
                String parameterName = (String)uriParametersNamesEnumeration.nextElement();
                info.append('\t').append(parameterName).append("=").append(request.getUriParameter(parameterName)).append(NEW_LINES_CHARACTERS);
            }
        }

        // Gathering information about body parameters
        Enumeration postParametersNamesEnumeration = request.getPostParametersNames();
        if (postParametersNamesEnumeration.hasMoreElements()) {
            info.append(NEW_LINES_CHARACTERS);
            info.append("POST parameters: ").append(NEW_LINES_CHARACTERS);
            for (;postParametersNamesEnumeration.hasMoreElements();) 
            {
                String parameterName = (String)postParametersNamesEnumeration.nextElement();
                info.append('\t').append(parameterName).append("=").append(request.getPostParameter(parameterName)).append(NEW_LINES_CHARACTERS);
            }
        }

        // Gathering information about cookies
        Vector httpCookies = request.getHttpCookies();
        if (httpCookies.size() > 0) {
            info.append(NEW_LINES_CHARACTERS);
            info.append("Cookies: ").append(NEW_LINES_CHARACTERS);
            for (Enumeration cookiesEnumeration = httpCookies.elements();cookiesEnumeration.hasMoreElements();) {
                HttpCookie cookie = (HttpCookie)cookiesEnumeration.nextElement();
                info.append('\t').append(cookie).append(NEW_LINES_CHARACTERS);
            }
        }

        // Appending request body
        String requestBodyAsString = request.getRequestBodyAsString();
        if (requestBodyAsString != null && requestBodyAsString.length() > 0) {
            info.append(NEW_LINES_CHARACTERS);
            info.append("Body:").append(NEW_LINES_CHARACTERS);
            info.append(requestBodyAsString).append(NEW_LINES_CHARACTERS);
        }
        System.out.println(info.toString());

        // Sending response with the collection information as a plain text
        return HttpResponse.ok(info.toString()).setContentType("text/plain; charset=utf-8");
    }

}
