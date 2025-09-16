package app.focusx.exception;

public class UnverifiedUserException extends RuntimeException {
    public UnverifiedUserException(String message) {
        super(message);
    }
}
