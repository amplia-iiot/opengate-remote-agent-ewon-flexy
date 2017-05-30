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
 * Exception which is thrown when it is impossible to obtain a length of the
 * resource due to some reasons.
 */
public class LengthIsUnavailableException extends IOException {

    /**
     * Creates a new {@link LengthIsUnavailableException} with no message and
     * cause specified.
     */
    public LengthIsUnavailableException() {
    }

    /**
     * Creates a new {@link LengthIsUnavailableException} with the specified
     * message.
     *
     * @param message exception cause description
     */
    public LengthIsUnavailableException(String message) {
        super(message);
    }

    /**
     * Creates a new {@link LengthIsUnavailableException} with the specified
     * message and a cause of this exception.
     *
     * @param message exception cause description
     * @param cause an exception which caused this exception
     */
    public LengthIsUnavailableException(String message, Throwable cause) {
        // super(message, cause);
        super(message + ". Cause:"+cause.toString());
    }

    /**
     * Creates a new {@link LengthIsUnavailableException} with the specified
     * cause of this exception.
     *
     * @param cause an exception which caused this exception
     */
    public LengthIsUnavailableException(Throwable cause) {
        super(cause.toString());
    }

}
