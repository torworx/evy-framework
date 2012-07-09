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

package evyframework.pool.impl;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import evyframework.pool.BaseKeyedPoolableObjectFactory;
import evyframework.pool.KeyedObjectPool;
import evyframework.pool.KeyedPoolableObjectFactory;
import evyframework.pool.TestKeyedObjectPool;
import evyframework.pool.VisitTracker;
import evyframework.pool.VisitTrackerFactory;
import evyframework.pool.Waiter;
import evyframework.pool.WaiterFactory;
import evyframework.pool.impl.GenericKeyedObjectPool;
import evyframework.pool.impl.GenericKeyedObjectPoolConfig;

/**
 * @version $Revision: 1334515 $
 */
public class TestGenericKeyedObjectPool extends TestKeyedObjectPool {

    @Override
    protected KeyedObjectPool<Object,Object> makeEmptyPool(int mincapacity) {

        KeyedPoolableObjectFactory<Object,Object> factory =
                new KeyedPoolableObjectFactory<Object,Object>()  {
            ConcurrentHashMap<Object,AtomicInteger> map = new ConcurrentHashMap<Object,AtomicInteger>();
            @Override
            public Object makeObject(Object key) {
                int counter = 0;
                AtomicInteger Counter = map.get(key);
                if(null != Counter) {
                    counter = Counter.incrementAndGet();
                } else {
                    map.put(key, new AtomicInteger(0));
                    counter = 0;
                }
                return String.valueOf(key) + String.valueOf(counter);
            }
            @Override
            public void destroyObject(Object key, Object obj) { }
            @Override
            public boolean validateObject(Object key, Object obj) { return true; }
            @Override
            public void activateObject(Object key, Object obj) { }
            @Override
            public void passivateObject(Object key, Object obj) { }
        };

        GenericKeyedObjectPool<Object,Object> pool =
            new GenericKeyedObjectPool<Object,Object>(factory);
        pool.setMaxTotalPerKey(mincapacity);
        pool.setMaxIdlePerKey(mincapacity);
        return pool;
    }

    @Override
    protected KeyedObjectPool<Object,Object> makeEmptyPool(KeyedPoolableObjectFactory<Object,Object> factory) {
        GenericKeyedObjectPool<Object,Object> pool =
            new GenericKeyedObjectPool<Object,Object>(factory);
        return pool;
    }

    @Override
    protected Object getNthObject(Object key, int n) {
        return String.valueOf(key) + String.valueOf(n);
    }

    @Override
    protected Object makeKey(int n) {
        return String.valueOf(n);
    }

    private GenericKeyedObjectPool<String,String> pool = null;
    private SimpleFactory<String> factory = null;
    private final Integer zero = new Integer(0);
    private final Integer one = new Integer(1);
    private final Integer two = new Integer(2);

    @Before
    public void setUp() throws Exception {
        factory = new SimpleFactory<String>();
        pool = new GenericKeyedObjectPool<String,String>(factory);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        String poolName = pool.getJmxName().toString();
        pool.clear();
        pool.close();
        pool = null;
        factory = null;

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        Set<ObjectName> result = mbs.queryNames(new ObjectName(
                "org.apache.commoms.pool2:type=GenericKeyedObjectPool,*"),
                null);
        // There should be no registered pools at this point
        int registeredPoolCount = result.size();
        StringBuilder msg = new StringBuilder("Current pool is: ");
        msg.append(poolName);
        msg.append("  Still open pools are: ");
        for (ObjectName name : result) {
            // Clean these up ready for the next test
            msg.append(name.toString());
            msg.append(" created via\n");
            msg.append(mbs.getAttribute(name, "CreationStackTrace"));
            msg.append('\n');
            mbs.unregisterMBean(name);
        }
        Assert.assertEquals(msg.toString(), 0, registeredPoolCount);
    }

    @Test(timeout=60000)
    public void testNegativeMaxTotalPerKey() throws Exception {
        pool.setMaxTotalPerKey(-1);
        pool.setBlockWhenExhausted(false);
        String obj = pool.borrowObject("");
        assertEquals("0",obj);
        pool.returnObject("",obj);
    }

    @Test(timeout=60000)
    public void testNumActiveNumIdle2() throws Exception {
        assertEquals(0,pool.getNumActive());
        assertEquals(0,pool.getNumIdle());
        assertEquals(0,pool.getNumActive("A"));
        assertEquals(0,pool.getNumIdle("A"));
        assertEquals(0,pool.getNumActive("B"));
        assertEquals(0,pool.getNumIdle("B"));

        String objA0 = pool.borrowObject("A");
        String objB0 = pool.borrowObject("B");

        assertEquals(2,pool.getNumActive());
        assertEquals(0,pool.getNumIdle());
        assertEquals(1,pool.getNumActive("A"));
        assertEquals(0,pool.getNumIdle("A"));
        assertEquals(1,pool.getNumActive("B"));
        assertEquals(0,pool.getNumIdle("B"));

        String objA1 = pool.borrowObject("A");
        String objB1 = pool.borrowObject("B");

        assertEquals(4,pool.getNumActive());
        assertEquals(0,pool.getNumIdle());
        assertEquals(2,pool.getNumActive("A"));
        assertEquals(0,pool.getNumIdle("A"));
        assertEquals(2,pool.getNumActive("B"));
        assertEquals(0,pool.getNumIdle("B"));

        pool.returnObject("A",objA0);
        pool.returnObject("B",objB0);

        assertEquals(2,pool.getNumActive());
        assertEquals(2,pool.getNumIdle());
        assertEquals(1,pool.getNumActive("A"));
        assertEquals(1,pool.getNumIdle("A"));
        assertEquals(1,pool.getNumActive("B"));
        assertEquals(1,pool.getNumIdle("B"));

        pool.returnObject("A",objA1);
        pool.returnObject("B",objB1);

        assertEquals(0,pool.getNumActive());
        assertEquals(4,pool.getNumIdle());
        assertEquals(0,pool.getNumActive("A"));
        assertEquals(2,pool.getNumIdle("A"));
        assertEquals(0,pool.getNumActive("B"));
        assertEquals(2,pool.getNumIdle("B"));
    }

    @Test(timeout=60000)
    public void testMaxIdle() throws Exception {
        pool.setMaxTotalPerKey(100);
        pool.setMaxIdlePerKey(8);
        String[] active = new String[100];
        for(int i=0;i<100;i++) {
            active[i] = pool.borrowObject("");
        }
        assertEquals(100,pool.getNumActive(""));
        assertEquals(0,pool.getNumIdle(""));
        for(int i=0;i<100;i++) {
            pool.returnObject("",active[i]);
            assertEquals(99 - i,pool.getNumActive(""));
            assertEquals((i < 8 ? i+1 : 8),pool.getNumIdle(""));
        }

        for(int i=0;i<100;i++) {
            active[i] = pool.borrowObject("a");
        }
        assertEquals(100,pool.getNumActive("a"));
        assertEquals(0,pool.getNumIdle("a"));
        for(int i=0;i<100;i++) {
            pool.returnObject("a",active[i]);
            assertEquals(99 - i,pool.getNumActive("a"));
            assertEquals((i < 8 ? i+1 : 8),pool.getNumIdle("a"));
        }

        // total number of idle instances is twice maxIdle
        assertEquals(16, pool.getNumIdle());
        // Each pool is at the sup
        assertEquals(8, pool.getNumIdle(""));
        assertEquals(8, pool.getNumIdle("a"));

    }

    @Test(timeout=60000)
    public void testMaxTotalPerKey() throws Exception {
        pool.setMaxTotalPerKey(3);
        pool.setBlockWhenExhausted(false);

        pool.borrowObject("");
        pool.borrowObject("");
        pool.borrowObject("");
        try {
            pool.borrowObject("");
            fail("Expected NoSuchElementException");
        } catch(NoSuchElementException e) {
            // expected
        }
    }

    @Test(timeout=60000)
    public void testMaxTotalPerKeyZero() throws Exception {
        pool.setMaxTotalPerKey(0);
        pool.setBlockWhenExhausted(false);

        try {
            pool.borrowObject("a");
            fail("Expected NoSuchElementException");
        } catch(NoSuchElementException e) {
            // expected
        }
    }

