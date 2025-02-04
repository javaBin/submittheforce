package no.java.submit.lang;

public class SubmitException extends Exception {
    public SubmitException(String message) {
        super(message);
    }

    public SubmitException(String message, Throwable cause) {
        super(message, cause);
    }
}
