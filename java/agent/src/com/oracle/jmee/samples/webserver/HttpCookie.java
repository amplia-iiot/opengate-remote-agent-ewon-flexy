/*
 * Copyright © 2015, Oracle and/or its affiliates.  All rights reserved. 
 *
 * This software is dual-licensed to you under the MIT License (MIT) and the
 * Universal Permissive License (UPL).  See the LICENSE file in the root directory
 * for license terms.  You may choose either license, or both.
 */
package com.oracle.jmee.samples.webserver;

import com.oracle.jmee.samples.webserver.util.DateUtils;
import java.util.NoSuchElementException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
// import java.util.ArrayList;
import java.util.Date;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
import amplia.util.Objects;
import amplia.util.StringTokenizer;

/**
 * Class which represents an HTTP cookie which is defined by the <a
 * href="http://tools.ietf.org/html/rfc6265">RFC 6265 "HTTP State Management
 * Mechanism"</a>. Cookie contains a name, a value and a set of attributes,
 * which describe how and when it should be used.<br><br>
 *
 * There are multiple ways to obtain {@link HttpCookie} objects:<ul>
 * <li>Create a new cookie using the constructor</li>
 * <li>Create a list of cookies supplied by the client in the HTTP communication
 * chain by providing {@value #COOKIE_HEADER} header in the HTTP request. Use
 * the {@link #fromCookieHeader(java.lang.String)} method in this case.</li>
 * <li>Create a cookie supplied by the HTTP server in the HTTP communication
 * chain by providing {@value #SET_COOKIE_HEADER} header in the HTTP response.
 * Use the {@link #fromString(java.lang.String)} method in this case.</li>
 * </ul>
 *
 * In order to convert cookie to the format supported by the
 * {@value #SET_COOKIE_HEADER} header use the {@link #toString()} method.
 */
public class HttpCookie {

    // Name of the cookie
    private String name;

    // Value of the cookie
    private String value;

    // The Expires attribute indicates the maximum lifetime of the cookie,
    // represented as the date and time at which the cookie expires.
    private Date expires;

    // Lifetime of the cookie in seconds. After that it should be
    // discarded. The Max-Age attribute has precedence and controls the
    // expiration date of the cookie. If a cookie has neither the Max-Age nor
    // the Expires attribute, the user agent will retain the cookie until "the
    // current session is over"
    private long maxAge = MAX_AGE_UNSPECIFIED;

    // Value to denote that max age attribute was not specified
    private final static long MAX_AGE_UNSPECIFIED = -1;

    // Specifies those hosts to which the cookie will be sent
    private String domain;

    // Subset of URLs on the server to which cookie applies
    private String path;

    // Flag that denotes that cookie should be sent only using isSecure means
    // typically HTTP over TLS
    private boolean secure;

    // Flag that limits cookie to the scope of the HTTP request
    private boolean httpOnly;

    // Date of cookie creation
    private long creationDateInMs = 0;

    // New attributes may be added, if both client and the server understand
    // them
    // private Map<String, String> extensionAttributes;
    private Hashtable extensionAttributes; // String, String

    // Cookie delimiter
    private static final String COOKIE_ATTRIBUTE_DELIMETER = "; ";

    // HTTP headers related to cookies
    private static final String COOKIE_HEADER = "Cookie:";
    private static final String SET_COOKIE_HEADER = "Set-Cookie:";

    /**
     * Constructs a cookie with the specified name and value.<br><br>
     *
     * The name of cookie must be a valid HTTP token as defined by the RFC 2616:
     * it should not contain control characters or separators. The name of
     * cookie cannot be changed after creation.<br><br>
     *
     * The value of cookie can be anything the server chooses to send.
     * Acceptable value characters are ASCII characters excluding control
     * characters, whitespace characters, double quote, comma, semicolon and
     * backslash characters. Value may be enclosed in double quotes. The value
     * of cookie can be changed after creation using the
     * {@link #setValue(java.lang.String)} method.
     *
     *
     * @param name the name of the cookie
     * @param value the value of the cookie
     *
     * @throws IllegalArgumentException if the name of the cookie or the value
     * of the cookie contains illegal characters
     * @throws NullPointerException if name or value is null
     */
    public HttpCookie(String name, String value) {
        Objects.requireNonNull(name, "Cookie name must not be null");
        Objects.requireNonNull(value, "Cookie value must not be null");
        if (!isTokenValid(name)) {
            throw new IllegalArgumentException("Illegal cookie name: " + name);
        }
        if (!isCookieValueValid(value)) {
            throw new IllegalArgumentException("Illegal cookie value: " + value);
        }
        this.name = name;
        this.value = value;
        creationDateInMs = System.currentTimeMillis();
    }