    @Test(timeout=60000)
    public void testMaxTotal() throws Exception {
        pool.setMaxTotalPerKey(2);
        pool.setMaxTotal(3);
        pool.setBlockWhenExhausted(false);

        String o1 = pool.borrowObject("a");
        assertNotNull(o1);
        String o2 = pool.borrowObject("a");
        assertNotNull(o2);
        String o3 = pool.borrowObject("b");
        assertNotNull(o3);
        try {
            pool.borrowObject("c");
            fail("Expected NoSuchElementException");
        } catch(NoSuchElementException e) {
            // expected
        }

        assertEquals(0, pool.getNumIdle());

        pool.returnObject("b", o3);
        assertEquals(1, pool.getNumIdle());
        assertEquals(1, pool.getNumIdle("b"));

        Object o4 = pool.borrowObject("b");
        assertNotNull(o4);
        assertEquals(0, pool.getNumIdle());
        assertEquals(0, pool.getNumIdle("b"));

        pool.setMaxTotal(4);
        Object o5 = pool.borrowObject("b");
        assertNotNull(o5);

        assertEquals(2, pool.getNumActive("a"));
        assertEquals(2, pool.getNumActive("b"));
        assertEquals(pool.getMaxTotal(),
                pool.getNumActive("b") + pool.getNumActive("b"));
        assertEquals(pool.getNumActive(),
                pool.getMaxTotal());
    }

    @Test(timeout=60000)
    public void testMaxTotalZero() throws Exception {
        pool.setMaxTotal(0);
        pool.setBlockWhenExhausted(false);

        try {
            pool.borrowObject("a");
            fail("Expected NoSuchElementException");
        } catch(NoSuchElementException e) {
            // expected
        }
    }

    @Test(timeout=60000)
    public void testMaxTotalLRU() throws Exception {
        pool.setMaxTotalPerKey(2);
        pool.setMaxTotal(3);

        String o1 = pool.borrowObject("a");
        assertNotNull(o1);
        pool.returnObject("a", o1);
        Thread.sleep(25);

        String o2 = pool.borrowObject("b");
        assertNotNull(o2);
        pool.returnObject("b", o2);
        Thread.sleep(25);

        String o3 = pool.borrowObject("c");
        assertNotNull(o3);
        pool.returnObject("c", o3);
        Thread.sleep(25);

        String o4 = pool.borrowObject("a");
        assertNotNull(o4);
        pool.returnObject("a", o4);
        Thread.sleep(25);

        assertSame(o1, o4);

        // this should cause b to be bumped out of the pool
        String o5 = pool.borrowObject("d");
        assertNotNull(o5);
        pool.returnObject("d", o5);
        Thread.sleep(25);

        // now re-request b, we should get a different object because it should
        // have been expelled from pool (was oldest because a was requested after b)
        String o6 = pool.borrowObject("b");
        assertNotNull(o6);
        pool.returnObject("b", o6);

        assertNotSame(o1, o6);
        assertNotSame(o2, o6);

        // second a is still in there
        String o7 = pool.borrowObject("a");
        assertNotNull(o7);
        pool.returnObject("a", o7);

        assertSame(o4, o7);
    }

    @Test(timeout=60000)
    public void testSettersAndGetters() throws Exception {
        {
            pool.setMaxTotalPerKey(123);
            assertEquals(123,pool.getMaxTotalPerKey());
        }
        {
            pool.setMaxIdlePerKey(12);
            assertEquals(12,pool.getMaxIdlePerKey());
        }
        {
            pool.setMaxWaitMillis(1234L);
            assertEquals(1234L,pool.getMaxWaitMillis());
        }
        {
            pool.setMinEvictableIdleTimeMillis(12345L);
            assertEquals(12345L,pool.getMinEvictableIdleTimeMillis());
        }
        {
            pool.setNumTestsPerEvictionRun(11);
            assertEquals(11,pool.getNumTestsPerEvictionRun());
        }
        {
            pool.setTestOnBorrow(true);
            assertTrue(pool.getTestOnBorrow());
            pool.setTestOnBorrow(false);
            assertTrue(!pool.getTestOnBorrow());
        }
        {
            pool.setTestOnReturn(true);
            assertTrue(pool.getTestOnReturn());
            pool.setTestOnReturn(false);
            assertTrue(!pool.getTestOnReturn());
        }
        {
            pool.setTestWhileIdle(true);
            assertTrue(pool.getTestWhileIdle());
            pool.setTestWhileIdle(false);
            assertTrue(!pool.getTestWhileIdle());
        }
        {
            pool.setTimeBetweenEvictionRunsMillis(11235L);
            assertEquals(11235L,pool.getTimeBetweenEvictionRunsMillis());
        }
        {
            pool.setBlockWhenExhausted(true);
            assertEquals(true,pool.getBlockWhenExhausted());
            pool.setBlockWhenExhausted(false);
            assertEquals(false,pool.getBlockWhenExhausted());
        }
    }

    @Test(timeout=60000)
    public void testEviction() throws Exception {
        pool.setMaxIdlePerKey(500);
        pool.setMaxTotalPerKey(500);
        pool.setNumTestsPerEvictionRun(100);
        pool.setMinEvictableIdleTimeMillis(250L);
        pool.setTimeBetweenEvictionRunsMillis(500L);

        String[] active = new String[500];
        for(int i=0;i<500;i++) {
            active[i] = pool.borrowObject("");
        }
        for(int i=0;i<500;i++) {
            pool.returnObject("",active[i]);
        }

        try { Thread.sleep(1000L); } catch(InterruptedException e) { }
        assertTrue("Should be less than 500 idle, found " + pool.getNumIdle(""),pool.getNumIdle("") < 500);
        try { Thread.sleep(600L); } catch(InterruptedException e) { }
        assertTrue("Should be less than 400 idle, found " + pool.getNumIdle(""),pool.getNumIdle("") < 400);
        try { Thread.sleep(600L); } catch(InterruptedException e) { }
        assertTrue("Should be less than 300 idle, found " + pool.getNumIdle(""),pool.getNumIdle("") < 300);
        try { Thread.sleep(600L); } catch(InterruptedException e) { }
        assertTrue("Should be less than 200 idle, found " + pool.getNumIdle(""),pool.getNumIdle("") < 200);
        try { Thread.sleep(600L); } catch(InterruptedException e) { }
        assertTrue("Should be less than 100 idle, found " + pool.getNumIdle(""),pool.getNumIdle("") < 100);
        try { Thread.sleep(600L); } catch(InterruptedException e) { }
        assertEquals("Should be zero idle, found " + pool.getNumIdle(""),0,pool.getNumIdle(""));

        for(int i=0;i<500;i++) {
            active[i] = pool.borrowObject("");
        }
        for(int i=0;i<500;i++) {
            pool.returnObject("",active[i]);
        }

        try { Thread.sleep(1000L); } catch(InterruptedException e) { }
        assertTrue("Should be less than 500 idle, found " + pool.getNumIdle(""),pool.getNumIdle("") < 500);
        try { Thread.sleep(600L); } catch(InterruptedException e) { }
        assertTrue("Should be less than 400 idle, found " + pool.getNumIdle(""),pool.getNumIdle("") < 400);
        try { Thread.sleep(600L); } catch(InterruptedException e) { }
        assertTrue("Should be less than 300 idle, found " + pool.getNumIdle(""),pool.getNumIdle("") < 300);
        try { Thread.sleep(600L); } catch(InterruptedException e) { }
        assertTrue("Should be less than 200 idle, found " + pool.getNumIdle(""),pool.getNumIdle("") < 200);
        try { Thread.sleep(600L); } catch(InterruptedException e) { }
        assertTrue("Should be less than 100 idle, found " + pool.getNumIdle(""),pool.getNumIdle("") < 100);
        try { Thread.sleep(600L); } catch(InterruptedException e) { }
        assertEquals("Should be zero idle, found " + pool.getNumIdle(""),0,pool.getNumIdle(""));
    }

