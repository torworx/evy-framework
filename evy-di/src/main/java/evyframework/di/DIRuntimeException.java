/*****************************************************************
 *   Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 ****************************************************************/

package evyframework.di;

public class DIRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
     * Creates new DIRuntimeException without detail message.
     */
    public DIRuntimeException() {
    }

    /**
     * Constructs an exception with the specified message and an optional list of message
     * formatting arguments. Message formatting rules follow "String.format(..)"
     * conventions.
     */
    public DIRuntimeException(String messageFormat, Object... messageArgs) {
        super(String.format(messageFormat, messageArgs));
    }

    /**
     * Constructs an <code>DIRuntimeException</code> that wraps
     * <code>exception</code> thrown elsewhere.
     */
    public DIRuntimeException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs an exception with the specified message and exception cause and an
     * optional list of message formatting arguments. Message formatting rules follow
     * "String.format(..)" conventions.
     */
    public DIRuntimeException(String messageFormat, Throwable cause,
            Object... messageArgs) {
        super(String.format(messageFormat, messageArgs), cause);
    }

    /**
     * Returns exception message without exception label.
     * 
     * @since 1.1
     */
    public String getUnlabeledMessage() {
        return super.getMessage();
    }

    /**
     * Returns message that includes exception label and the actual exception
     * message.
     */
    @Override
    public String getMessage() {
        String message = super.getMessage();
        return (message != null)
                ? DIException.getExceptionLabel() + message
                : DIException.getExceptionLabel() + "(no message)";
    }
}