    // Checking validity according to the https://tools.ietf.org/html/rfc7230#page-27
    private boolean isTokenValid(String token) {
        if (token == null || token.length() == 0) {
            return false;
        }

        char[] cArray = token.toCharArray();
        for (int n=0; n < cArray.length; n++) 
        {
            char character = cArray[n];
            if (character == '!'
                || (character >= '#' && character <= '\'')
                || character == '*' || character == '+'
                || character == '-' || character == '.'
                || (Character.isDigit(character)
                    || Character.isLowerCase(character) || Character.isUpperCase(character))
                || character == '_' || character == '`' || character == '|' || character == '~') {
                continue;
            }
            return false;
        }
        return true;
    }

    /**
     * Returns the name of the cookie. The name cannot be changed after
     * creation.
     *
     * @return the name of the cookie
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value of the cookie.
     *
     * @return the value of the cookie
     *
     * @see #setValue
     */
    public String getValue() {
        return value;
    }

    /**
     * Assigns a new value to a cookie. If it is needed to set the binary data,
     * it is advised to use Base64 encoding. Acceptable value characters are
     * ASCII characters excluding control characters, whitespace characters,
     * double quote, comma, semicolon and backslash characters. Value may be
     * enclosed in double quotes.
     *
     * @param newValue a <code>String</code> specifying the new value
     *
     * @throws IllegalArgumentException if new value contains illegal characters
     *
     * @see #getValue
     */
    public void setValue(String newValue) {
        if (isCookieValueValid(newValue)) {
            value = newValue;
        } else {
            throw new IllegalArgumentException("Illegal value of cookie: " + newValue);
        }
    }

    // Checking the validity according to the http://tools.ietf.org/html/rfc6265#section-4.1.1
    private static boolean isCookieValueValid(String cookieValue) {
        if (cookieValue == null) {
            return false;
        }

        // Value may be enclosed in the double quotes
        cookieValue = stripOffSurroundingQuote(cookieValue);
        
        char[] cArray = cookieValue.toCharArray();
        for (int n=0; n < cArray.length; n++) 
        {
            char character = cArray[n];
            // Checking that each character is in the list of allowed characters
            if (character == '!'
                || (character >= '#' && character <= '+')
                || (character >= '-' && character <= ':')
                || (character >= '<' && character <= '[')
                || (character >= ']' && character <= '~')) {
                continue;
            }
            return false;
        }
        return true;
    }

    /**
     * Returns the expiration date of the cookie.
     *
     * @return the expiration date of the cookie or null if it is not set
     */
    public Date getExpires() {
        return expires;
    }

    /**
     * Sets the expiration data of the cookie. Provide null as a parameter to
     * remove the expiration date.
     *
     * @param expires the expiration date of the cookie or null to remove
     * "Expires" attribute
     */
    public void setExpires(Date expires) {
        this.expires = expires;
    }

    /**
     * Reports whether this HTTP cookie has expired or not. Max-Age attribute
     * takes precedence over Expires attributes. If neither is specified cookie
     * should be retained until the session ends, however it is not considered
     * as expired.
     *
     * @return true to indicate that this HTTP cookie has expired
     */
    public boolean hasExpired() {
        // Max-Age takes precedence over Expires
        if (maxAge == MAX_AGE_UNSPECIFIED) {
            if (expires == null) {
                // Neither Max-Age is specified, nor Expires is specified,
                // cookie should be retained until session ends
                return false;
            } else {
                return (expires.getTime() - System.currentTimeMillis()) / 1000 <= 0;
            }
        } else {
            if (maxAge == 0) {
                return true;
            }

            long deltaSeconds = (System.currentTimeMillis() - creationDateInMs) / 1000;
            return deltaSeconds > maxAge;
        }
    }

    /**
     * Sets the maximum lifetime of the cookie represented in seconds until the
     * cookie expires. A zero value causes the cookie to be deleted.
     *
     * @param maxAge number of seconds until the cookie is considered as
     * expired; if zero or negative, the cookie should be discarded immediately;
     * setting {@value #MAX_AGE_UNSPECIFIED} value means that Max-Age is not
     * specified
     *
     * @see #getMaxAge
     */
    public void setMaxAge(long maxAge) {
        this.maxAge = maxAge;
    }