    @Test(timeout=60000)
    public void testEviction2() throws Exception {
        pool.setMaxIdlePerKey(500);
        pool.setMaxTotalPerKey(500);
        pool.setNumTestsPerEvictionRun(100);
        pool.setMinEvictableIdleTimeMillis(500L);
        pool.setTimeBetweenEvictionRunsMillis(500L);

        String[] active = new String[500];
        String[] active2 = new String[500];
        for(int i=0;i<500;i++) {
            active[i] = pool.borrowObject("");
            active2[i] = pool.borrowObject("2");
        }
        for(int i=0;i<500;i++) {
            pool.returnObject("",active[i]);
            pool.returnObject("2",active2[i]);
        }

        try { Thread.sleep(1100L); } catch(InterruptedException e) { }
        assertTrue("Should be less than 1000 idle, found " + pool.getNumIdle(),pool.getNumIdle() < 1000);
        try { Thread.sleep(600L); } catch(InterruptedException e) { }
        assertTrue("Should be less than 900 idle, found " + pool.getNumIdle(),pool.getNumIdle() < 900);
        try { Thread.sleep(600L); } catch(InterruptedException e) { }
        assertTrue("Should be less than 800 idle, found " + pool.getNumIdle(),pool.getNumIdle() < 800);
        try { Thread.sleep(600L); } catch(InterruptedException e) { }
        assertTrue("Should be less than 700 idle, found " + pool.getNumIdle(),pool.getNumIdle() < 700);
        try { Thread.sleep(600L); } catch(InterruptedException e) { }
        assertTrue("Should be less than 600 idle, found " + pool.getNumIdle(),pool.getNumIdle() < 600);
        try { Thread.sleep(600L); } catch(InterruptedException e) { }
        assertTrue("Should be less than 500 idle, found " + pool.getNumIdle(),pool.getNumIdle() < 500);
        try { Thread.sleep(600L); } catch(InterruptedException e) { }
        assertTrue("Should be less than 400 idle, found " + pool.getNumIdle(),pool.getNumIdle() < 400);
        try { Thread.sleep(600L); } catch(InterruptedException e) { }
        assertTrue("Should be less than 300 idle, found " + pool.getNumIdle(),pool.getNumIdle() < 300);
        try { Thread.sleep(600L); } catch(InterruptedException e) { }
        assertTrue("Should be less than 200 idle, found " + pool.getNumIdle(),pool.getNumIdle() < 200);
        try { Thread.sleep(600L); } catch(InterruptedException e) { }
        assertTrue("Should be less than 100 idle, found " + pool.getNumIdle(),pool.getNumIdle() < 100);
        try { Thread.sleep(600L); } catch(InterruptedException e) { }
        assertEquals("Should be zero idle, found " + pool.getNumIdle(),0,pool.getNumIdle());
    }

    /**
     * Kicks off <numThreads> test threads, each of which will go through
     * <iterations> borrow-return cycles with random delay times <= delay
     * in between.
     */
    public <T> void runTestThreads(int numThreads, int iterations, int delay, GenericKeyedObjectPool<String,T> pool) {
        ArrayList<TestThread<T>> threads = new ArrayList<TestThread<T>>();
        for(int i=0;i<numThreads;i++) {
            TestThread<T> testThread = new TestThread<T>(pool, iterations, delay);
            threads.add(testThread);
            Thread t = new Thread(testThread);
            t.start();
        }
        for (TestThread<T> testThread : threads) {
            while(!(testThread.complete())) {
                try {
                    Thread.sleep(500L);
                } catch(InterruptedException e) {
                    // ignored
                }
            }
            if(testThread.failed()) {
                fail("Thread failed: " + threads.indexOf(testThread) + "\n" +
                        getExceptionTrace(testThread._exception));
            }
        }
    }

    @Test(timeout=60000)
    public void testThreaded1() throws Exception {
        pool.setMaxTotalPerKey(15);
        pool.setMaxIdlePerKey(15);
        pool.setMaxWaitMillis(1000L);
        runTestThreads(20, 100, 50, pool);
    }

    /**
     * Verifies that maxTotal is not exceeded when factory destroyObject
     * has high latency, testOnReturn is set and there is high incidence of
     * validation failures.
     */
    @Test(timeout=60000)
    public void testMaxTotalInvariant() throws Exception {
        int maxTotal = 15;
        factory.setEvenValid(false);     // Every other validation fails
        factory.setDestroyLatency(100);  // Destroy takes 100 ms
        factory.setMaxTotalPerKey(maxTotal);  // (makes - destroys) bound
        factory.setValidationEnabled(true);
        pool.setMaxTotal(maxTotal);
        pool.setMaxIdlePerKey(-1);
        pool.setTestOnReturn(true);
        pool.setMaxWaitMillis(10000L);
        runTestThreads(5, 10, 50, pool);
    }

    @Test(timeout=60000)
    public void testMinIdle() throws Exception {
        pool.setMaxIdlePerKey(500);
        pool.setMinIdlePerKey(5);
        pool.setMaxTotalPerKey(10);
        pool.setNumTestsPerEvictionRun(0);
        pool.setMinEvictableIdleTimeMillis(50L);
        pool.setTimeBetweenEvictionRunsMillis(100L);
        pool.setTestWhileIdle(true);


        //Generate a random key
        String key = "A";

        pool.preparePool(key);

        try { Thread.sleep(150L); } catch(InterruptedException e) { }
        assertTrue("Should be 5 idle, found " + pool.getNumIdle(),pool.getNumIdle() == 5);

        String[] active = new String[5];
        active[0] = pool.borrowObject(key);

        try { Thread.sleep(150L); } catch(InterruptedException e) { }
        assertTrue("Should be 5 idle, found " + pool.getNumIdle(),pool.getNumIdle() == 5);

        for(int i=1 ; i<5 ; i++) {
            active[i] = pool.borrowObject(key);
        }

        try { Thread.sleep(150L); } catch(InterruptedException e) { }
        assertTrue("Should be 5 idle, found " + pool.getNumIdle(),pool.getNumIdle() == 5);

        for(int i=0 ; i<5 ; i++) {
            pool.returnObject(key, active[i]);
        }

        try { Thread.sleep(150L); } catch(InterruptedException e) { }
        assertTrue("Should be 10 idle, found " + pool.getNumIdle(),pool.getNumIdle() == 10);
    }

    @Test(timeout=60000)
    public void testMinIdleMaxTotalPerKey() throws Exception {
        pool.setMaxIdlePerKey(500);
        pool.setMinIdlePerKey(5);
        pool.setMaxTotalPerKey(10);
        pool.setNumTestsPerEvictionRun(0);
        pool.setMinEvictableIdleTimeMillis(50L);
        pool.setTimeBetweenEvictionRunsMillis(100L);
        pool.setTestWhileIdle(true);

        String key = "A";

        pool.preparePool(key);
        assertTrue("Should be 5 idle, found " +
                pool.getNumIdle(),pool.getNumIdle() == 5);

        try { Thread.sleep(150L); } catch(InterruptedException e) { }
        assertTrue("Should be 5 idle, found " + pool.getNumIdle(),pool.getNumIdle() == 5);

        String[] active = new String[10];

        try { Thread.sleep(150L); } catch(InterruptedException e) { }
        assertTrue("Should be 5 idle, found " + pool.getNumIdle(),pool.getNumIdle() == 5);

        for(int i=0 ; i<5 ; i++) {
            active[i] = pool.borrowObject(key);
        }

        try { Thread.sleep(150L); } catch(InterruptedException e) { }
        assertTrue("Should be 5 idle, found " + pool.getNumIdle(),pool.getNumIdle() == 5);

        for(int i=0 ; i<5 ; i++) {
            pool.returnObject(key, active[i]);
        }

        try { Thread.sleep(150L); } catch(InterruptedException e) { }
        assertTrue("Should be 10 idle, found " + pool.getNumIdle(),pool.getNumIdle() == 10);

        for(int i=0 ; i<10 ; i++) {
            active[i] = pool.borrowObject(key);
        }

        try { Thread.sleep(150L); } catch(InterruptedException e) { }
        assertTrue("Should be 0 idle, found " + pool.getNumIdle(),pool.getNumIdle() == 0);

        for(int i=0 ; i<10 ; i++) {
            pool.returnObject(key, active[i]);
        }

        try { Thread.sleep(150L); } catch(InterruptedException e) { }
        assertTrue("Should be 10 idle, found " + pool.getNumIdle(),pool.getNumIdle() == 10);
    }

