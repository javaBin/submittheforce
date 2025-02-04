package no.java.submit.config;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Produces;
import no.java.submit.util.HmacHelper;

@Dependent
public class HmacConfig {

    @Inject
    @Named("app.secret")
    String appSecret;

    @Produces
    @Singleton
    public HmacHelper loadHmacHelper() {
        return new HmacHelper(appSecret);
    }

}
