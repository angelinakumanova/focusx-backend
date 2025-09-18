package app.focusx.exception;

public class UnverifiedUserException extends RuntimeException {

    private String email;

    public UnverifiedUserException(String message, String email) {
        super(message);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
