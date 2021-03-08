package at.ac.uibk.mcsconnect.executorservice.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ExtendedThreadPoolExecutor extends ThreadPoolExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtendedThreadPoolExecutor.class);

    private final ThreadLocal<Long> startTime = new ThreadLocal<Long>();
    private final AtomicLong numTasks = new AtomicLong();
    private final AtomicLong totalTime = new AtomicLong();

    /**
     * Constructor to imitate the @see ThreadPoolExecutor
     *
     * @param corePoolSize executors
     * @param maximumPoolSize
     * @param keepAliveTime
     * @param unit
     * @param workQueue
     * @param threadFactory
     * @param handler
     */
    public ExtendedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    protected void beforeExecute(Thread t, Runnable r) {
        try {
            super.beforeExecute(t, r);
            LOGGER.trace(String.format("Thread executed: Thread: %s     Task: %s", t, r));
            startTime.set(System.nanoTime());
        } catch (Throwable e) {
            LOGGER.trace(String.format("Thread killed early: executing beforeExecute.    id: %s     message: %s", t.getId(), e.getMessage()));
        }
    }

    protected void afterExecute(Runnable r, Throwable t) {
        try {
            long endTime = System.nanoTime();
            long taskTime = endTime - startTime.get();
            numTasks.incrementAndGet();
            totalTime.addAndGet(taskTime);
            LOGGER.trace(String.format("Thread finish: type: %s     Total Time: %dns", r,taskTime));
        } catch (Throwable e) {
            LOGGER.trace(String.format("Thread killed: executing afterExecute.     id: destroyed     message: %s", e.getMessage()));
        }
        finally {
            super.afterExecute(r,t);
        }
    }
}
