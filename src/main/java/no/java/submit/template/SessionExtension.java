package no.java.submit.template;

import io.quarkus.qute.TemplateExtension;
import no.java.submit.service.TalksService;
import no.java.submit.util.SessionHelper;

import java.util.List;

@TemplateExtension(namespace = "session")
public class SessionExtension {

    public static List<String> tags(TalksService.Session session) {
        return SessionHelper.getTags(session);
    }
}