    /**
     * Returns the maximum lifetime of the cookie, specified in seconds. By
     * default, {@value #MAX_AGE_UNSPECIFIED} indicates that the maximum age of
     * the cookie is not specified.
     *
     * @return the maximum age of the cookie in seconds or
     * {@value #MAX_AGE_UNSPECIFIED}
     *
     * @see #setMaxAge
     */
    public long getMaxAge() {
        return maxAge;
    }

    /**
     * Specifies those hosts to which the cookie will be sent. Domain must have
     * the form specified in the
     * <a href="http://tools.ietf.org/html/rfc6265#section-4.1.2.3">RFC
     * 6265</a>.
     *
     * @param pattern the domain name within which this cookie is visible or
     * null to remove this attribute
     *
     * @see #getDomain
     */
    public void setDomain(String pattern) {
        if (pattern != null) {
            domain = pattern.toLowerCase();
        } else {
            domain = pattern;
        }
    }

    /**
     * Returns the domain name within which this cookie is visible. Domain must
     * have the form specified in the
     * <a href="http://tools.ietf.org/html/rfc6265#section-4.1.2.3">RFC
     * 6265</a>.
     *
     * @return the domain name within which this cookie is visible or null if
     * this attribute is not set
     *
     * @see #setDomain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Specifies a path to limit scope of the cookie. If path is omitted, then
     * directory of the request URI is used. Cookie is included in an HTTP
     * request only if the path portion of the request URI matches (or is a
     * subdirectory of) the Path attribute of the cookie.
     *
     * See <a href="http://tools.ietf.org/html/rfc6265#section-4.1.2.4">RFC
     * 6265</a> for more information about this attribute.
     *
     * @param uri a path to limit cookie visibility or null to remove this
     * attribute
     *
     * @see #getPath
     */
    public void setPath(String uri) {
        path = uri;
    }

    /**
     * Returns the path which limits scope of the cookie. Cookie is included in
     * an HTTP request only if the path portion of the request URI matches (or
     * is a subdirectory of) the Path attribute of the cookie.
     *
     * See <a href="http://tools.ietf.org/html/rfc6265#section-4.1.2.4">RFC
     * 6265</a> for more information about this attribute.
     *
     *
     * @return a path which which limits scope of the cookie or null if this
     * attribute is not set
     *
     * @see #setPath
     */
    public String getPath() {
        return path;
    }

    /**
     * Parses the {@value #COOKIE_HEADER} header and returns all contained
     * cookies as a list. The provided argument may contain header name part as
     * well as contain header value only. Parsing is done according to the <a
     * href="http://tools.ietf.org/html/rfc6265#section-4.2.1">Cookie header
     * syntax</a>.
     *
     * @param cookieHeader string which contains either "cookie-header" or
     * "cookie-string" in terms of Cookie header syntax.
     * @return list of contained in the provided string cookies
     * @throws NullPointerException if cookieHeader is null
     * @throws IllegalArgumentException if cookieHeader has bad format
     */
    public static Vector fromCookieHeader(String cookieHeader) {
        Objects.requireNonNull(cookieHeader, "Cookie header must not be null");

        // Deleting the header if it is present
        if (cookieHeader.startsWith(COOKIE_HEADER)) {
            cookieHeader = cookieHeader.substring(COOKIE_HEADER.length());
        }

        cookieHeader = cookieHeader.trim();
        if (cookieHeader.length() == 0) {
            return new Vector();
        }

        Vector cookies = new Vector();
        // Splitting cookiePairs by delimeters
        StringTokenizer tokenizer = new StringTokenizer(cookieHeader, ";");
        while (tokenizer.hasMoreTokens()) {
            String cookiePair = tokenizer.nextToken();

            // Extracting a name and a value from each cookiePair
            int delimeterPosition = cookiePair.indexOf("=");
            if (delimeterPosition != -1) {
                String cookieName = cookiePair.substring(0, delimeterPosition).trim();
                String cookieValue;
                if (cookiePair.length() > delimeterPosition + 1) {
                    cookieValue = cookiePair.substring(delimeterPosition + 1).trim();

                    if (!HttpCookie.isCookieValueValid(cookieValue)) {
                        throw new IllegalArgumentException("Cookie has invalid value: \"" + cookieValue + "\"");
                    }
                } else {
                    // Value is absent (has zero length)
                    cookieValue = "";
                }
                HttpCookie cookie = new HttpCookie(cookieName, cookieValue);
                cookies.addElement(cookie);
            } else {
                throw new IllegalArgumentException("Bad cookies format");
            }
        }
        return cookies;
    }

