package no.java.submit.util;

import io.quarkus.security.identity.SecurityIdentity;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.Optional;

@RequestScoped
public class ExtensionService {

    public static final String COOKIE_NAME = "extension";

    @Inject
    CodeHelper codeHelper;

    @Inject
    RoutingContext routingContext;

    public Optional<LocalDate> validFor(SecurityIdentity identity) {
        if (identity == null || identity.isAnonymous())
            return Optional.empty();

        var cookie = routingContext.request().getCookie(COOKIE_NAME);
        if (cookie == null)
            return Optional.empty();

        return codeHelper.validate(cookie.getValue(), UserHelper.getEmail(identity))
                .filter(date -> !date.isBefore(LocalDate.now()));
    }

    public boolean has(SecurityIdentity identity) {
        return validFor(identity).isPresent();
    }
}
