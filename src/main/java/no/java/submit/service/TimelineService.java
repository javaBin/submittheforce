package no.java.submit.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Named("timeline")
@ApplicationScoped
public class TimelineService {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d");

    @ConfigProperty(name = "timeline.opening")
    public Instant opening;

    @ConfigProperty(name = "timeline.closing")
    Instant closing;

    @ConfigProperty(name = "timeline.finalized")
    Instant finalized;

    @ConfigProperty(name = "timeline.feedback")
    Instant feedback;

    @ConfigProperty(name = "timeline.refund")
    Instant refund;

    public LocalDate getOpening() {
        return LocalDate.ofInstant(opening, ZoneId.systemDefault());
    }

    public LocalDate getClosing() {
        return LocalDate.ofInstant(closing, ZoneId.systemDefault());
    }

    public LocalDateTime getClosingHard() {
        return LocalDateTime.ofInstant(closing.plus(24, ChronoUnit.HOURS), ZoneId.systemDefault());
    }

    public LocalDate getFeedback() {
        return LocalDate.ofInstant(feedback, ZoneId.systemDefault());
    }

    public LocalDate getRefund() {
        return LocalDate.ofInstant(refund, ZoneId.systemDefault());
    }

    public boolean isClosed(boolean hasExtension) {
        if (hasExtension)
            return false;

        return getClosingHard().isBefore(LocalDateTime.now());
    }

    public boolean isFinalized() {
        return finalized != null && finalized.isBefore(Instant.now());
    }

    public String format(LocalDate date) {
        return formatter.format(date).substring(0, 1).toUpperCase() +
                formatter.format(date).substring(1) +
                switch (date.getDayOfMonth()) {
                    case 1, 21, 31 -> "st";
                    case 2, 22 -> "nd";
                    case 3, 23 -> "rd";
                    default -> "th";
                };
    }
}
