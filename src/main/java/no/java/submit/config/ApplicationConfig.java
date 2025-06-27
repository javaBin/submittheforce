package no.java.submit.config;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Named;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Arrays;
import java.util.List;

@Dependent
public class ApplicationConfig {

    @ConfigProperty(name = "app.url", defaultValue = "http://localhost:8080")
    String appUrl;

    @ConfigProperty(name = "app.secret", defaultValue = "JavaZoneForever")
    String appSecret;

    @ConfigProperty(name = "app.admins", defaultValue = "admin@example.com")
    String[] appAdmins;

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

    @Produces
    @Named("app.admins")
    public List<String> getAppAdmins() {
        return Arrays.asList(appAdmins);
    }
}
