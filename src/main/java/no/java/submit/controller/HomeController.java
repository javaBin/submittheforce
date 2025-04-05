package no.java.submit.controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;

@Path("")
public class HomeController {

    @Inject
    Template home;

    @Inject
    Template listing;

    @GET
    @Blocking
    public TemplateInstance get(@Context SecurityIdentity securityIdentity) {
        if (securityIdentity.isAnonymous()) {
            return home.instance();
        }

        return listing.instance();
    }
}
