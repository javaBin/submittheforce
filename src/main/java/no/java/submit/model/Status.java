package no.java.submit.model;

import io.quarkus.qute.TemplateEnum;

@TemplateEnum
public enum Status {
    DRAFT("Draft"),
    SUBMITTED("Submitted"),
    ACCEPTED("Accepted"),
    REJECTED("Rejected");

    public String title;

    Status(String title) {
        this.title = title;
    }
}
