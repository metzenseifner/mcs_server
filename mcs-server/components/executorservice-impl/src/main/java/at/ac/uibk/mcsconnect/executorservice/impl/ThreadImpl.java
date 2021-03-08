package at.ac.uibk.mcsconnect.executorservice.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Ensures the same state between the @see at.ac.uibk.unit.recording.RecordingInstance
 * and @see at.ac.uibk.unit.recorder.Recorder objects.
 */
public class ThreadImpl extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadImpl.class);

    public static final String DEFAULT_NAME = "mcsconnect-thread";
    private static final AtomicInteger created = new AtomicInteger();
    private static final AtomicInteger alive = new AtomicInteger();

    public ThreadImpl(Runnable r) { this(r, DEFAULT_NAME); }

    public ThreadImpl(Runnable runnable, String name) {
        super(runnable, name + "-ThreadCount: " + created.incrementAndGet());
        setUncaughtExceptionHandler(
                new Thread.UncaughtExceptionHandler() {
                    public void uncaughtException(Thread t, Throwable e) {
                        LOGGER.error("UNCAUGHT in thread " + t.getName(), e);
                    }
                }
        );
    }

    public void run() {
        LOGGER.trace("Thread created: \"{}\"", getName());
        try {
            super.run();
        } finally {
            LOGGER.trace("Thread existing: \"{}\"", getName());
        }
    }

    public static int getThreadsCreated() { return created.get(); }
    public static int getThreadsAlive() { return alive.get(); }

}