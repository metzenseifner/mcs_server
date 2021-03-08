package at.ac.uibk.mcsconnect.sshsessionmanager.impl.hidden;

import at.ac.uibk.mcsconnect.sshsessionmanager.impl.hidden.exception.SshChannelException;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Returns an SSH channel that may not necessary be connected.
 *
 * The SSH session is far more time-consuming. Channels can be created
 * quickly.
 *
 * See https://tools.ietf.org/html/rfc4254
 *
 * Implementation decision: The channel object is created, but
 * must be explicitly opened using {@link Channel#connect()}.
 */
public class SshChannelFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SshChannelFactory.class);

    /**
     * Allocate a pseudo-tty (ssh -t), which joins all 3 stdio file descriptors to tty, without it, it outputs stderr
     * This also affects carriage returns and line feeds.
     * <p>
     * Example:
     * Allocation of a pseudo-tty (ssh -t) will add carriage returns
     * to each character typed, which will make the SMP react immediately.
     * <p>
     * Typing "I" for status information yields
     * I<ChA1*ChB3>*<stopped>*<internal>*<9303356>*<00:00:00>*<25:44:07>
     * <p>
     * No pseudo-tty allocation (ssh -T)
     * Typing "I" yields
     * I
     * (an explicit stroke of the return key is required to yield the status response)
     */
    public static ChannelShell create(Session session) throws SshChannelException {

        try {
            ChannelShell channelShell = (ChannelShell) session.openChannel("shell"); // type shell returns a new ChannelShell
            channelShell.setPty(true); // redundant, ChannelShell implies Pty
            channelShell.setPtyType("vt100", 80, 24, 0, 0); // tell SMP we are an old VT100 for CRLF
            return channelShell;
        } catch (JSchException j) {
            throw new SshChannelException(String.format("%s unable to create channel.", ChannelShell.class.getSimpleName()), j);
        }
    }

    private static byte[]  terminalMode = {};

}