package no.java.submit.config;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Produces;
import no.java.submit.service.ConferenceService;
import no.java.submit.util.ExtensionCodeHelper;

@Dependent
public class ExtensionCodeConfig {

    @Inject
    @Named("app.secret")
    String appSecret;

    @Inject
    ConferenceService conferenceService;

    @Produces
    @Singleton
    public ExtensionCodeHelper loadExtensionCodeHelper() {
        return new ExtensionCodeHelper(appSecret, conferenceService.current().id);
    }
}
