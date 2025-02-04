package no.java.submit.template;

import io.quarkus.arc.Arc;
import io.quarkus.qute.TemplateExtension;
import no.java.submit.service.ConferenceService;
import no.java.submit.service.TalksService;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.List;

// Makes conference information available to templates as "conference:[method]"
@TemplateExtension(namespace = "conference")
public class ConferenceExtension {

    public static List<TalksService.Conference> all() {
        return Arc.container().instance(ConferenceService.class).get().all();
    }

    public static TalksService.Conference current() {
        return Arc.container().instance(ConferenceService.class).get().current();
    }
}
