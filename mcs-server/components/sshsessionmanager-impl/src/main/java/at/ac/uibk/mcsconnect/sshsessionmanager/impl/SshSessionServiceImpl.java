package at.ac.uibk.mcsconnect.sshsessionmanager.impl;

import at.ac.uibk.mcsconnect.common.api.CancellableRunnable;
import at.ac.uibk.mcsconnect.common.api.NetworkTarget;
import at.ac.uibk.mcsconnect.common.api.OsgiProperties;
import at.ac.uibk.mcsconnect.common.api.OsgiProperty;
import at.ac.uibk.mcsconnect.common.api.Preparable;
import at.ac.uibk.mcsconnect.common.api.PreparableCacheImpl;
import at.ac.uibk.mcsconnect.common.api.PreparableFactory;
import at.ac.uibk.mcsconnect.common.api.StrUtils;
import at.ac.uibk.mcsconnect.common.api.ThrowingFunction;
import at.ac.uibk.mcsconnect.executorservice.api.McsScheduledExecutorService;
import at.ac.uibk.mcsconnect.executorservice.api.McsSingletonExecutorService;
import at.ac.uibk.mcsconnect.functional.common.Result;
import at.ac.uibk.mcsconnect.functional.osgi.OsgiPropertyReader;
import at.ac.uibk.mcsconnect.sshsessionmanager.api.SshSessionManagerService;
import at.ac.uibk.mcsconnect.sshsessionmanager.impl.hidden.SshCacheType;
import at.ac.uibk.mcsconnect.sshsessionmanager.impl.hidden.SshSessionManagerDefaults;
import at.ac.uibk.mcsconnect.sshsessionmanager.impl.hidden.exception.SshChannelException;
import at.ac.uibk.mcsconnect.sshsessionmanager.impl.hidden.exception.SshSessionException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.temporal.ChronoUnit;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Controller for SSH sessions.
 *
 *
 * <p>
 * The way to return from the {@link this#serialAccessSessionCache} is through the access method,
 * which applies strict rules as to how an object is to be
 * accessed so that it is thread safe.
 * <p>
 * Keep me thread safe:
 * DO NOT ALLOW REFERENCES TO SSH CHANNELS ESCAPE USING ACCESSORS
 *
 * <h1>Included tasks</h1>
 * <ul>
 * <li>Opens SSH sessions with their corresponding channels.</li>
 * <li>Ensures concurrent access to SSH channel cache.</li>
 * <li>Ensures atomic access to SSH channels.</li>
 * </ul>
 *
 * <p>
 * This class manages contract between the producers and consumers.
 * It might also be called SshChannelService, because it provides
 * safe thread access to shared objects in the map. It should be in
 * the same package as the objects it owns:
 * <p>
 * Objects in the {@link SshSessionServiceImpl#serialAccessSessionCache} uphold the one-time publication safety rules
 * necessary for sharing mutable objects.
 *
 * <ul>
 * <li>@see SshChannelShellLockable</li>
 * </ul>
 * <p>
 * Each connection is associated with a key-value object that supports
 * SSH sessions.
 * The purpose it to reduce the expense of opening new connections
 * for future communications. Each connection can be acquired from
 * a map using a hostname.
 */
@Component(
        name = "at.ac.uibk.mcsconnect.sshsessionmanager.impl.SshSessionServiceImpl"
)
public class SshSessionServiceImpl implements SshSessionManagerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SshSessionServiceImpl.class);

    // Config Keys
    private static final String CFG_CONNECTION_TIMEOUT = "ssh.session.timeout"; // int
    private static final String CFG_CONNECTION_TIMEOUT_UNIT = "ssh.session.timeout.unit"; // TimeUnit
    private static final String CFG_ACCESS_SSH_SESSION_MAX_TRIES = "ssh.session.max.tries"; // int
    private static final String CFG_ACCESS_RETRY_SLEEP = "ssh.session.retry.interval"; // int
    private static final String CFG_ACCESS_RETRY_SLEEP_UNIT = "ssh.session.retry.interval.unit"; // TimeUnit
    private static final String CFG_SSH_SESSION_MAX_AGE = "ssh.session.max.age"; // int
    private static final String CFG_SSH_SESSION_MAX_AGE_UNIT = "ssh.session.max.age.unit"; // TimeUnit
    private static final String CFG_CORE_POOL_SIZE = "ssh.core.pool.size"; // int
    private static final String CFG_KEEP_ALIVE_TIME = "ssh.keep.alive.max.time"; // int
    private static final String CFG_KEEP_ALIVE_TIME_UNIT = "ssh.keep.alive.max.time.unit"; //TimeUnit
    private static final String CFG_AWAIT_FOR_THREAD_TO_DIE_TIMEOUT = "ssh.thread.timeout";
    private static final String CFG_AWAIT_FOR_THREAD_TO_DIE_TIMEOUT_UNIT = "ssh.thread.timeout";

    private final OsgiProperties osgiProperties = OsgiProperties.create(
            OsgiProperty.create(CFG_CONNECTION_TIMEOUT,
                    s -> r -> r.getAsInt(s).getOrElse(SshSessionManagerDefaults.CONNECTION_TIMEOUT),
                    this::setConnectionTimeout),
            OsgiProperty.create(CFG_CONNECTION_TIMEOUT_UNIT,
                    s -> r -> r.getAsChronoUnit(s).getOrElse(SshSessionManagerDefaults.CONNECTION_TIMEOUT_UNIT),
                    this::setConnectionTimoutUnit,
                    false),
            OsgiProperty.create(CFG_ACCESS_SSH_SESSION_MAX_TRIES,
                    s -> r -> r.getAsInt(s).getOrElse(SshSessionManagerDefaults.ACCESS_SSH_SESSION_MAX_TRIES),
                    this::setAccessSshSessionMaxTries),
            OsgiProperty.create(CFG_ACCESS_RETRY_SLEEP,
                    s -> r -> r.getAsInt(s).getOrElse(SshSessionManagerDefaults.ACCESS_RETRY_SLEEP_TIME),
                    this::setAccessSshSessionRetrySleep),
            OsgiProperty.create(CFG_ACCESS_RETRY_SLEEP_UNIT,
                    s -> r -> r.getAsTimeUnit(s).getOrElse(SshSessionManagerDefaults.ACCESS_RETRY_SLEEP_UNIT),
                    this::setAccessSshSessionRetrySleepUnit),
            OsgiProperty.create(CFG_SSH_SESSION_MAX_AGE,
                    s -> r -> r.getAsInt(s).getOrElse(SshSessionManagerDefaults.SSH_SESSION_MAX_AGE),
                    this::setSshSessionMaxAge),
            OsgiProperty.create(CFG_SSH_SESSION_MAX_AGE_UNIT,
                    s -> r -> r.getAsChronoUnit(s).getOrElse(SshSessionManagerDefaults.SSH_SESSION_MAX_AGE_UNIT),
                    this::setSshSessionMaxAgeUnit),
            OsgiProperty.create(CFG_CORE_POOL_SIZE,
                    s -> r -> r.getAsInt(s).getOrElse(SshSessionManagerDefaults.CORE_POOL_SIZE),
                    this::setCorePoolSize),
            OsgiProperty.create(CFG_KEEP_ALIVE_TIME,
                    s -> r -> r.getAsInt(s).getOrElse(SshSessionManagerDefaults.KEEP_ALIVE_TIME),
                    this::setKeepAliveTime),
            OsgiProperty.create(CFG_KEEP_ALIVE_TIME_UNIT,
                    s -> r -> r.getAsChronoUnit(s).getOrElse(SshSessionManagerDefaults.KEEP_ALIVE_TIME_UNIT),
                    this::setKeepAliveUnit),
            OsgiProperty.create(CFG_AWAIT_FOR_THREAD_TO_DIE_TIMEOUT,
                    s -> r -> r.getAsInt(s).getOrElse(SshSessionManagerDefaults.AWAIT_FOR_THREAD_TO_DIE_TIME),
                    this::setAwaitForThreadToDieTimeout),
            OsgiProperty.create(CFG_AWAIT_FOR_THREAD_TO_DIE_TIMEOUT_UNIT,
                    s -> r -> r.getAsTimeUnit(s).getOrElse(SshSessionManagerDefaults.AWAIT_FOR_THREAD_TO_DIE_TIME_UNIT),
                    this::setAwaitForThreadToDieTimeoutUnit)
    );

    // defaults
    private int connectionTimeout;
    private ChronoUnit connectionTimoutUnit;
    private int accessSshSessionMaxTries;
    private int accessSshSessionRetrySleep;
    private TimeUnit accessSshSessionRetrySleepUnit;
    private int sshSessionMaxAge;
    private ChronoUnit sshSessionMaxAgeUnit;
    private int corePoolSize;
    private int keepAliveTime;
    private ChronoUnit keepAliveUnit;
    private int awaitForThreadToDieTimeout;
    private TimeUnit awaitForThreadToDieTimeoutUnit;

    private McsSingletonExecutorService mcsSingletonExecutorService;
    private McsScheduledExecutorService mcsScheduledExecutorService;
    private PreparableFactory<NetworkTarget, SshChannelShellLockable> preparableFactory;
    private Pattern greeter;
    /**
     *  This is a new version that uses the {@link PreparableFactory} to create its {@link Preparable}.
     *  This cache provides one session per network target. Blocks until released. It is the main cache.
     *  THE CONDITIONAL IS THE FIRST LINE OF DEFENSE FOR THE CACHE (and prob makes defensive code against disconnections in SshChannelShellLockable obsolete)
     */
    private Preparable<NetworkTarget, SshChannelShellLockable> serialAccessSessionCache;


    @Activate
    public SshSessionServiceImpl(Map<String, ?> properties, @Reference McsSingletonExecutorService mcsSingletonExecutorService, @Reference McsScheduledExecutorService mcsScheduledExecutorService) {
        OsgiPropertyReader reader = OsgiPropertyReader.create(properties);
        Pattern greeter = reader.getAsPattern("at.ac.uibk.mcsconnect.sshsessionmanager.greeter.pattern").getOrElse(Pattern.compile(""));
        this.greeter = greeter;
        this.mcsSingletonExecutorService = mcsSingletonExecutorService;
        this.mcsScheduledExecutorService = mcsScheduledExecutorService;
        this.serialAccessSessionCache = new PreparableCacheImpl<>(
                                prepareSession,
                                (sshChannelShellLockable) ->
                                        sshChannelShellLockable.isConnected()
                                                && sshChannelShellLockable.isNotOlderThan(sshSessionMaxAge, sshSessionMaxAgeUnit));
        handleConfig(properties);
    }

    //@Modified
    public void modified(Map<String, ?> properties) {
        handleConfig(properties);
    }

    private void handleConfig(Map<String, ?> properties) {
        osgiProperties.resolve(properties, OsgiProperties.LogLevel.INFO);
    }

    /**
     * Algorithm to initialize an SSH connection.
     * It is possible to remove any tty greeter here.
     */
    private final Preparable<NetworkTarget, SshChannelShellLockable> prepareSession = new Preparable<NetworkTarget, SshChannelShellLockable>() {
        public Result<SshChannelShellLockable> prepare(NetworkTarget networkTargetUserPass) {
            try {
                SshChannelShellLockable channel = SshChannelShellLockable.create(networkTargetUserPass);
                channel.read(responseString -> p -> p.matcher(responseString).find(), greeter);
                return Result.success(channel);
            } catch (IllegalStateException i) {
                return Result.failure(i);
            }
        }
    };

    // This cache provides one session per network target. Blocks until released. It is the main cache. THE CONDITIONAL IS THE FIRST LINE OF DEFENSE FOR THE CACHE (and prob makes defensive code against disconnections in SshChannelShellLockable obsolete)
     //private final Preparable<NetworkTarget, SshChannelShellLockable> serialAccessSessionCache =
     //        new PreparableCache<>(
     //                prepareSession,
     //                (sshChannelShellLockable) ->
     //                        sshChannelShellLockable.isConnected()
     //                                && sshChannelShellLockable.isNotOlderThan(sessionMaxAge, sessionMaxAgeUnit));

    Map<CancellableRunnable, Future<?>> cancellableFutures = new HashMap<>();

    private static BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(); // LinkedBlockingQueue and ArrayBlockingQueue use FIFO order

    //private final ExecutorService mainExecutorService = mcsExecutorService.getExecutorService();
    //private final ScheduledExecutorService scheduledExecutorService = mcsExecutorService.getScheduledExecutorService();


    public String toString() {
        return "SSHSessionManager";
    }

    //ThreadScheduleConnect consumer = new ThreadScheduleConnect(workQueue, channelCache);


    // volatile ensures compile does not pull value from cpu cache, rather from main memory such that all reads happen after writes are completed
    //private static volatile SshSessionServiceImpl instance = null;


    /**
     * Factory for this class that ensures a singleton instance.
     *
     * @return
     */
    //public static SshSessionServiceImpl getSingletonInstance() {
    //    // only synchronize after null-check (if no instance exists) for better performance
    //    if (instance == null) {
    //        synchronized (SshSessionServiceImpl.class) {
    //            if (instance == null) {
    //                instance = new SshSessionServiceImpl(); // TODO use indirection to remove dep on central config
    //            }
    //        }
    //    }
    //    return instance;
    //}

    /**
     * Shutdown {@link this} manager.
     * <p>
     * There are two policies that work together:
     * <ul>
     *     <li>Task Cancellation Policy</li>
     *     <li>Thread Interruption Policy</li>
     * </ul>
     * <p>
     * The <strong>Task Cancellation Policy</strong> is simply a cancel() method in
     * Runnables or Cancellables that executes some code. {@link #stop()}
     * is a client for the cancel() method.
     * <p>
     * The <strong>Thread Interruption Policy</strong> is more complicated because its implementation
     * must pervade the callstack of a task. Also, the way methods in this project
     * can be interrupted were intentionally designed to conform to the interrupt()
     * functionality of Java blocking functions.
     *
     * <p>
     * Remember that shutdown() only stops new tasks from being submitted.
     * This means that outstanding tasks will be processed. They can be
     * processed more quickly if they check the interrupt() flag.
     * <p>
     * I implement this using a callback method {@link Future#cancel(boolean)}
     * made possible by keeping a reference to each task as a {@link Future}.
     * <p>
     * <p>
     * TODO: Close all sockets (currently the app waits until the timeout)
     */
    public void stop() {
        try {
            mcsScheduledExecutorService.getScheduledExecutorService().shutdown();
            mcsSingletonExecutorService.getExecutorService().shutdown();
            for (Future task : cancellableFutures.values()) {
                task.cancel(true);
            }
            if (!mcsSingletonExecutorService.getExecutorService().awaitTermination(this.awaitForThreadToDieTimeout, this.awaitForThreadToDieTimeoutUnit) || !mcsScheduledExecutorService.getScheduledExecutorService().awaitTermination(this.awaitForThreadToDieTimeout, this.awaitForThreadToDieTimeoutUnit)) {
                LOGGER.warn("{} could not properly shutdown {} or {}. Forced a shutdown of all threads occurred.", this, mcsSingletonExecutorService.getExecutorService(), mcsScheduledExecutorService.getScheduledExecutorService());
                mcsScheduledExecutorService.getScheduledExecutorService().shutdownNow();
                mcsSingletonExecutorService.getExecutorService().shutdownNow();
            }
        } catch (InterruptedException e) {
            LOGGER.error("{} was interrupted while attempting to safely shut down.", this);
        }
    }

    /**
     * Private constructor for singleton class.
     */
    private SshSessionServiceImpl() {
    }

    /**
     * MOST IMPORTANT: Used by other methods to control mutually-exclusive access to SSH connection sessions,
     * and indirectly the session's channels.
     * <p>
     * The most important aspect of this function is the parameter {@param atomicAction},
     * which can be passed as a lambda function.
     * <p>
     * Purposely do not catch {@link InterruptedException} and allow to propagate up the stack.
     * <p>
     * Update: This method effectively implements {@link Result} termination of the Result created by {@link SshSessionServiceImpl#prepareSession}.
     *
     * @param networkTargetUserPass the key of the {@link SshSessionServiceImpl#serialAccessSessionCache}. Serves as objectId of the shared (between threads) object.
     * @param atomicAction          The asynchronously accessed object that should be locked + any actions it should undergo as a function.
     * @param lock                  the function to lock {@link SshChannelShellLockable}
     * @param unlock                the function to unlock the object {@link SshChannelShellLockable}
     * @return a locked {@link SshChannelShellLockable} object
     */
    private String access(NetworkTarget networkTargetUserPass,
                          Function<SshChannelShellLockable, String> atomicAction,
                          Function<SshChannelShellLockable, Boolean> lock,
                          Consumer<SshChannelShellLockable> unlock)
            throws InterruptedException, ConcurrentModificationException, IllegalStateException {
        LOGGER.debug("{}.access() called for network target: {}", this, networkTargetUserPass);
        Result<SshChannelShellLockable> sshSessionLockableResult = serialAccessSessionCache.prepare(networkTargetUserPass); // most powerful part!
        int TRY_COUNT = 0;
        do {
            TRY_COUNT++;
            if (sshSessionLockableResult.isSuccess()) {
                LOGGER.debug(String.format("%s.access() succeeded connecting to %s. Try %s of %s.", this, networkTargetUserPass, TRY_COUNT, this.accessSshSessionMaxTries));
                break;
            }
            LOGGER.debug(String.format("%s.access(%s) failed on attempt %s of %s. Retry after %s milliseconds.", this, networkTargetUserPass, TRY_COUNT, this.accessSshSessionMaxTries, this.accessSshSessionRetrySleep, this.accessSshSessionRetrySleepUnit));
            //Thread.sleep(ACCESS_RETRY_SLEEP_MILLISECONDS); // 5 seconds in milliseconds
            this.accessSshSessionRetrySleepUnit.sleep(this.accessSshSessionRetrySleep);
            sshSessionLockableResult = serialAccessSessionCache.prepare(networkTargetUserPass);
        } while (TRY_COUNT < this.accessSshSessionMaxTries);
        try {


            SshChannelShellLockable sshChannelShellLockable = sshSessionLockableResult.getOrElse(() -> defaultLastResort()); // TODO prevent Failure subtype here, throws IllegalStateException, but this should not be possible

            LOGGER.debug("{} attempting to acquire lock on session for {}", this, networkTargetUserPass);
            if (lock.apply(sshChannelShellLockable)) { // runs tryLock()
                try {
                    LOGGER.debug("{} acquired lock on session {}", this, networkTargetUserPass);
                    return atomicAction.apply(sshChannelShellLockable);
                } finally {
                    LOGGER.debug("{} releasing lock on session for {}", this, networkTargetUserPass);
                    unlock.accept(sshChannelShellLockable);
                }
            } else {
                throw new ConcurrentModificationException(String.format("%s failed to acquire lock on session for network target \"%s\" is locked. Try again later.", this, networkTargetUserPass));
            }

        } catch (IllegalStateException i) {
            throw new IllegalStateException(String.format("%s.access() unresponsive.", this), i);
        }
    }

    private final static SshChannelShellLockable defaultLastResort() {
        throw new RuntimeException("SSH Channel Shell could not be opened.");
    }

    /**
     * Send message over a {@link SshChannelShellLockable} object.
     * This should ideally not be called directly. This should be
     * called indirectly by concrete recorder implementations such that
     * only a predefined set of messages/responses can be used.
     *
     * <p>
     * If the {@link SshChannelShellLockable} does not exist for the given {@link NetworkTarget},
     * then a new instance will be created by {@link this#access(NetworkTarget, Function, Function, Consumer)}.
     *
     * @param message               the StrUtil to send over the channel.
     * @param dataExtractionPattern the StrUtil that is expected. Because this is a shell, all text send and received is included.
     * @param networkTargetUserPass the key of the {@link SshSessionServiceImpl#serialAccessSessionCache}.
     * @return shallow copy of {@link SshChannelShellLockable} object (copies reference)
     * @throws InterruptedException is caught and rethrown so that the caller thread can detect an interrupt.
     */
    public String send(NetworkTarget networkTargetUserPass, String message, Function<String, Function<Pattern, Boolean>> bufferMatchCondition, Pattern dataExtractionPattern) throws InterruptedException {
        LOGGER.debug(String.format("%s.send() called to send %s message with expected correspondence: %s",
                this, networkTargetUserPass,
                StrUtils.swapNonPrintable(message),
                dataExtractionPattern.toString()));
        return access(networkTargetUserPass,
                sshChannelShellLockable -> {
                    LOGGER.debug(String.format("%s sending message to network target %s: %s", this, networkTargetUserPass, StrUtils.swapNonPrintable(message)));
                    try {
                        sshChannelShellLockable.write(message); // WRITE TO CHANNEL
                    } catch (SshChannelException e) {
                        throw new SshChannelException(e.getMessage(), e.getCause());
                    }
                    LOGGER.debug(String.format("%s reading response from %s to message %s", this, networkTargetUserPass, StrUtils.swapNonPrintable(message)));
                    try {
                        //Function<String, Function<Pattern, Boolean>>
                        return sshChannelShellLockable.read(bufferMatchCondition, dataExtractionPattern); // READ FROM CHANNEL
                    } catch (SshChannelException c) {
                        throw new SshChannelException(c.getMessage(), c.getCause());
                    }
                },
                ThrowingFunction.unchecked(sshSessionLockable -> sshSessionLockable.lock()),
                sshSessionLockable -> sshSessionLockable.unlock()
        );
    }

    /**
     * Add new {@link SshChannelShellLockable} to the
     * {@link SshSessionServiceImpl#serialAccessSessionCache} by providing
     * a network target.
     * <p>
     * This is how the {@link this#serialAccessSessionCache} is prepopulated. Of course, it is somewhat
     * redundant, because {@link this#access(NetworkTarget, Function, Function, Consumer)} will attempt to add a
     * new object upon first request.
     * <p>
     * PreparableCache auto-creates missing {@link SshChannelShellLockable} so we utilize this and there is not much work to do.
     *
     * @param networkTargetUserPass
     */
    public void putIfAbsent(NetworkTarget networkTargetUserPass) throws SshSessionException, InterruptedException { // pass InterruptedException up stack
        LOGGER.debug(String.format("%s.putIfAbsent(%s)", this, networkTargetUserPass));
        access(networkTargetUserPass,
                sshSessionLockable -> "", // only need to return itself (no calls)
                ThrowingFunction.unchecked(sshSessionLockable -> sshSessionLockable.lock()),
                sshSessionLockable -> sshSessionLockable.unlock()
        );
    }

    /**
     * Overloaded putIfAbsent to allow control over which cache
     *
     * @param networkTargetUserPass
     * @param sshCacheType
     * @throws SshSessionException
     * @throws InterruptedException
     */
    public void putIfAbsent(NetworkTarget networkTargetUserPass, SshCacheType sshCacheType) throws SshSessionException, InterruptedException {
        LOGGER.debug(String.format("%s.putIfAbsent(NetworkTargetUserPass: %s,SshCacheType: %s) called.", this, networkTargetUserPass, sshCacheType));
    }

    public ExecutorService getMainExecutorService() {
        return mcsSingletonExecutorService.getExecutorService();
    }

    public ScheduledExecutorService getScheduledExecutorService() {
        return mcsScheduledExecutorService.getScheduledExecutorService();
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public void setConnectionTimoutUnit(ChronoUnit connectionTimoutUnit) {
        this.connectionTimoutUnit = connectionTimoutUnit;
    }

    public void setAccessSshSessionMaxTries(int accessSshSessionMaxTries) {
        this.accessSshSessionMaxTries = accessSshSessionMaxTries;
    }

    public void setAccessSshSessionRetrySleep(int accessSshSessionRetrySleep) {
        this.accessSshSessionRetrySleep = accessSshSessionRetrySleep;
    }

    public void setAccessSshSessionRetrySleepUnit(TimeUnit accessSshSessionRetrySleepUnit) {
        this.accessSshSessionRetrySleepUnit = accessSshSessionRetrySleepUnit;
    }

    public void setSshSessionMaxAge(int sshSessionMaxAge) {
        this.sshSessionMaxAge = sshSessionMaxAge;
    }

    public void setSshSessionMaxAgeUnit(ChronoUnit sshSessionMaxAgeUnit) {
        this.sshSessionMaxAgeUnit = sshSessionMaxAgeUnit;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public void setKeepAliveTime(int keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    public void setKeepAliveUnit(ChronoUnit keepAliveUnit) {
        this.keepAliveUnit = keepAliveUnit;
    }

    public void setAwaitForThreadToDieTimeout(int awaitForThreadToDieTimeout) {
        this.awaitForThreadToDieTimeout = awaitForThreadToDieTimeout;
    }

    public void setAwaitForThreadToDieTimeoutUnit(TimeUnit awaitForThreadToDieTimeoutUnit) {
        this.awaitForThreadToDieTimeoutUnit = awaitForThreadToDieTimeoutUnit;
    }
}