    /**
     * Returns true if sending of this cookie should be restricted to a secure
     * protocol.
     *
     * @return true if cookie should be sent only via secure means, false
     * otherwise
     *
     * @see #setSecure
     */
    public boolean isSecure() {
        return secure;
    }

    /**
     * Indicates whether the cookie should only be sent using an isSecure
     * protocol, such as HTTPS or SSL. The default value is false.
     *
     * @param flag If true, the cookie can only be sent over a isSecure protocol
     * like HTTPS. If false, it can be sent over any protocol.
     *
     * @see #isSecure()
     */
    public void setSecure(boolean flag) {
        secure = flag;
    }

    /**
     * Returns true if this cookie contains the HttpOnly attribute. This means
     * that the cookie should not be accessible to scripting engines, like
     * javascript. By default HTTPOnly attribute is not set.
     *
     * @return true if this cookie should be considered HTTP only.
     * @see #setHttpOnly(boolean)
     */
    public boolean isHttpOnly() {
        return httpOnly;
    }

    /**
     * Sets whether the cookie should be considered HTTPOnly. If set to true it
     * means the cookie should not be accessible to scripting engines like
     * javascript. By default HTTPOnly attribute is not set.
     *
     * @param httpOnly if true, make the cookie HTTP only, i.e. only visible as
     * part of an HTTP request.
     * @see #isHttpOnly()
     */
    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    /**
     * Adds an extension attribute with the specified value. It should be
     * recognized both by user-agent and a server to work properly.
     *
     * @param key the name of the attribute
     * @param value the value of the attribute
     * @throws NullPointerException if key or value is null
     */
    public void addExtensionAttribute(String key, String value) {
        Objects.requireNonNull(key, "Key must not be null");
        Objects.requireNonNull(value, "Value must not be null");
        if (extensionAttributes == null) {
            extensionAttributes = new Hashtable();
        }
        extensionAttributes.put(key, value);
    }

    /**
     * Returns the value of an attribute with the specified name.
     *
     * @param key the name of the attribute
     * @return the value of the attribute or null if there is no such attribute
     * @throws NullPointerException if key is null
     */
    public String getExtensionAttribute(String key) {
        Objects.requireNonNull(key, "Key must not be null");

        if (extensionAttributes == null) {
            return null;
        }
        return (String)extensionAttributes.get(key);
    }

    /**
     * Parses the {@value #SET_COOKIE_HEADER} header and returns a contained
     * cookie. The provided argument may contain header name part as well as
     * contain header value only.Parsing is done according to the <a
     * href="http://tools.ietf.org/html/rfc6265#section-4.1.1">Set-Cookie header
     * syntax</a>.
     *
     * @param setCookieString string which contains either "set-cookie-header"
     * or "set-cookie-string" in terms of Set-Cookie header syntax.
     * @return contained in the provided string cookie
     * @throws NullPointerException if setCookieString is null
     * @throws IllegalArgumentException if setCookieString has bad format
     */
    public static HttpCookie fromString(String setCookieString) {
        Objects.requireNonNull(setCookieString, "SetCookie string must not be null");
        return fromSetCookieString(setCookieString);
    }

    private static HttpCookie fromSetCookieString(String header) {
        HttpCookie cookie = null;
        String nameValuePair;

        // Deleting a header name if it is present
        if (header.startsWith(SET_COOKIE_HEADER)) {
            header = header.substring(SET_COOKIE_HEADER.length());
        }

        StringTokenizer tokenizer = new StringTokenizer(header, ";");

        // There should always be at least one name-value pair containing cookie
        // name and value. This pair must be first
        try {
            nameValuePair = tokenizer.nextToken();
            int index = nameValuePair.indexOf('=');
            if (index != -1) {
                String name = nameValuePair.substring(0, index).trim();
                String value = nameValuePair.substring(index + 1).trim();
                if (!isCookieValueValid(value)) {
                    throw new IllegalArgumentException("Cookie value is invalid: \"" + value + "\"");
                }
                cookie = new HttpCookie(name, value);
            } else {
                // "=" should always be in the first name-value pair to delimit
                // cookie name and value
                throw new IllegalArgumentException("Invalid cookie name-value pair");
            }
        } catch (NoSuchElementException ignored) {
            throw new IllegalArgumentException("Empty cookie header string");
        }

        // Remaining name-value pairs are attributes of the cookie
        while (tokenizer.hasMoreTokens()) {
            nameValuePair = tokenizer.nextToken();
            int index = nameValuePair.indexOf('=');
            String name, value;
            if (index != -1) {
                name = nameValuePair.substring(0, index).trim();
                value = nameValuePair.substring(index + 1).trim();
            } else {
                name = nameValuePair.trim();
                value = null;
            }

            // Assign attribute of the cookie
            assignAttribute(cookie, name, value);
        }

        return cookie;
    }