    @Test(timeout=60000)
    public void testMinIdleNoPreparePool() throws Exception {
        pool.setMaxIdlePerKey(500);
        pool.setMinIdlePerKey(5);
        pool.setMaxTotalPerKey(10);
        pool.setNumTestsPerEvictionRun(0);
        pool.setMinEvictableIdleTimeMillis(50L);
        pool.setTimeBetweenEvictionRunsMillis(100L);
        pool.setTestWhileIdle(true);


        //Generate a random key
        String key = "A";

        try { Thread.sleep(150L); } catch(InterruptedException e) { }
        assertTrue("Should be 0 idle, found " + pool.getNumIdle(),pool.getNumIdle() == 0);

        Object active = pool.borrowObject(key);
        assertNotNull(active);

        try { Thread.sleep(150L); } catch(InterruptedException e) { }
        assertTrue("Should be 5 idle, found " + pool.getNumIdle(),pool.getNumIdle() == 5);
    }

    @Test(timeout=60000)
    public void testFIFO() throws Exception {
        pool.setLifo(false);
        final String key = "key";
        pool.addObject(key); // "key0"
        pool.addObject(key); // "key1"
        pool.addObject(key); // "key2"
        assertEquals("Oldest", "key0", pool.borrowObject(key));
        assertEquals("Middle", "key1", pool.borrowObject(key));
        assertEquals("Youngest", "key2", pool.borrowObject(key));
        String s = pool.borrowObject(key);
        assertEquals("new-3", "key3", s);
        pool.returnObject(key, s);
        assertEquals("returned", s, pool.borrowObject(key));
        assertEquals("new-4", "key4", pool.borrowObject(key));
    }

    @Test(timeout=60000)
    public void testLIFO() throws Exception {
        pool.setLifo(true);
        final String key = "key";
        pool.addObject(key); // "key0"
        pool.addObject(key); // "key1"
        pool.addObject(key); // "key2"
        assertEquals("Youngest", "key2", pool.borrowObject(key));
        assertEquals("Middle", "key1", pool.borrowObject(key));
        assertEquals("Oldest", "key0", pool.borrowObject(key));
        String s = pool.borrowObject(key);
        assertEquals("new-3", "key3", s);
        pool.returnObject(key, s);
        assertEquals("returned", s, pool.borrowObject(key));
        assertEquals("new-4", "key4", pool.borrowObject(key));
    }

    /**
     * Test to make sure evictor visits least recently used objects first,
     * regardless of FIFO/LIFO
     *
     * JIRA: POOL-86
     */
    @Test(timeout=60000)
    public void testEvictionOrder() throws Exception {
        checkEvictionOrder(false);
        checkEvictionOrder(true);
    }

    private void checkEvictionOrder(boolean lifo) throws Exception {
        SimpleFactory<Integer> factory = new SimpleFactory<Integer>();
        GenericKeyedObjectPool<Integer,String> pool =
            new GenericKeyedObjectPool<Integer,String>(factory);
        pool.setNumTestsPerEvictionRun(2);
        pool.setMinEvictableIdleTimeMillis(100);
        pool.setLifo(lifo);

        for (int i = 0; i < 3; i ++) {
            Integer key = new Integer(i);
            for (int j = 0; j < 5; j++) {
                pool.addObject(key);
            }
        }

        // Make all evictable
        Thread.sleep(200);

        /*
         * Initial state (Key, Object) pairs in order of age:
         *
         * (0,0), (0,1), (0,2), (0,3), (0,4)
         * (1,5), (1,6), (1,7), (1,8), (1,9)
         * (2,10), (2,11), (2,12), (2,13), (2,14)
         */

        pool.evict(); // Kill (0,0),(0,1)
        assertEquals(3, pool.getNumIdle(zero));
        String objZeroA = pool.borrowObject(zero);
        assertTrue(lifo ? objZeroA.equals("04") : objZeroA.equals("02"));
        assertEquals(2, pool.getNumIdle(zero));
        String objZeroB = pool.borrowObject(zero);
        assertTrue(objZeroB.equals("03"));
        assertEquals(1, pool.getNumIdle(zero));

        pool.evict(); // Kill remaining 0 survivor and (1,5)
        assertEquals(0, pool.getNumIdle(zero));
        assertEquals(4, pool.getNumIdle(one));
        String objOneA = pool.borrowObject(one);
        assertTrue(lifo ? objOneA.equals("19") : objOneA.equals("16"));
        assertEquals(3, pool.getNumIdle(one));
        String objOneB = pool.borrowObject(one);
        assertTrue(lifo ? objOneB.equals("18") : objOneB.equals("17"));
        assertEquals(2, pool.getNumIdle(one));

        pool.evict(); // Kill remaining 1 survivors
        assertEquals(0, pool.getNumIdle(one));
        pool.evict(); // Kill (2,10), (2,11)
        assertEquals(3, pool.getNumIdle(two));
        String objTwoA = pool.borrowObject(two);
        assertTrue(lifo ? objTwoA.equals("214") : objTwoA.equals("212"));
        assertEquals(2, pool.getNumIdle(two));
        pool.evict(); // All dead now
        assertEquals(0, pool.getNumIdle(two));

        pool.evict(); // Should do nothing - make sure no exception
        // Currently 2 zero, 2 one and 1 two active. Return them
        pool.returnObject(zero, objZeroA);
        pool.returnObject(zero, objZeroB);
        pool.returnObject(one, objOneA);
        pool.returnObject(one, objOneB);
        pool.returnObject(two, objTwoA);
        // Remove all idle objects
        pool.clear();

        // Reload
        pool.setMinEvictableIdleTimeMillis(500);
        factory.counter = 0; // Reset counter
        for (int i = 0; i < 3; i ++) {
            Integer key = new Integer(i);
            for (int j = 0; j < 5; j++) {
                pool.addObject(key);
            }
            Thread.sleep(200);
        }

        // 0's are evictable, others not
        pool.evict(); // Kill (0,0),(0,1)
        assertEquals(3, pool.getNumIdle(zero));
        pool.evict(); // Kill (0,2),(0,3)
        assertEquals(1, pool.getNumIdle(zero));
        pool.evict(); // Kill (0,4), leave (1,5)
        assertEquals(0, pool.getNumIdle(zero));
        assertEquals(5, pool.getNumIdle(one));
        assertEquals(5, pool.getNumIdle(two));
        pool.evict(); // (1,6), (1,7)
        assertEquals(5, pool.getNumIdle(one));
        assertEquals(5, pool.getNumIdle(two));
        pool.evict(); // (1,8), (1,9)
        assertEquals(5, pool.getNumIdle(one));
        assertEquals(5, pool.getNumIdle(two));
        pool.evict(); // (2,10), (2,11)
        assertEquals(5, pool.getNumIdle(one));
        assertEquals(5, pool.getNumIdle(two));
        pool.evict(); // (2,12), (2,13)
        assertEquals(5, pool.getNumIdle(one));
        assertEquals(5, pool.getNumIdle(two));
        pool.evict(); // (2,14), (1,5)
        assertEquals(5, pool.getNumIdle(one));
        assertEquals(5, pool.getNumIdle(two));
        Thread.sleep(200); // Ones now timed out
        pool.evict(); // kill (1,6), (1,7) - (1,5) missed
        assertEquals(3, pool.getNumIdle(one));
        assertEquals(5, pool.getNumIdle(two));
        String obj = pool.borrowObject(one);
        if (lifo) {
            assertEquals("19", obj);
        } else {
            assertEquals("15", obj);
        }
        pool.close();
    }


    /**
     * Verifies that the evictor visits objects in expected order
     * and frequency.
     */
    @Test(timeout=60000)
    public void testEvictorVisiting() throws Exception {
        checkEvictorVisiting(true);
        checkEvictorVisiting(false);
    }

