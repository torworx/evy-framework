/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package evyframework.pool;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import org.junit.Test;

import evyframework.pool.BaseObjectPool;
import evyframework.pool.ObjectPool;
import evyframework.pool.PoolableObjectFactory;


/**
 * @version $Revision: 1333195 $ 
 */
public class TestBaseObjectPool extends TestObjectPool {
    private ObjectPool<Object> _pool = null;

    /**
     * @param mincapacity  
     */
    protected ObjectPool<Object> makeEmptyPool(int mincapacity) {
        if (this.getClass() != TestBaseObjectPool.class) {
            fail("Subclasses of TestBaseObjectPool must reimplement this method.");
        }
        throw new UnsupportedOperationException("BaseObjectPool isn't a complete implementation.");
    }

    @Override
    protected ObjectPool<Object> makeEmptyPool(final PoolableObjectFactory<Object> factory) {
        if (this.getClass() != TestBaseObjectPool.class) {
            fail("Subclasses of TestBaseObjectPool must reimplement this method.");
        }
        throw new UnsupportedOperationException("BaseObjectPool isn't a complete implementation.");
    }

    /**
     * @param n  
     */
    protected Object getNthObject(final int n) {
        if (this.getClass() != TestBaseObjectPool.class) {
            fail("Subclasses of TestBaseObjectPool must reimplement this method.");
        }
        throw new UnsupportedOperationException("BaseObjectPool isn't a complete implementation.");
    }

    protected boolean isLifo() {
        if (this.getClass() != TestBaseObjectPool.class) {
            fail("Subclasses of TestBaseObjectPool must reimplement this method.");
        }
        return false;
    }

    protected boolean isFifo() {
        if (this.getClass() != TestBaseObjectPool.class) {
            fail("Subclasses of TestBaseObjectPool must reimplement this method.");
        }
        return false;
    }

