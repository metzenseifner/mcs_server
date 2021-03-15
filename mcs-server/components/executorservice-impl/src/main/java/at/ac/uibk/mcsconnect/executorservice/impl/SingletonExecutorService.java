package at.ac.uibk.mcsconnect.executorservice.impl;

import at.ac.uibk.mcsconnect.executorservice.api.McsScheduledExecutorService;
import at.ac.uibk.mcsconnect.executorservice.api.McsSingletonExecutorService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Consider adding an interface to restart this because when rooms are removed,
 * it could restart this entire module. Ideally, there will be better management
 * of threads to support the dynamic nature of the room configuration.
 */
@Component(
        name = "at.ac.uibk.mcsconnect.executorservice.impl.SingletonExecutorService",
        scope = ServiceScope.SINGLETON
)
public class SingletonExecutorService implements McsSingletonExecutorService, McsScheduledExecutorService {

    private static BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(); // LinkedBlockingQueue and ArrayBlockingQueue use FIFO order

    // Optimize CORE_POOL_SIZE and MAX_POOLSIZE to the system
    private static int N_CPUS = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = N_CPUS * 2;
    private static final int MAX_POOL_SIZE = N_CPUS * 26;
    private static final int KEEP_ALIVE_TIME = 1; // MINUTES until threads over core threshold die
    private static final int AWAIT_FOR_THREAD_TO_DIE_TIME_SECONDS = 60;

    private final ExecutorService mainExecutorService;
    private final ScheduledExecutorService scheduledExecutorService;

    @Activate
    // ONLY MADE PUBLIC BECAUSE OF OSGI DS
    public SingletonExecutorService() {
        this.mainExecutorService = new ExtendedThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.MINUTES, workQueue, new ThreadFactoryImpl("McsConnect-WorkPool"), new ThreadPoolExecutor.CallerRunsPolicy());
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(CORE_POOL_SIZE, new ThreadFactoryImpl("McsConnect-ScheduledPool")); // DOes not support Core pool vs Max Pool. Only fixed number of threads.
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = (ScheduledThreadPoolExecutor) scheduledExecutorService; //TODO ugly cast
        scheduledThreadPoolExecutor.setRemoveOnCancelPolicy(true);
        this.scheduledExecutorService = scheduledThreadPoolExecutor;
    }

    //public static SingletonExecutorService getInstance() {
    //    return INSTANCE;
    //}

    public ExecutorService getExecutorService() {
        return mainExecutorService;
    }

    public ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

}

//    Assertion.assertPositive(corePoolSize, "Nominal core pool size must be a positive integer.");
//    Assertion.assertPositive(maxPoolSize, "Max pool size must be positive integer.");
//    Assertion.assertPositive(keepAliveTime, "Keep alive time must be a positive integer.");
//    Assertion.assertType(keepAliveTimeUnit, TimeUnit.class, "Must be of type time unit.");