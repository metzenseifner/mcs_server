package at.ac.uibk.mcsconnect.sshsessionmanager.impl.hidden.exception;

public class SshChannelException extends RuntimeException {

    private static final long serialVersionUID = -8866751008965953603L;

    public SshChannelException() {
    }

    public SshChannelException(String message) {
        super(message);
    }
    public SshChannelException(String message, Throwable cause) {
        super(message, cause);
    }
}
