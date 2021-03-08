package at.ac.uibk.mcsconnect.sshsessionmanager.impl.hidden.exception;

/**
 * The EstablishSshSessionConnectionException refers to a situation where the
 * {SshSessionManager} cannot acquire a {SshChannelShellLockable}.
 *
 * It has been created to allow clients to distinguish this situation
 * from other generic exceptions.
 *
 */
public class EstablishSshSessionConnectionException extends RuntimeException {

    private static final long serialVersionUID = -1166751008965953603L;

    public EstablishSshSessionConnectionException() {
    }

    public EstablishSshSessionConnectionException(String message) {
        super(message);
    }
    public EstablishSshSessionConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
