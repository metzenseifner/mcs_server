package at.ac.uibk.mcsconnect.sshsessionmanager.impl.hidden;

import at.ac.uibk.mcsconnect.common.api.NetworkTarget;
import at.ac.uibk.mcsconnect.sshsessionmanager.impl.hidden.exception.SshSessionException;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Represents a SSH {@link Session} that may not necessarily be connected.
 * <p>
 * The {@link Session} handles the socket.
 * <p>
 * This is an time-expensive Operation.
 * <p>
 * Before a channel can be established, a session must be connected using {@link Session#connect()}.
 * Implementation decision: I decided to separate the logic that creates
 * the object from the logic that connects the session, because recorders
 * may be off or not reachable. In this case, the session objects can still
 * be created to populate the cache.
 */
public class SshSessionFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SshSessionFactory.class);

    public static Session create(NetworkTarget networkTargetUserPass) throws SshSessionException {

        JschLogger jschLogger = new JschLogger();
        JSch.setLogger(jschLogger);
        final JSch jsch = new JSch();
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        /**
         *  These are necessary to avoid problems with latent connections being
         * closed. This tells JSCH to place a 120 second SO timeout on the
         * underlying socket. When that interrupt is received, JSCH will send a
         * keep alive message. This will repeat up to a 1000 times, which should
         * be more than enough for any long operations to prevent the socket
         * from being closed.
         *
         * SSH has a TCPKeepAlive option, but JSCH doesn't seem to ever check it:
         * session.setConfig("TCPKeepAlive", "yes");
         */
        try {
            // Configure the underlying socket of the session.
            Session session = jsch.getSession(networkTargetUserPass.getUsername(), networkTargetUserPass.getHost(), networkTargetUserPass.getPort());
            session.setPassword(networkTargetUserPass.getPassword());
            session.setConfig(config);
            session.setServerAliveInterval((int) TimeUnit.MILLISECONDS.toMillis(265)); // send a null packet to the server and expect a response within
            session.setServerAliveCountMax(0); // give up if no response after n tries
            session.setConfig("TCPKeepAlive", "yes");
            session.setConfig("StrictHostKeyChecking", "no");
            session.setTimeout(2500); // If too short, causes JSchException Auth fail (IMPORTANT VALUE FOR STABILITY) 15000 works for sure, but testing lower values for performance
            return session;
        } catch (JSchException e) {
            throw new SshSessionException(String.format("SshSessionFactory could not create SSH session for network target \"%s:%s\"", networkTargetUserPass.getHost(), networkTargetUserPass.getPort()), e);
        }
    }
}