    // tests
    @Test
    public void testUnsupportedOperations() throws Exception {
        if (!getClass().equals(TestBaseObjectPool.class)) {
            return; // skip redundant tests
        }
        ObjectPool<Object> pool = new BaseObjectPool<Object>() { 
            @Override
            public Object borrowObject() {
                return null;
            }
            @Override
            public void returnObject(Object obj) {
            }
            @Override
            public void invalidateObject(Object obj) {
            }            
        };   

        assertTrue("Negative expected.", pool.getNumIdle() < 0);
        assertTrue("Negative expected.", pool.getNumActive() < 0);

        try {
            pool.clear();
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }

        try {
            pool.addObject();
            fail("Expected UnsupportedOperationException");
        } catch(UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testClose() throws Exception {
        ObjectPool<Object> pool = new BaseObjectPool<Object>() {
            @Override
            public Object borrowObject() {
                return null;
            }
            @Override
            public void returnObject(Object obj) {
            }
            @Override
            public void invalidateObject(Object obj) {
            }
        };

        pool.close();
        pool.close(); // should not error as of Pool 2.0.
    }

    @Test
    public void testBaseBorrow() throws Exception {
        try {
            _pool = makeEmptyPool(3);
        } catch(UnsupportedOperationException e) {
            return; // skip this test if unsupported
        }
        assertEquals(getNthObject(0),_pool.borrowObject());
        assertEquals(getNthObject(1),_pool.borrowObject());
        assertEquals(getNthObject(2),_pool.borrowObject());
        _pool.close();
    }

    @Test
    public void testBaseAddObject() throws Exception {
        try {
            _pool = makeEmptyPool(3);
        } catch(UnsupportedOperationException e) {
            return; // skip this test if unsupported
        }
        try {
            assertEquals(0,_pool.getNumIdle());
            assertEquals(0,_pool.getNumActive());
            _pool.addObject();
            assertEquals(1,_pool.getNumIdle());
            assertEquals(0,_pool.getNumActive());
            Object obj = _pool.borrowObject();
            assertEquals(getNthObject(0),obj);
            assertEquals(0,_pool.getNumIdle());
            assertEquals(1,_pool.getNumActive());
            _pool.returnObject(obj);
            assertEquals(1,_pool.getNumIdle());
            assertEquals(0,_pool.getNumActive());
        } catch(UnsupportedOperationException e) {
            return; // skip this test if one of those calls is unsupported
        } finally {
            _pool.close();
        }
    }

    @Test
    public void testBaseBorrowReturn() throws Exception {
        try {
            _pool = makeEmptyPool(3);
        } catch(UnsupportedOperationException e) {
            return; // skip this test if unsupported
        }
        Object obj0 = _pool.borrowObject();
        assertEquals(getNthObject(0),obj0);
        Object obj1 = _pool.borrowObject();
        assertEquals(getNthObject(1),obj1);
        Object obj2 = _pool.borrowObject();
        assertEquals(getNthObject(2),obj2);
        _pool.returnObject(obj2);
        obj2 = _pool.borrowObject();
        assertEquals(getNthObject(2),obj2);
        _pool.returnObject(obj1);
        obj1 = _pool.borrowObject();
        assertEquals(getNthObject(1),obj1);
        _pool.returnObject(obj0);
        _pool.returnObject(obj2);
        obj2 = _pool.borrowObject();
        if (isLifo()) {
            assertEquals(getNthObject(2),obj2);
        }
        if (isFifo()) {
            assertEquals(getNthObject(0),obj2);
        }

        obj0 = _pool.borrowObject();
        if (isLifo()) {
            assertEquals(getNthObject(0),obj0);
        }
        if (isFifo()) {
            assertEquals(getNthObject(2),obj0);
        }
        _pool.close();
    }

    @Test
    public void testBaseNumActiveNumIdle() throws Exception {
        try {
            _pool = makeEmptyPool(3);
        } catch(UnsupportedOperationException e) {
            return; // skip this test if unsupported
        }
        assertEquals(0,_pool.getNumActive());
        assertEquals(0,_pool.getNumIdle());
        Object obj0 = _pool.borrowObject();
        assertEquals(1,_pool.getNumActive());
        assertEquals(0,_pool.getNumIdle());
        Object obj1 = _pool.borrowObject();
        assertEquals(2,_pool.getNumActive());
        assertEquals(0,_pool.getNumIdle());
        _pool.returnObject(obj1);
        assertEquals(1,_pool.getNumActive());
        assertEquals(1,_pool.getNumIdle());
        _pool.returnObject(obj0);
        assertEquals(0,_pool.getNumActive());
        assertEquals(2,_pool.getNumIdle());
        _pool.close();
    }

    @Test
    public void testBaseClear() throws Exception {
        try {
            _pool = makeEmptyPool(3);
        } catch(UnsupportedOperationException e) {
            return; // skip this test if unsupported
        }
        assertEquals(0,_pool.getNumActive());
        assertEquals(0,_pool.getNumIdle());
        Object obj0 = _pool.borrowObject();
        Object obj1 = _pool.borrowObject();
        assertEquals(2,_pool.getNumActive());
        assertEquals(0,_pool.getNumIdle());
        _pool.returnObject(obj1);
        _pool.returnObject(obj0);
        assertEquals(0,_pool.getNumActive());
        assertEquals(2,_pool.getNumIdle());
        _pool.clear();
        assertEquals(0,_pool.getNumActive());
        assertEquals(0,_pool.getNumIdle());
        Object obj2 = _pool.borrowObject();
        assertEquals(getNthObject(2),obj2);
        _pool.close();
    }

    @Test
    public void testBaseInvalidateObject() throws Exception {
        try {
            _pool = makeEmptyPool(3);
        } catch(UnsupportedOperationException e) {
            return; // skip this test if unsupported
        }
        assertEquals(0,_pool.getNumActive());
        assertEquals(0,_pool.getNumIdle());
        Object obj0 = _pool.borrowObject();
        Object obj1 = _pool.borrowObject();
        assertEquals(2,_pool.getNumActive());
        assertEquals(0,_pool.getNumIdle());
        _pool.invalidateObject(obj0);
        assertEquals(1,_pool.getNumActive());
        assertEquals(0,_pool.getNumIdle());
        _pool.invalidateObject(obj1);
        assertEquals(0,_pool.getNumActive());
        assertEquals(0,_pool.getNumIdle());
        _pool.close();
    }

    @Test
    public void testBaseClosePool() throws Exception {
        try {
            _pool = makeEmptyPool(3);
        } catch(UnsupportedOperationException e) {
            return; // skip this test if unsupported
        }
        Object obj = _pool.borrowObject();
        _pool.returnObject(obj);

        _pool.close();
        try {
            _pool.borrowObject();
            fail("Expected IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }
    }
}
