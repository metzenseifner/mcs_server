package at.ac.uibk.mcsconnect.http.impl.hidden;

public class PatchException extends RuntimeException {

    private static final long serialVersionUID = -8866551008965953603L;

    public PatchException() {
    }

    public PatchException(String message) {
        super(message);
    }
    public PatchException(String message, Throwable cause) {
        super(message, cause);
    }

}
