package no.java.submit.lang;

public class MailSendingException extends SubmitException {
    public MailSendingException(String message) {
        super(message);
    }

    public MailSendingException(String message, Throwable cause) {
        super(message, cause);
    }
}
