package at.ac.uibk.mcsconnect.sshsessionmanager.impl.hidden.exception;

public class SshSessionException extends RuntimeException {

    private static final long serialVersionUID = -2266751008965953603L;

    public SshSessionException() {
    }

    public SshSessionException(String message) {
        super(message);
    }
    public SshSessionException(String message, Throwable cause) {
        super(message, cause);
    }

}
