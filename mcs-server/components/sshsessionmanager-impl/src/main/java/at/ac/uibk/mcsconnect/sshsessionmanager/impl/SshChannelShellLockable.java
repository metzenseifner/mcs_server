package at.ac.uibk.mcsconnect.sshsessionmanager.impl;

import at.ac.uibk.mcsconnect.common.api.CreationTimestamp;
import at.ac.uibk.mcsconnect.common.api.NetworkTarget;
import at.ac.uibk.mcsconnect.functional.common.Result;
import at.ac.uibk.mcsconnect.sshsessionmanager.impl.hidden.SshChannelFactory;
import at.ac.uibk.mcsconnect.sshsessionmanager.impl.hidden.SshSessionFactory;
import at.ac.uibk.mcsconnect.sshsessionmanager.impl.hidden.TaskRead;
import at.ac.uibk.mcsconnect.sshsessionmanager.impl.hidden.exception.SshChannelException;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.regex.Pattern;

import static at.ac.uibk.mcsconnect.common.api.StrUtils.stringToHexString;
import static at.ac.uibk.mcsconnect.common.api.StrUtils.swapNonPrintable;

/**
 * This is lockable version of the {@link Session}, which cannot be subclassed.
 * <p>
 * Only publicize this object to the {@link SshSessionServiceImpl}. No object besides
 * the manager should call methods here directly. This makes all functions here
 * effectively atomic. This object represent a session that can be shared across threads
 * by means of a manager, {@link SshSessionServiceImpl}.
 * <p>
 * Only one thread at a time may access this session.
 */
public final class SshChannelShellLockable extends CreationTimestamp {

    private static final Logger LOGGER = LoggerFactory.getLogger(SshChannelShellLockable.class);

    private static final int IN_BUFFER_SIZE = 1024;
    private static final int OUT_BUFFER_SIZE = 50;
    private static final int CONNECTION_TIMEOUT_MILLISECONDS = (int) TimeUnit.SECONDS.toMillis(15);
    private final long readTimeout = TimeUnit.SECONDS.toMillis(2);; // milliseconds
    private final TimeUnit readTimeoutUnit = TimeUnit.MILLISECONDS;
    private final NetworkTarget networkTargetUserPass;
    private final Session underlyingSession;
    private final ChannelShell channelShell;
    private final BufferedInputStream in;
    private final BufferedOutputStream out;

    // Note that the Lock (ReentrantLock) defaults to an unlocked state
    private final Lock lock = new ReentrantLock();

    /**
     * Constructor with patternAfterChannelHook Pattern
     *
     * @param channelShell
     * @param br
     * @param bw
     */
    private SshChannelShellLockable(NetworkTarget target, Session session, ChannelShell channelShell, BufferedInputStream br, BufferedOutputStream bw) {
        super();
        this.networkTargetUserPass = target;
        this.underlyingSession = session;
        this.channelShell = channelShell;
        this.in = br;
        this.out = bw;
    }

    /**
     * Default factory.
     *
     * @param target
     * @return
     */
    public static SshChannelShellLockable create(NetworkTarget target) throws IllegalStateException {

        Result<Session> session = makeSession(target);
        if (session.isFailure()) throw new IllegalStateException(session.failureValue());

        Result<ChannelShell> channelShell = setupChannel(session.successValue());
        if (channelShell.isFailure()) throw new IllegalStateException(channelShell.failureValue());

        Result<BufferedInputStream> br = channelShell.flatMap(c -> getChannelInputStream(c));
        if (channelShell.isFailure()) throw new IllegalStateException(channelShell.failureValue());

        Result<BufferedOutputStream> bw = channelShell.flatMap(c -> getChannelOutputStream(c));
        if (channelShell.isFailure()) throw new IllegalStateException(channelShell.failureValue());

        return new SshChannelShellLockable(target, session.successValue(), channelShell.successValue(), br.successValue(), bw.successValue());
    }

    Function<NetworkTarget, Session> makeSession;
    Function<Session, ChannelShell> makeChannelShell;
    Function<ChannelShell, BufferedInputStream> makeBufferedInputStream;
    Function<ChannelShell, BufferedOutputStream> makeBufferedOutputStream;

    // BEGIN NEW FUNCTION CODE
    private static Result<Session> makeSession(NetworkTarget target) {
        return Result.success(SshSessionFactory.create(target));
    }

    private static Result<ChannelShell> setupChannel(Session session) {
        return Result.of(session)
                .flatMap(SshChannelShellLockable::sessionConnect)
                .flatMap(SshChannelShellLockable::createChannel)
                .flatMap(SshChannelShellLockable::channelConnect);
    }