    private void checkEvictorVisiting(boolean lifo) throws Exception {
        VisitTrackerFactory<Integer> factory = new VisitTrackerFactory<Integer>();
        GenericKeyedObjectPool<Integer,VisitTracker<Integer>> pool =
            new GenericKeyedObjectPool<Integer,VisitTracker<Integer>>(factory);
        pool.setNumTestsPerEvictionRun(2);
        pool.setMinEvictableIdleTimeMillis(-1);
        pool.setTestWhileIdle(true);
        pool.setLifo(lifo);
        pool.setTestOnReturn(false);
        pool.setTestOnBorrow(false);
        for (int i = 0; i < 3; i ++) {
            factory.resetId();
            Integer key = new Integer(i);
            for (int j = 0; j < 8; j++) {
                pool.addObject(key);
            }
        }
        pool.evict(); // Visit oldest 2 - 00 and 01
        VisitTracker<Integer> obj = pool.borrowObject(zero);
        pool.returnObject(zero, obj);
        obj = pool.borrowObject(zero);
        pool.returnObject(zero, obj);
        //  borrow, return, borrow, return
        //  FIFO will move 0 and 1 to end - 2,3,4,5,6,7,0,1
        //  LIFO, 7 out, then in, then out, then in - 7,6,5,4,3,2,1,0
        pool.evict();  // Should visit 02 and 03 in either case
        for (int i = 0; i < 8; i++) {
            VisitTracker<Integer> tracker = pool.borrowObject(zero);
            if (tracker.getId() >= 4) {
                assertEquals("Unexpected instance visited " + tracker.getId(),
                        0, tracker.getValidateCount());
            } else {
                assertEquals("Instance " +  tracker.getId() +
                        " visited wrong number of times.",
                        1, tracker.getValidateCount());
            }
        }
        // 0's are all out

        pool.setNumTestsPerEvictionRun(3);

        pool.evict(); // 10, 11, 12
        pool.evict(); // 13, 14, 15

        obj = pool.borrowObject(one);
        pool.returnObject(one, obj);
        obj = pool.borrowObject(one);
        pool.returnObject(one, obj);
        obj = pool.borrowObject(one);
        pool.returnObject(one, obj);
        // borrow, return, borrow, return
        //  FIFO 3,4,5,^,6,7,0,1,2
        //  LIFO 7,6,^,5,4,3,2,1,0
        // In either case, pointer should be at 6
        pool.evict();
        // LIFO - 16, 17, 20
        // FIFO - 16, 17, 10
        pool.evict();
        // LIFO - 21, 22, 23
        // FIFO - 11, 12, 20
        pool.evict();
        // LIFO - 24, 25, 26
        // FIFO - 21, 22, 23
        pool.evict();
        // LIFO - 27, 10, 11
        // FIFO - 24, 25, 26
        for (int i = 0; i < 8; i++) {
            VisitTracker<Integer> tracker = pool.borrowObject(one);
            if ((lifo && tracker.getId() > 1) ||
                    (!lifo && tracker.getId() > 2)) {
                assertEquals("Instance " +  tracker.getId() +
                        " visited wrong number of times.",
                        1, tracker.getValidateCount());
            } else {
                assertEquals("Instance " +  tracker.getId() +
                        " visited wrong number of times.",
                        2, tracker.getValidateCount());
            }
        }
        pool.close();

        // Randomly generate some pools with random numTests
        // and make sure evictor cycles through elements appropriately
        int[] smallPrimes = {2, 3, 5, 7};
        Random random = new Random();
        random.setSeed(System.currentTimeMillis());
        for (int i = 0; i < smallPrimes.length; i++) {
            for (int j = 0; j < 5; j++) {// Try the tests a few times
                // Can't use clear as some objects are still active so create
                // a new pool
                factory = new VisitTrackerFactory<Integer>();
                pool = new GenericKeyedObjectPool<Integer,VisitTracker<Integer>>(
                        factory);
                pool.setMaxIdlePerKey(-1);
                pool.setMaxTotalPerKey(-1);
                pool.setNumTestsPerEvictionRun(smallPrimes[i]);
                pool.setMinEvictableIdleTimeMillis(-1);
                pool.setTestWhileIdle(true);
                pool.setLifo(lifo);
                pool.setTestOnReturn(false);
                pool.setTestOnBorrow(false);

                int zeroLength = 10 + random.nextInt(20);
                for (int k = 0; k < zeroLength; k++) {
                    pool.addObject(zero);
                }
                int oneLength = 10 + random.nextInt(20);
                for (int k = 0; k < oneLength; k++) {
                    pool.addObject(one);
                }
                int twoLength = 10 + random.nextInt(20);
                for (int k = 0; k < twoLength; k++) {
                    pool.addObject(two);
                }

                // Choose a random number of evictor runs
                int runs = 10 + random.nextInt(50);
                for (int k = 0; k < runs; k++) {
                    pool.evict();
                }

                // Total instances in pool
                int totalInstances = zeroLength + oneLength + twoLength;

                // Number of times evictor should have cycled through pools
                int cycleCount = (runs * pool.getNumTestsPerEvictionRun())
                    / totalInstances;

                // Look at elements and make sure they are visited cycleCount
                // or cycleCount + 1 times
                VisitTracker<Integer> tracker = null;
                int visitCount = 0;
                for (int k = 0; k < zeroLength; k++) {
                    tracker = pool.borrowObject(zero);
                    visitCount = tracker.getValidateCount();
                    if (visitCount < cycleCount || visitCount > cycleCount + 1){
                        fail(formatSettings("ZERO", "runs", runs, "lifo", lifo, "i", i, "j", j,
                                "k", k, "visitCount", visitCount, "cycleCount", cycleCount,
                                "totalInstances", totalInstances, zeroLength, oneLength, twoLength));
                    }
                }
                for (int k = 0; k < oneLength; k++) {
                    tracker = pool.borrowObject(one);
                    visitCount = tracker.getValidateCount();
                    if (visitCount < cycleCount || visitCount > cycleCount + 1){
                        fail(formatSettings("ONE", "runs", runs, "lifo", lifo, "i", i, "j", j,
                                "k", k, "visitCount", visitCount, "cycleCount", cycleCount,
                                "totalInstances", totalInstances, zeroLength, oneLength, twoLength));
                    }
                }
                int visits[] = new int[twoLength];
                for (int k = 0; k < twoLength; k++) {
                    tracker = pool.borrowObject(two);
                    visitCount = tracker.getValidateCount();
                    visits[k] = visitCount;
                    if (visitCount < cycleCount || visitCount > cycleCount + 1){
                        StringBuilder sb = new StringBuilder("Visits:");
                        for (int l = 0; l <= k; l++){
                            sb.append(visits[l]).append(' ');
                        }
                        fail(formatSettings("TWO "+sb.toString(), "runs", runs, "lifo", lifo, "i", i, "j", j,
                                "k", k, "visitCount", visitCount, "cycleCount", cycleCount,
                                "totalInstances", totalInstances, zeroLength, oneLength, twoLength));
                    }
                }
                pool.close();
            }
        }
    }

