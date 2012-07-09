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

public class DIException extends Exception {

	private static final long serialVersionUID = 1L;

	private static String exceptionLabel = "";

    static {
//        String version = LocalizedStringsHandler.getString("cayenne.version");
//        String date = LocalizedStringsHandler.getString("cayenne.build.date");
//
//        if (version != null || date != null) {
//            exceptionLabel = "[v." + version + " " + date + "] ";
//        }
//        else {
//            exceptionLabel = "";
//        }
    }

    public static String getExceptionLabel() {
        return exceptionLabel;
    }

    /**
     * Creates new <code>DIException</code> without detail message.
     */
    public DIException() {
    }

    /**
     * Constructs an <code>DIException</code> with the specified detail message.
     * 
     * @param message the detail message.
     */
    public DIException(String messageFormat, Object... messageArgs) {
        super(String.format(messageFormat, messageArgs));
    }

    /**
     * Constructs an <code>DIException</code> that wraps a <code>cause</code> thrown
     * elsewhere.
     */
    public DIException(Throwable cause) {
        super(cause);
    }

    public DIException(String messageFormat, Throwable cause, Object... messageArgs) {
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

    @Override
    public String getMessage() {
        String message = super.getMessage();
        return (message != null) ? getExceptionLabel() + message : getExceptionLabel()
                + "(no message)";
    }
}
