/*
 * Copyright © 2015, Oracle and/or its affiliates.  All rights reserved. 
 *
 * This software is dual-licensed to you under the MIT License (MIT) and the
 * Universal Permissive License (UPL).  See the LICENSE file in the root directory
 * for license terms.  You may choose either license, or both.
 */
package com.oracle.jmee.samples.webserver;

import java.io.IOException;

/**
 * Exception which is thrown when resource has not been found.
 */
public class ResourceNotFoundException extends IOException {

    /**
     * Creates a new {@link ResourceNotFoundException} with no message and cause
     * specified.
     */
    public ResourceNotFoundException() {
    }

    /**
     * Creates a new {@link ResourceNotFoundException} with the specified
     * message.
     *
     * @param message exception cause description
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates a new {@link ResourceNotFoundException} with the specified
     * message and a cause of this exception.
     *
     * @param message exception cause description
     * @param cause an exception which caused this exception
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        // super(message, cause);
        super(message + ". Cause:"+cause.toString());        
    }

    /**
     * Creates a new {@link ResourceNotFoundException} with the specified cause
     * of this exception.
     *
     * @param cause an exception which caused this exception
     */
    public ResourceNotFoundException(Throwable cause) {
        super(cause.toString());
    }

}
