package at.ac.uibk.mcsconnect.executorservice.impl;

import java.util.concurrent.ThreadFactory;

public class ThreadFactoryImpl implements ThreadFactory {

    private final String poolName;

    public ThreadFactoryImpl(String poolName) {
        this.poolName = poolName;
    }

    public Thread newThread(Runnable runnable) {
        return new ThreadImpl(runnable, poolName);
    }

}