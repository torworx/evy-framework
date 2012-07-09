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

import java.io.PrintWriter;
import java.io.StringWriter;

import evyframework.di.DIException;
import evyframework.di.DIRuntimeException;

import junit.framework.TestCase;

/**
 */
public class DIRuntimeExceptionTest extends TestCase {

    public void testConstructor1() throws Exception {
        DIRuntimeException ex = new DIRuntimeException();
        assertNull(ex.getCause());
        assertTrue(ex.getMessage().startsWith(DIException.getExceptionLabel()));
    }

    public void testConstructor2() throws Exception {
        DIRuntimeException ex = new DIRuntimeException("abc");
        assertNull(ex.getCause());
        assertEquals(DIException.getExceptionLabel() + "abc", ex.getMessage());
    }

    public void testConstructor3() throws Exception {
        Throwable cause = new Throwable();
        DIRuntimeException ex = new DIRuntimeException(cause);
        assertSame(cause, ex.getCause());
        assertEquals(
            DIException.getExceptionLabel() + cause.toString(),
            ex.getMessage());
    }

    public void testConstructor4() throws Exception {
        Throwable cause = new Throwable();
        DIRuntimeException ex = new DIRuntimeException("abc", cause);
        assertSame(cause, ex.getCause());
        assertEquals(DIException.getExceptionLabel() + "abc", ex.getMessage());
    }

    public void testThrow1() throws Exception {
        try {
            throw new DIRuntimeException();
        }
        catch (DIRuntimeException rtex) {
            StringWriter w = new StringWriter();
            rtex.printStackTrace(new PrintWriter(w));
        }
    }

    public void testThrow2() throws Exception {
        try {
            try {
                throw new Throwable("Test Cause");
            }
            catch (Throwable th) {
                throw new DIRuntimeException(th);
            }
        }
        catch (DIRuntimeException rtex) {
            StringWriter w = new StringWriter();
            rtex.printStackTrace(new PrintWriter(w));
        }
    }
    
    public void testMessageFormatting1() throws Exception {
        DIRuntimeException ex = new DIRuntimeException("x%sx%sx", "a", "b");
        assertEquals("xaxbx", ex.getUnlabeledMessage());
    }
    
    public void testMessageFormatting2() throws Exception {
        Throwable cause = new Throwable();
        DIRuntimeException ex = new DIRuntimeException("x%sx%sx", cause, "a", "b");
        assertEquals("xaxbx", ex.getUnlabeledMessage());
    }

}
