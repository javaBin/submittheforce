package no.java.submit.config;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.Produces;
import no.java.submit.util.TokenHelper;

@Dependent
public class TokenConfig {

    @Inject
    @Named("app.url")
    String appUrl;

    @Inject
    @Named("app.secret")
    String appSecret;

    @Produces
    @Named("login")
    public TokenHelper loadLoginTokenHelper() {
        return new TokenHelper(appUrl, "login", appSecret, 10);
    }

    @Produces
    @Named("cookie")
    public TokenHelper loadCookieTokenHelper() {
        return new TokenHelper(appUrl, "cookie", appSecret, 8 * 60);
    }
}
