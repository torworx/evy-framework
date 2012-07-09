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

package evyframework.pool.performance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import evyframework.pool.impl.GenericObjectPool;

/**
 * Multi-thread performance test
 * 
 * @version $Revision: 1333195 $ 
 */
public class PerformanceTest {
    private int logLevel = 0;
    private int nrIterations = 5;

    private GenericObjectPool<Integer> pool;

    public void setLogLevel(int i) {
        logLevel = i;
    }

    private class TaskStats {
        public int waiting = 0;
        public int complete = 0;
        public long totalBorrowTime = 0;
        public long totalReturnTime = 0;
        public int nrSamples = 0;
    }
    
    class PerfTask implements Callable<TaskStats> {
        TaskStats taskStats = new TaskStats();
        long borrowTime;
        long returnTime;
        
        public void runOnce() {
            try {
                taskStats.waiting++;
                if (logLevel >= 5) {
                    String name = "thread" + Thread.currentThread().getName();
                    System.out.println(name +
                            "   waiting: " + taskStats.waiting +
                            "   complete: " + taskStats.complete);
                }
                long bbegin = System.currentTimeMillis();
                Integer o = pool.borrowObject();
                long bend = System.currentTimeMillis();
                taskStats.waiting--;

                if (logLevel >= 3) {
                    String name = "thread" + Thread.currentThread().getName();
                    System.out.println(name +
                            "    waiting: " + taskStats.waiting +
                            "   complete: " + taskStats.complete);
                }
                                 
                long rbegin = System.currentTimeMillis();
                pool.returnObject(o);
                long rend = System.currentTimeMillis();
                Thread.yield();
                taskStats.complete++;
                borrowTime = (bend-bbegin);
                returnTime = (rend-rbegin);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

       @Override
    public TaskStats call() throws Exception {
           runOnce(); // warmup
           for (int i = 0; i < nrIterations; i++) {
               runOnce();
               taskStats.totalBorrowTime += borrowTime;
               taskStats.totalReturnTime += returnTime;
               taskStats.nrSamples++;
               if (logLevel >= 2) {
                   String name = "thread" + Thread.currentThread().getName();
                   System.out.println("result " + taskStats.nrSamples + "\t"
                           + name + "\t" + "borrow time: " + borrowTime + "\t"
                           + "return time: " + returnTime + "\t" + "waiting: "
                           + taskStats.waiting + "\t" + "complete: "
                           + taskStats.complete);
               }
           }
           return taskStats;
       }
    }

    private void run(int nrIterations, int nrThreads, int maxTotal, int maxIdle) {
        this.nrIterations = nrIterations;
        
        SleepingObjectFactory factory = new SleepingObjectFactory();
        if (logLevel >= 4) { factory.setDebug(true); } 
        pool = new GenericObjectPool<Integer>(factory);
        pool.setMaxTotal(maxTotal);
        pool.setMaxIdle(maxIdle);
        pool.setTestOnBorrow(true);

        ExecutorService threadPool = Executors.newFixedThreadPool(nrThreads);

        List<Callable<TaskStats>> tasks = new ArrayList<Callable<TaskStats>>();
        for (int i = 0; i < nrThreads; i++) {
            tasks.add(new PerfTask());
            Thread.yield();
        }
        
        if (logLevel >= 1) {
            System.out.println("created");
        }
        Thread.yield();
        List<Future<TaskStats>> futures = null;
        try {
            futures = threadPool.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
            
        if (logLevel >= 1) { System.out.println("started"); }
        Thread.yield();

        if (logLevel >= 1) { System.out.println("go"); }
        Thread.yield();

        if (logLevel >= 1) { System.out.println("finish"); }
        
        TaskStats aggregate = new TaskStats();
        if (futures != null) {
            for (Future<TaskStats> future : futures) {
                TaskStats taskStats = null;
                try {
                    taskStats = future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                if (taskStats != null) {
                    aggregate.complete += taskStats.complete;
                    aggregate.nrSamples += taskStats.nrSamples;
                    aggregate.totalBorrowTime += taskStats.totalBorrowTime;
                    aggregate.totalReturnTime += taskStats.totalReturnTime;
                    aggregate.waiting += taskStats.waiting;
                }
            }
        }
        
        System.out.println("-----------------------------------------");
        System.out.println("nrIterations: " + nrIterations);
        System.out.println("nrThreads: " + nrThreads);
        System.out.println("maxTotal: " + maxTotal);
        System.out.println("maxIdle: " + maxIdle);
        System.out.println("nrSamples: " + aggregate.nrSamples);
        System.out.println("totalBorrowTime: " + aggregate.totalBorrowTime);
        System.out.println("totalReturnTime: " + aggregate.totalReturnTime);
        System.out.println("avg BorrowTime: " +
                aggregate.totalBorrowTime/aggregate.nrSamples);
        System.out.println("avg ReturnTime: " +
                aggregate.totalReturnTime/aggregate.nrSamples);
        
        threadPool.shutdown();
    }

    public static void main(String[] args) {
        PerformanceTest test = new PerformanceTest();
        test.setLogLevel(0);
        System.out.println("Increase threads");
        test.run(1,  50,  5,  5);
        test.run(1, 100,  5,  5);
        test.run(1, 200,  5,  5);
        test.run(1, 400,  5,  5);

        System.out.println("Increase threads & poolsize");
        test.run(1,  50,  5,  5);
        test.run(1, 100, 10, 10);
        test.run(1, 200, 20, 20);
        test.run(1, 400, 40, 40);

        System.out.println("Increase maxIdle");
        test.run(1, 400, 40,  5);
        test.run(1, 400, 40, 40);

//      System.out.println("Show creation/destruction of objects");
//      test.setLogLevel(4);
//      test.run(1, 400, 40,  5);
    }
}