    @Test(timeout=60000)
    public void testConstructors() throws Exception {

        // Make constructor arguments all different from defaults
        int maxTotalPerKey = 1;
        int minIdle = 2;
        long maxWait = 3;
        int maxIdle = 4;
        int maxTotal = 5;
        long minEvictableIdleTimeMillis = 6;
        int numTestsPerEvictionRun = 7;
        boolean testOnBorrow = true;
        boolean testOnReturn = true;
        boolean testWhileIdle = true;
        long timeBetweenEvictionRunsMillis = 8;
        boolean blockWhenExhausted = false;
        boolean lifo = false;
        KeyedPoolableObjectFactory<Object,Object> factory = new BaseKeyedPoolableObjectFactory<Object,Object>() {
            @Override
            public Object makeObject(Object key) throws Exception {
                return null;
            }
        };

        GenericKeyedObjectPool<Object,Object> pool =
            new GenericKeyedObjectPool<Object,Object>(factory);
        assertEquals(GenericKeyedObjectPoolConfig.DEFAULT_MAX_TOTAL_PER_KEY, pool.getMaxTotalPerKey());
        assertEquals(GenericKeyedObjectPoolConfig.DEFAULT_MAX_IDLE_PER_KEY, pool.getMaxIdlePerKey());
        assertEquals(GenericKeyedObjectPoolConfig.DEFAULT_MAX_WAIT_MILLIS, pool.getMaxWaitMillis());
        assertEquals(GenericKeyedObjectPoolConfig.DEFAULT_MIN_IDLE_PER_KEY, pool.getMinIdlePerKey());
        assertEquals(GenericKeyedObjectPoolConfig.DEFAULT_MAX_TOTAL, pool.getMaxTotal());
        assertEquals(GenericKeyedObjectPoolConfig.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS,
                pool.getMinEvictableIdleTimeMillis());
        assertEquals(GenericKeyedObjectPoolConfig.DEFAULT_NUM_TESTS_PER_EVICTION_RUN,
                pool.getNumTestsPerEvictionRun());
        assertEquals(GenericKeyedObjectPoolConfig.DEFAULT_TEST_ON_BORROW,
                pool.getTestOnBorrow());
        assertEquals(GenericKeyedObjectPoolConfig.DEFAULT_TEST_ON_RETURN,
                pool.getTestOnReturn());
        assertEquals(GenericKeyedObjectPoolConfig.DEFAULT_TEST_WHILE_IDLE,
                pool.getTestWhileIdle());
        assertEquals(GenericKeyedObjectPoolConfig.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS,
                pool.getTimeBetweenEvictionRunsMillis());
        assertEquals(GenericKeyedObjectPoolConfig.DEFAULT_BLOCK_WHEN_EXHAUSTED,
                pool.getBlockWhenExhausted());
        assertEquals(GenericKeyedObjectPoolConfig.DEFAULT_LIFO, pool.getLifo());
        pool.close();

        GenericKeyedObjectPoolConfig config =
                new GenericKeyedObjectPoolConfig();
        config.setLifo(lifo);
        config.setMaxTotalPerKey(maxTotalPerKey);
        config.setMaxIdlePerKey(maxIdle);
        config.setMinIdlePerKey(minIdle);
        config.setMaxTotal(maxTotal);
        config.setMaxWaitMillis(maxWait);
        config.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        config.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);
        config.setTestWhileIdle(testWhileIdle);
        config.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        config.setBlockWhenExhausted(blockWhenExhausted);
        pool = new GenericKeyedObjectPool<Object,Object>(factory, config);
        assertEquals(maxTotalPerKey, pool.getMaxTotalPerKey());
        assertEquals(maxIdle, pool.getMaxIdlePerKey());
        assertEquals(maxWait, pool.getMaxWaitMillis());
        assertEquals(minIdle, pool.getMinIdlePerKey());
        assertEquals(maxTotal, pool.getMaxTotal());
        assertEquals(minEvictableIdleTimeMillis,
                pool.getMinEvictableIdleTimeMillis());
        assertEquals(numTestsPerEvictionRun, pool.getNumTestsPerEvictionRun());
        assertEquals(testOnBorrow,pool.getTestOnBorrow());
        assertEquals(testOnReturn,pool.getTestOnReturn());
        assertEquals(testWhileIdle,pool.getTestWhileIdle());
        assertEquals(timeBetweenEvictionRunsMillis,
                pool.getTimeBetweenEvictionRunsMillis());
        assertEquals(blockWhenExhausted,pool.getBlockWhenExhausted());
        assertEquals(lifo, pool.getLifo());
        pool.close();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConstructorNullFactory() {
        // add dummy assert (won't be invoked because of IAE) to avoid "unused" warning
        assertNotNull(new GenericKeyedObjectPool<String,String>(null));
        // TODO this currently causes tearDown to report an error
        // Looks like GKOP needs to call close() on its parent before throwing IAE
    }

    @Test(timeout=60000)
    public void testExceptionOnPassivateDuringReturn() throws Exception {
        SimpleFactory<String> factory = new SimpleFactory<String>();
        GenericKeyedObjectPool<String,String> pool =
            new GenericKeyedObjectPool<String,String>(factory);
        String obj = pool.borrowObject("one");
        factory.setThrowExceptionOnPassivate(true);
        pool.returnObject("one", obj);
        assertEquals(0,pool.getNumIdle());
        pool.close();
    }

    @Test(timeout=60000)
    public void testExceptionOnDestroyDuringBorrow() throws Exception {
        factory.setThrowExceptionOnDestroy(true);
        factory.setValidationEnabled(true);
        pool.setTestOnBorrow(true);
        pool.borrowObject("one");
        factory.setValid(false); // Make validation fail on next borrow attempt
        try {
            pool.borrowObject("one");
            fail("Expecting NoSuchElementException");
        } catch (NoSuchElementException ex) {
            // expected
        }
        assertEquals(1, pool.getNumActive("one"));
        assertEquals(0, pool.getNumIdle("one"));
        assertEquals(1, pool.getNumActive());
        assertEquals(0, pool.getNumIdle());
    }

    @Test(timeout=60000)
    public void testExceptionOnDestroyDuringReturn() throws Exception {
        factory.setThrowExceptionOnDestroy(true);
        factory.setValidationEnabled(true);
        pool.setTestOnReturn(true);
        String obj1 = pool.borrowObject("one");
        pool.borrowObject("one");
        factory.setValid(false); // Make validation fail
        pool.returnObject("one", obj1);
        assertEquals(1, pool.getNumActive("one"));
        assertEquals(0, pool.getNumIdle("one"));
        assertEquals(1, pool.getNumActive());
        assertEquals(0, pool.getNumIdle());
    }

    @Test(timeout=60000)
    public void testExceptionOnActivateDuringBorrow() throws Exception {
        String obj1 = pool.borrowObject("one");
        String obj2 = pool.borrowObject("one");
        pool.returnObject("one", obj1);
        pool.returnObject("one", obj2);
        factory.setThrowExceptionOnActivate(true);
        factory.setEvenValid(false);
        // Activation will now throw every other time
        // First attempt throws, but loop continues and second succeeds
        String obj = pool.borrowObject("one");
        assertEquals(1, pool.getNumActive("one"));
        assertEquals(0, pool.getNumIdle("one"));
        assertEquals(1, pool.getNumActive());
        assertEquals(0, pool.getNumIdle());

        pool.returnObject("one", obj);
        factory.setValid(false);
        // Validation will now fail on activation when borrowObject returns
        // an idle instance, and then when attempting to create a new instance
        try {
            pool.borrowObject("one");
            fail("Expecting NoSuchElementException");
        } catch (NoSuchElementException ex) {
            // expected
        }
        assertEquals(0, pool.getNumActive("one"));
        assertEquals(0, pool.getNumIdle("one"));
        assertEquals(0, pool.getNumActive());
        assertEquals(0, pool.getNumIdle());
    }

    @Test(timeout=60000)
    public void testBlockedKeyDoesNotBlockPool() throws Exception {
        pool.setBlockWhenExhausted(true);
        pool.setMaxWaitMillis(5000);
        pool.setMaxTotalPerKey(1);
        pool.setMaxTotal(-1);
        pool.borrowObject("one");
        long start = System.currentTimeMillis();
        // Needs to be in a separate thread as this will block
        Runnable simple = new SimpleTestThread<String>(pool, "one");
        (new Thread(simple)).start();
        // This should be almost instant. If it isn't it means this thread got
        // stuck behind the thread created above which is bad.
        // Give other thread a chance to start
        Thread.sleep(1000);
        pool.borrowObject("two");
        long end = System.currentTimeMillis();
        // If it fails it will be more than 4000ms (5000 less the 1000 sleep)
        // If it passes it should be almost instant
        // Use 3000ms as the threshold - should avoid timing issues on most
        // (all? platforms)
        assertTrue ("Elapsed time: "+(end-start)+" should be less than 4000",(end-start) < 4000);

    }

    private static final boolean DISPLAY_THREAD_DETAILS=
        Boolean.valueOf(System.getProperty("TestGenericKeyedObjectPool.display.thread.details", "false")).booleanValue();
    // To pass this to a Maven test, use:
    // mvn test -DargLine="-DTestGenericKeyedObjectPool.display.thread.details=true"
    // @see http://jira.codehaus.org/browse/SUREFIRE-121

