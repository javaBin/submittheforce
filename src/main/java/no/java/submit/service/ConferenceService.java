package no.java.submit.service;

import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Startup
@Singleton
public class ConferenceService {

    private static final Logger log = LoggerFactory.getLogger(ConferenceService.class);

    @ConfigProperty(name = "conference.current")
    String currentConferenceId;

    @Inject
    TalksService talksService;

    private TalksService.ConferencesResponse response;

    @PostConstruct
    public void init() {
        response = talksService.conferences();

        log.info("Current conference: {}", response.id(currentConferenceId).name);
    }

    public List<TalksService.Conference> all() {
        return response.conferences;
    }

    public TalksService.Conference current() {
        return response.id(currentConferenceId);
    }
}
