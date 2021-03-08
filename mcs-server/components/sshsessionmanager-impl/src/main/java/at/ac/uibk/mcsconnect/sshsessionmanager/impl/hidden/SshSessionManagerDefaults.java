package at.ac.uibk.mcsconnect.sshsessionmanager.impl.hidden;

import java.sql.Time;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public abstract class SshSessionManagerDefaults {

    public static final int CONNECTION_TIMEOUT = 15; // affects thread interrupt time if connect() already run
    public static final ChronoUnit CONNECTION_TIMEOUT_UNIT = ChronoUnit.MILLIS;
    public static final int ACCESS_SSH_SESSION_MAX_TRIES = 3;
    public static final int ACCESS_RETRY_SLEEP_TIME = 150;
    public static final TimeUnit ACCESS_RETRY_SLEEP_UNIT = TimeUnit.MILLISECONDS;
    public static final int SSH_SESSION_MAX_AGE = 10;
    public static final ChronoUnit SSH_SESSION_MAX_AGE_UNIT = ChronoUnit.MINUTES;

    // Optimize CORE_POOL_SIZE and MAX_POOLSIZE to the system
    public static int N_CPUS = Runtime.getRuntime().availableProcessors();
    public static final int CORE_POOL_SIZE = N_CPUS;
    public static final int MAX_POOL_SIZE = N_CPUS * 2; // TODO Consider this hard. If SMPs are offline, a crazy amnt of threads will actually help.
    public static final int KEEP_ALIVE_TIME = 5;
    public static final ChronoUnit KEEP_ALIVE_TIME_UNIT = ChronoUnit.MINUTES;
    public static final int AWAIT_FOR_THREAD_TO_DIE_TIME = 60;
    public static final TimeUnit AWAIT_FOR_THREAD_TO_DIE_TIME_UNIT = TimeUnit.SECONDS;
}