    /*
     * Test multi-threaded pool access.
     * Multiple keys, multiple threads, but maxActive only allows half the threads to succeed.
     *
     * This test was prompted by Continuum build failures in the Commons DBCP test case:
     * TestSharedPoolDataSource.testMultipleThreads2()
     * Let's see if the this fails on Continuum too!
     */
    @Test(timeout=60000)
    public void testMaxWaitMultiThreaded() throws Exception {
        final long maxWait = 500; // wait for connection
        final long holdTime = 4 * maxWait; // how long to hold connection
        final int keyCount = 4; // number of different keys
        final int threadsPerKey = 5; // number of threads to grab the key initially
        pool.setBlockWhenExhausted(true);
        pool.setMaxWaitMillis(maxWait);
        pool.setMaxTotalPerKey(threadsPerKey);
        // Create enough threads so half the threads will have to wait
        WaitingTestThread wtt[] = new WaitingTestThread[keyCount * threadsPerKey * 2];
        for(int i=0; i < wtt.length; i++){
            wtt[i] = new WaitingTestThread(pool,Integer.toString(i % keyCount),holdTime);
        }
        long origin = System.currentTimeMillis()-1000;
        for(int i=0; i < wtt.length; i++){
            wtt[i].start();
        }
        int failed = 0;
        for(int i=0; i < wtt.length; i++){
            wtt[i].join();
            if (wtt[i]._thrown != null){
                failed++;
            }
        }
        if (DISPLAY_THREAD_DETAILS || wtt.length/2 != failed){
            System.out.println(
                    "MaxWait: "+maxWait
                    +" HoldTime: "+holdTime
                    +" KeyCount: "+keyCount
                    +" MaxActive: "+threadsPerKey
                    +" Threads: "+wtt.length
                    +" Failed: "+failed
                    );
            for(int i=0; i < wtt.length; i++){
                WaitingTestThread wt = wtt[i];
                System.out.println(
                        "Preborrow: "+(wt.preborrow-origin)
                        + " Postborrow: "+(wt.postborrow != 0 ? wt.postborrow-origin : -1)
                        + " BorrowTime: "+(wt.postborrow != 0 ? wt.postborrow-wt.preborrow : -1)
                        + " PostReturn: "+(wt.postreturn != 0 ? wt.postreturn-origin : -1)
                        + " Ended: "+(wt.ended-origin)
                        + " Key: "+(wt._key)
                        + " ObjId: "+wt.objectId
                        );
            }
        }
        assertEquals("Expected half the threads to fail",wtt.length/2,failed);
    }

    /**
     * Test case for POOL-180.
     */
    @Test(timeout=200000)
    public void testMaxActivePerKeyExceeded() throws Exception {
        WaiterFactory<String> factory = new WaiterFactory<String>(0, 20, 0, 0, 0, 0, 8, 5, 0);
        // TODO Fix this. Can't use local pool since runTestThreads uses the
        //      protected pool field
        GenericKeyedObjectPool<String,Waiter> pool =
            new GenericKeyedObjectPool<String,Waiter>(factory);
        pool.setMaxTotalPerKey(5);
        pool.setMaxTotal(8);
        pool.setTestOnBorrow(true);
        pool.setMaxIdlePerKey(5);
        pool.setMaxWaitMillis(-1);
        runTestThreads(20, 300, 250, pool);
        pool.close();
    }

    /**
     * Test to make sure that clearOldest does not destroy instances that have been checked out.
     */
    @Test(timeout=60000)
    public void testClearOldest() throws Exception {
        // Make destroy have some latency so clearOldest takes some time
        WaiterFactory<String> factory = new WaiterFactory<String>(0, 20, 0, 0, 0, 0, 50, 5, 0);
        GenericKeyedObjectPool<String,Waiter> pool =
            new GenericKeyedObjectPool<String,Waiter>(factory);
        pool.setMaxTotalPerKey(5);
        pool.setMaxTotal(50);
        pool.setLifo(false);
        // Load the pool with idle instances - 5 each for 10 keys
        for (int i = 0; i < 10; i++) {
            final String key = Integer.valueOf(i).toString();
            for (int j = 0; j < 5; j++) {
               pool.addObject(key);
            }
            // Make sure order is maintained
            Thread.sleep(20);
        }
        // Now set up a race - one thread wants a new instance, triggering clearOldest
        // Other goes after an element on death row
        // See if we end up with dead man walking
        SimpleTestThread<Waiter> t2 = new SimpleTestThread<Waiter>(pool, "51");
        Thread thread2 = new Thread(t2);
        thread2.start();  // Triggers clearOldest, killing all of the 0's and the 2 oldest 1's
        Thread.sleep(50); // Wait for clearOldest to kick off, but not long enough to reach the 1's
        Waiter waiter = pool.borrowObject("1");
        Thread.sleep(200); // Wait for execution to happen
        pool.returnObject("1", waiter);  // Will throw IllegalStateException if dead
        pool.close();
    }


    /**
     * Verifies that threads that get parked waiting for keys not in use
     * when the pool is at maxTotal eventually get served.
     */
    @Test(timeout=60000)
    public void testLivenessPerKey() throws Exception {
        pool.setMaxIdlePerKey(3);
        pool.setMaxTotal(3);
        pool.setMaxTotalPerKey(3);
        pool.setMaxWaitMillis(3000);  // Really a timeout for the test

        // Check out and briefly hold 3 "1"s
        WaitingTestThread t1 = new WaitingTestThread(pool, "1", 100);
        WaitingTestThread t2 = new WaitingTestThread(pool, "1", 100);
        WaitingTestThread t3 = new WaitingTestThread(pool, "1", 100);
        t1.start();
        t2.start();
        t3.start();

        // Try to get a "2" while all capacity is in use.
        // Thread will park waiting on empty queue. Verify it gets served.
        pool.borrowObject("2");
    }

    /**
     * POOL-192
     * Verify that clear(key) does not leak capacity.
     */
    @Test(timeout=60000)
    public void testClear() throws Exception {
        SimpleFactory<String> factory = new SimpleFactory<String>();
        GenericKeyedObjectPool<String,String> pool =
            new GenericKeyedObjectPool<String,String>(factory);
        pool.setMaxTotal(2);
        pool.setMaxTotalPerKey(2);
        pool.setBlockWhenExhausted(false);
        pool.addObject("one");
        pool.addObject("one");
        assertEquals(2, pool.getNumIdle());
        pool.clear("one");
        assertEquals(0, pool.getNumIdle());
        assertEquals(0, pool.getNumIdle("one"));
        String obj1 = pool.borrowObject("one");
        String obj2 = pool.borrowObject("one");
        pool.returnObject("one", obj1);
        pool.returnObject("one", obj2);
        pool.clear();
        assertEquals(0, pool.getNumIdle());
        assertEquals(0, pool.getNumIdle("one"));
        pool.borrowObject("one");
        pool.borrowObject("one");
        pool.close();
    }

    /**
     * POOL-189
     */
    @Test(timeout=60000)
    public void testWhenExhaustedBlockClosePool() throws Exception {
        SimpleFactory<String> factory = new SimpleFactory<String>();
        GenericKeyedObjectPool<String,String> pool =
            new GenericKeyedObjectPool<String,String>(factory);
        pool.setMaxTotalPerKey(1);
        pool.setBlockWhenExhausted(true);
        pool.setMaxWaitMillis(-1);
        String obj1 = pool.borrowObject("a");

        // Make sure an object was obtained
        assertNotNull(obj1);

        // Create a separate thread to try and borrow another object
        WaitingTestThread wtt = new WaitingTestThread(pool, "a", 200);
        wtt.start();
        // Give wtt time to start
        Thread.sleep(200);

        // close the pool (Bug POOL-189)
        pool.close();

        // Give interrupt time to take effect
        Thread.sleep(200);

        // Check thread was interrupted
        assertTrue(wtt._thrown instanceof InterruptedException);
    }

