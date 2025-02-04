package no.java.submit.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import no.java.submit.lang.MailSendingException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

@Startup
@ApplicationScoped
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @ConfigProperty(name = "sendgrid.token")
    Optional<String> token;

    @ConfigProperty(name = "sendgrid.from", defaultValue = "no-reply@javazone.no")
    String from;

    private SendGrid sendGrid;

    @Location("email/login.html")
    Template loginTemplate;

    @PostConstruct
    public void init() {
        // Leave the SendGrid client un-configured in case of no SendGrid token
        if (token.isEmpty()) {
            log.warn("No email sender configured");
            return;
        }

        // Configure SendGrid client
        sendGrid = new SendGrid(token.get());
    }

    public void sendLogin(String email, String link) throws MailSendingException {
        send(new Mail(
                new Email(from),
                "Login to JavaZone Speaker Submission",
                new Email(email),
                new Content("text/html", loginTemplate.data("link", link).render())
        ));
    }

    public void send(Mail mail) throws MailSendingException {
        // Write contents of email if SendGrid client is not configured
        if (sendGrid == null) {
            log.info(mail.content.getFirst().getValue());
            return;
        }

        try {
            // Prepare request
            var request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            // Perform sending
            var response = sendGrid.api(request);

            // Verify response code
            if (response.getStatusCode() != 202) {
                throw new MailSendingException(String.format(
                        "Received error code %d while sending email", response.getStatusCode()));
            }
        } catch (IOException e) {
            throw new MailSendingException("Unable to send email", e);
        }
    }
}