    private static Result<Session> sessionConnect(Session session) {
        try {
            session.connect();
            return Result.success(session);
        } catch (JSchException j) {
            return Result.failure(j);
        }
    }

    private static Result<ChannelShell> createChannel(Session session) {
        return Result.of(SshChannelFactory.create(session));
    }

    private static Result<ChannelShell> channelConnect(ChannelShell channel) {
        try {
            channel.connect();
            return Result.success(channel);
        } catch (JSchException j) {
            return Result.failure(j);
        }
    }

    private static Result<BufferedInputStream> getChannelInputStream(ChannelShell channel) {
        try {
            return Result.success(new BufferedInputStream(channel.getInputStream()));
        } catch (IOException i) {
            return Result.failure(i);
        }
    }

    private static Result<BufferedOutputStream> getChannelOutputStream(ChannelShell channel) {
        try {
            return Result.success(new BufferedOutputStream(channel.getOutputStream()));
        } catch (IOException i) {
            return Result.failure(i);
        }
    }
    // END NEW FUNCTIONAL CODE

    public String toString() {
        return String.format("%s %s:%s", this.getClass().getSimpleName(), networkTargetUserPass.getHost(), networkTargetUserPass.getPort());
    }

    /**
     * The read function reads from {@link this#out}, and is a blocking method.
     * <p>
     * This is a sensitive and critical method because it is most like block if the target does not
     * respond according to its protocol. The conditions unblock this method:
     *
     * <ul>
     * <li>finds a match in the stream.</li>
     * <li>The underlying socket is interrupted.</li>
     * </ul>
     *
     * @return
     */
    public String read(Function<String, Function<Pattern, Boolean>> bufferMatchCondition, Pattern correspondencePattern) throws SshChannelException {

        FutureTask<String> future = new FutureTask<>(new TaskRead(in, bufferMatchCondition, correspondencePattern));
        future.run();
        Result<String> response = extractRead(future);

        response.forEachOrFail(s -> LOGGER.debug(String.format("%s.read(%s, %s): %s", this, "bufferMatchCondition", correspondencePattern, swapNonPrintable(s)))).forEach(e -> LOGGER.error(e));

        return response.getOrElse("");
    }

    /**
     * Helper function for {@link SshChannelShellLockable::read}.
     *
     * @param future context holding value returned from read Operation.
     * @return the value contained.
     */
    private Result<String> extractRead(Future<String> future) {
        try {
            String response = future.get(readTimeout, readTimeoutUnit);
            return Result.success(response);
        } catch (InterruptedException i) {
            return Result.failure("Interrupted read Operation", i);
        } catch (TimeoutException t) {
            return Result.failure("Timed out read Operation", t);
        } catch (ExecutionException e) {
            return Result.failure("Failed read Operation", e);
        }
    }

     /**
     * This is the primitive write implementation that
     * writes to the ChannelShell.
     *
     * <p>
     * Only SshSessionManager should call this method using its own { SshSessionManager#send(NetworkTargetUserPass, String, String)} method.
     *
     * <p>
     * It uses a shared channel.
     *
     * @param message
     * @throws SshChannelException
     */
    public void write(String message) throws SshChannelException {
        LOGGER.trace("Creating BufferedWriter.");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out, Charset.forName("UTF-8")), OUT_BUFFER_SIZE);
        LOGGER.trace("BufferedWriter created.");
        try {
            LOGGER.trace("Writing message");
            bw.write(message, 0, message.length());
            LOGGER.trace("Flushing StrUtil.");
            bw.flush();
            LOGGER.trace("String flushed.");
        } catch (IOException i) {
            LOGGER.error(String.format("Encountered write error to %s. Message: %s", this, stringToHexString.apply(message)));
            throw new SshChannelException(String.format("Could not write message to %s", this), i);
        }
    }

    public boolean isConnected() {
        return isSessionConnected() && isChannelShellConnected() ? true : false;
    }
    private boolean isSessionConnected() {
        return underlyingSession.isConnected() ? true : false;
    }
    private boolean isChannelShellConnected() {
        return channelShell.isConnected() ? true : false;
    }


    /**
     * Defines the locking mechanism for this object.
     * <p>
     * It can either be a fail immediately Operation or fail after timeout with {@link InterruptedException}.
     * <p>
     * This is used in the lambda functions at the {@link SshSessionServiceImpl} level.
     *
     * @return
     */
    protected boolean lock() throws InterruptedException {
        return lock.tryLock(4000L, TimeUnit.MILLISECONDS);
    }

    protected void unlock() {
        lock.unlock();
    }

}
