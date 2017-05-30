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
import com.oracle.jmee.samples.webserver.Status;

/**
 * Handler to show the usage of cookies API of the web server. It allows to
 * authorize users using the post parameters without any protection. User inputs
 * his login and password, sends {@value #LOGIN_NAME} and
 * {@value #PASSWORD_NAME} as post parameters in the HTTP request to access
 * "&lt;context_path&gt/login" resource. Response sets the needed for
 * authorization cookies up.<br><br>
 *
 * After that a request may be sent to access the
 * "&lt;context_path&gt;/checkAuthorization" resource which checks the provided
 * cookies and returns "400 Bad Request" if any of the cookies is missing or
 * "200 OK" with the login information if client is authorized.<br><br>
 *
 * Also it is possible to logout by sending the request to access the
 * "&lt;context_path&gt;/logout" resource. This removes the cookies which are
 * used for authorization.<br><br>
 *
 * Note that this sample handler is intended solely to demonstrate usage of
 * cookies. Such authorization scheme is prone to simple privilege escalation
 * attacks.
 */
public class CookieTestRequestHandler implements RequestHandler {

    // Names for body parameters and cookies
    private static final String LOGIN_NAME = "login";
    private static final String PASSWORD_NAME = "password";

    // Template to generate simple responses
    private static final String RESPONSE_TEMPLATE = "<!DOCTYPE html><html><head><title>%s</title></head><body>%s</body></html>";
    
    private static final String String_format(String _result, String _description)
    {
        StringBuffer ret = new StringBuffer();
        
        ret.append("<!DOCTYPE html><html><head><title>").append(_result).append("</title></head>");
        
        ret.append("<body>").append(_description).append("</body></html>");
        
        return ret.toString();
    }    

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
        // Filtering action by path which is relative to the context path
        if(relativePath.equals("/login")) return login(request);
        else if(relativePath.equals("/checkAuthorization")) return checkAuthorization(request);
        else if(relativePath.equals("/logout")) return logout(request);
        else return HttpResponse.notFound(String_format( "Not found", "Nothing found. Check /login, /checkAuthorization, /logout pages"));
    }

    private HttpResponse login(HttpRequest request) {
        // Retrieving body parameters
        String userLogin = request.getPostParameter(LOGIN_NAME);
        String userPassword = request.getPostParameter(PASSWORD_NAME);
        if (userLogin == null || userPassword == null) {
            return HttpResponse.badRequest(String_format("Operation failed", "Check that both login and password are specified"));
        }

        // Setting the values of the parameters to the cookies
        HttpCookie loginCookie = new HttpCookie(LOGIN_NAME, userLogin);
        // Cookie will be valid for 24 hours
        loginCookie.setMaxAge(24 * 60 * 60);
        loginCookie.setPath("/");

        HttpCookie passwordCookie = new HttpCookie(PASSWORD_NAME, userPassword);
        passwordCookie.setMaxAge(24 * 60 * 60);
        passwordCookie.setPath("/");

        // Generating response which will be sent to the client
        return new HttpResponse(Status.NO_CONTENT).addCookie(loginCookie).addCookie(passwordCookie);
    }

    private HttpResponse checkAuthorization(HttpRequest request) {
        // Getting cookie values if they are present
        String userLogin = request.getHttpCookieValue(LOGIN_NAME);
        String password = request.getHttpCookieValue(PASSWORD_NAME);
        if (userLogin == null || password == null) {
            return HttpResponse.badRequest("You are not logged in").addHeader(HttpResponse.CONTENT_TYPE_HEADER, "text/plain");
        }

        // As an example checking that both login and password are "admin"
        // strings. If it is true, then welcome user as an admin
        if (userLogin.equals("admin") && password.equals("admin")) {
            return HttpResponse.ok("You are administrator").addHeader(HttpResponse.CONTENT_TYPE_HEADER, "text/plain");
        } else {
            // Otherwise welcome user simply by name
            return HttpResponse.ok("You are not an administrator. Hello, " + userLogin).addHeader(HttpResponse.CONTENT_TYPE_HEADER, "text/plain");
        }
    }

    // Request parameter is not used
    private HttpResponse logout(HttpRequest request) {

        HttpCookie loginCookie = new HttpCookie(LOGIN_NAME, "");
        // Setting max age to 0 for a cookie to be deleted
        loginCookie.setMaxAge(0);
        loginCookie.setPath("/");

        HttpCookie passwordCookie = new HttpCookie(PASSWORD_NAME, "");
        passwordCookie.setMaxAge(0);
        passwordCookie.setPath("/");

        return HttpResponse.ok(String_format("Success", "Logout successful")).addCookie(loginCookie).addCookie(passwordCookie);
    }
}