    /*
     * Very simple test thread that just tries to borrow an object from
     * the provided pool with the specified key and returns it
     */
    static class SimpleTestThread<T> implements Runnable {
        private final KeyedObjectPool<String,T> _pool;
        private final String _key;

        public SimpleTestThread(KeyedObjectPool<String,T> pool, String key) {
            _pool = pool;
            _key = key;
        }

        @Override
        public void run() {
            try {
                T obj = _pool.borrowObject(_key);
                _pool.returnObject(_key, obj);
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    /*
     * Very simple test thread that just tries to borrow an object from
     * the provided pool with the specified key and returns it after a wait
     */
    static class WaitingTestThread extends Thread {
        private final KeyedObjectPool<String,String> _pool;
        private final String _key;
        private final long _pause;
        private Throwable _thrown;

        private long preborrow; // just before borrow
        private long postborrow; //  borrow returned
        private long postreturn; // after object was returned
        private long ended;
        private String objectId;

        public WaitingTestThread(KeyedObjectPool<String,String> pool, String key, long pause) {
            _pool = pool;
            _key = key;
            _pause = pause;
            _thrown = null;
        }

        @Override
        public void run() {
            try {
                preborrow = System.currentTimeMillis();
                String obj = _pool.borrowObject(_key);
                objectId=obj.toString();
                postborrow = System.currentTimeMillis();
                Thread.sleep(_pause);
                _pool.returnObject(_key, obj);
                postreturn = System.currentTimeMillis();
            } catch (Exception e) {
                _thrown = e;
            } finally{
                ended = System.currentTimeMillis();
            }
        }
    }

    static class TestThread<T> implements Runnable {
        private final java.util.Random _random = new java.util.Random();

        // Thread config items
        private final KeyedObjectPool<String,T> _pool;
        private final int _iter;
        private final int _delay;

        private volatile boolean _complete = false;
        private volatile boolean _failed = false;
        private volatile Exception _exception;

        public TestThread(KeyedObjectPool<String,T> pool) {
            this(pool, 100, 50);
        }

        public TestThread(KeyedObjectPool<String,T> pool, int iter) {
            this(pool, iter, 50);
        }

        public TestThread(KeyedObjectPool<String,T> pool, int iter, int delay) {
            _pool = pool;
            _iter = iter;
            _delay = delay;
        }

        public boolean complete() {
            return _complete;
        }

        public boolean failed() {
            return _failed;
        }

        @Override
        public void run() {
            for(int i=0;i<_iter;i++) {
                String key = String.valueOf(_random.nextInt(3));
                try {
                    Thread.sleep(_random.nextInt(_delay));
                } catch(InterruptedException e) {
                    // ignored
                }
                T obj = null;
                try {
                    obj = _pool.borrowObject(key);
                } catch(Exception e) {
                    _exception = e;
                    _failed = true;
                    _complete = true;
                    break;
                }

                try {
                    Thread.sleep(_random.nextInt(_delay));
                } catch(InterruptedException e) {
                    // ignored
                }
                try {
                    _pool.returnObject(key,obj);
                } catch(Exception e) {
                    _exception = e;
                    _failed = true;
                    _complete = true;
                    break;
                }
            }
            _complete = true;
        }
    }

    static class SimpleFactory<K> implements KeyedPoolableObjectFactory<K,String> {
        public SimpleFactory() {
            this(true);
        }
        public SimpleFactory(boolean valid) {
            this.valid = valid;
        }
        @Override
        public String makeObject(K key) {
            String out = null;
            synchronized(this) {
                activeCount++;
                if (activeCount > maxTotalPerKey) {
                    throw new IllegalStateException(
                        "Too many active instances: " + activeCount);
                }
                out = String.valueOf(key) + String.valueOf(counter++);
            }
            return out;
        }
        @Override
        public void destroyObject(K key, String obj) throws Exception {
            doWait(destroyLatency);
            synchronized(this) {
                activeCount--;
            }
            if (exceptionOnDestroy) {
                throw new Exception();
            }
        }
        @Override
        public boolean validateObject(K key, String obj) {
            if (enableValidation) {
                return validateCounter++%2 == 0 ? evenValid : oddValid;
            } else {
                return valid;
            }
        }
        @Override
        public void activateObject(K key, String obj) throws Exception {
            if (exceptionOnActivate) {
                if (!(validateCounter++%2 == 0 ? evenValid : oddValid)) {
                    throw new Exception();
                }
            }
        }
        @Override
        public void passivateObject(K key, String obj) throws Exception {
            if (exceptionOnPassivate) {
                throw new Exception();
            }
        }

        public void setMaxTotalPerKey(int maxTotalPerKey) {
            this.maxTotalPerKey = maxTotalPerKey;
        }
        public void setDestroyLatency(long destroyLatency) {
            this.destroyLatency = destroyLatency;
        }
        public void setValidationEnabled(boolean b) {
            enableValidation = b;
        }
        void setEvenValid(boolean valid) {
            evenValid = valid;
        }
        void setValid(boolean valid) {
            evenValid = valid;
            oddValid = valid;
        }

        public void setThrowExceptionOnActivate(boolean b) {
            exceptionOnActivate = b;
        }

        public void setThrowExceptionOnDestroy(boolean b) {
            exceptionOnDestroy = b;
        }

        public void setThrowExceptionOnPassivate(boolean b) {
            exceptionOnPassivate = b;
        }

        int counter = 0;
        boolean valid;

        int activeCount = 0;
        int validateCounter = 0;
        boolean evenValid = true;
        boolean oddValid = true;
        boolean enableValidation = false;
        long destroyLatency = 0;
        int maxTotalPerKey = Integer.MAX_VALUE;
        boolean exceptionOnPassivate = false;
        boolean exceptionOnActivate = false;
        boolean exceptionOnDestroy = false;

        private void doWait(long latency) {
            try {
                Thread.sleep(latency);
            } catch (InterruptedException ex) {
                // ignore
            }
        }
    }

    @Override
    protected boolean isLifo() {
        return true;
    }

    @Override
    protected boolean isFifo() {
        return false;
    }

    private String getExceptionTrace(Throwable t){
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private String formatSettings(String title, String s, int i, String s0, boolean b0, String s1, int i1, String s2, int i2, String s3, int i3,
            String s4, int i4, String s5, int i5, String s6, int i6, int zeroLength, int oneLength, int twoLength){
        StringBuilder sb = new StringBuilder(80);
        sb.append(title).append(' ');
        sb.append(s).append('=').append(i).append(' ');
        sb.append(s0).append('=').append(b0).append(' ');
        sb.append(s1).append('=').append(i1).append(' ');
        sb.append(s2).append('=').append(i2).append(' ');
        sb.append(s3).append('=').append(i3).append(' ');
        sb.append(s4).append('=').append(i4).append(' ');
        sb.append(s5).append('=').append(i5).append(' ');
        sb.append(s6).append('=').append(i6).append(' ');
        sb.append("Lengths=").append(zeroLength).append(',').append(oneLength).append(',').append(twoLength).append(' ');
        return sb.toString();
    }

    /**
     * Ensure the pool is registered.
     */
    @Test(timeout=60000)
    public void testJmxRegistration() {
        ObjectName oname = pool.getJmxName();
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        Set<ObjectName> result = mbs.queryNames(oname, null);
        Assert.assertEquals(1, result.size());
    }

    @Test(timeout=60000)
    public void testJmxNotification() throws Exception {
        factory.setThrowExceptionOnPassivate(true);
        ObjectName oname = pool.getJmxName();
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        JmxNotificationListener listener = new JmxNotificationListener();
        mbs.addNotificationListener(oname, listener, null, null);

        String obj = pool.borrowObject("one");
        pool.returnObject("one", obj);

        List<String> messages = listener.getMessages();
        Assert.assertEquals(1, messages.size());
        Assert.assertNotNull(messages.get(0));
        Assert.assertTrue(messages.get(0).length() > 0);
    }

    private static class JmxNotificationListener
            implements NotificationListener {

        private List<String> messages = new ArrayList<String>();

        public List<String> getMessages() {
            return messages;
        }

        @Override
        public void handleNotification(Notification notification,
                Object handback) {
            messages.add(notification.getMessage());
        }
    }
}


