package no.java.submit.config;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Named;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Dependent
public class ApplicationConfig {

    @ConfigProperty(name = "app.url", defaultValue = "http://localhost:8080")
    String appUrl;

    @ConfigProperty(name = "app.secret", defaultValue = "JavaZoneForever")
    String appSecret;

    @Produces
    @Named("app.url")
    public String getAppUrl() {
        return appUrl;
    }

    @Produces
    @Named("app.secret")
    public String getAppSecret() {
        return appSecret;
    }
}