    private static void assignAttribute(HttpCookie cookie, String name, String value) throws IllegalArgumentException 
    {
        if(name.equals("Expires")) cookie.setExpires(DateUtils.stringToHttpDate(value));
        else if(name.equals("Max-Age")) 
        {
            try {
                cookie.setMaxAge(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Wrong value: " + value);
            }
        }  else if(name.equals("Domain")) cookie.setDomain(value);
        else if(name.equals("Path")) cookie.setPath(value);
        else if(name.equals("Secure")) cookie.setSecure(true);
        else if(name.equals("HttpOnly")) cookie.setHttpOnly(true);
        else cookie.addExtensionAttribute(name, value);
    }

    /**
     * Returns the hash code of the cookie. Name, lower-case domain and path
     * attributes are used to calculate the hash value.
     *
     * @return the hash code of the cookie
     */
    // @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.name);
        hash = 53 * hash + Objects.hashCode(this.domain != null ? this.domain.toLowerCase() : null);
        hash = 53 * hash + Objects.hashCode(this.path);
        return hash;
    }

    /**
     * Tests whether another cookie is equal to this cookie. According to the <a
     * href="http://tools.ietf.org/html/rfc6265#section-5.3">Step 11 of RFC 6265
     * Storage Model section</a> cookies are considered the same if their names
     * are equal case-sensitively, the Paths attribute values are equal
     * case-sensitively and the Domain attribute values are equal
     * case-insensitively.
     *
     * @param cookie the cookie to test equality
     * @return true if the provided cookie equals to this one
     */
    // @Override
    public boolean equals(Object cookie) {
        if (cookie == null) {
            return false;
        }
        if (getClass() != cookie.getClass()) {
            return false;
        }
        HttpCookie other = (HttpCookie) cookie;
        return Objects.equals(this.name, other.name) && equalsIgnoreCase(this.domain, other.domain) && Objects.equals(this.path, other.path);
    }

    private static boolean equalsIgnoreCase(String one, String another) {
        if (one == another) {
            return true;
        }
        if (one != null && another != null) {
            return one.equalsIgnoreCase(another);
        }
        return false;
    }

    /**
     * Constructs a {@value #SET_COOKIE_HEADER} cookie header string
     * representation of this cookie, which is in the format defined by the RFC
     * 6265 specification, but without the leading {@value #SET_COOKIE_HEADER}
     * token.
     *
     * @return a string form of the cookie
     */
    // @Override
    public String toString() {
        return toRFC6265String();
    }

    private String toRFC6265String() {
        StringBuffer builder = new StringBuffer();
        builder.append(name).append("=").append(value);
        if (expires != null) {
            builder.append(COOKIE_ATTRIBUTE_DELIMETER).append("Expires=").append(DateUtils.httpDateToString(expires));
        }

        if (maxAge != MAX_AGE_UNSPECIFIED) {
            builder.append(COOKIE_ATTRIBUTE_DELIMETER).append("Max-Age=").append(maxAge);
        }

        if (domain != null) {
            builder.append(COOKIE_ATTRIBUTE_DELIMETER).append("Domain=").append(domain);
        }

        if (path != null) {
            builder.append(COOKIE_ATTRIBUTE_DELIMETER).append("Path=").append(path);
        }

        if (secure) {
            builder.append(COOKIE_ATTRIBUTE_DELIMETER).append("Secure");
        }

        if (httpOnly) {
            builder.append(COOKIE_ATTRIBUTE_DELIMETER).append("HttpOnly");
        }

        // If extension attributes map has been created, then it contains at
        // least one element
        if (extensionAttributes != null) {
            
            for (Enumeration en=extensionAttributes.keys();en.hasMoreElements();)
            {
                String key = (String)en.nextElement();
                String value = (String)extensionAttributes.get(key);
                builder.append(COOKIE_ATTRIBUTE_DELIMETER).append(key).append("=").append(value);                
            }
        }

        return builder.toString();
    }

    private static String stripOffSurroundingQuote(String string) {
        // If string is surrounded by double quotes, removing them
        if (string != null && string.length() > 2
            && string.charAt(0) == '"' && string.charAt(string.length() - 1) == '"') {
            return string.substring(1, string.length() - 1);
        }
        return string;
    }
}
