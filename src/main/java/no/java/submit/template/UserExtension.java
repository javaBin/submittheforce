package no.java.submit.template;

import io.quarkus.arc.Arc;
import io.quarkus.qute.TemplateExtension;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.security.identity.SecurityIdentity;
import no.java.submit.filter.SecurityFilter;

// Makes user information available to templates as "user:[method]"
@TemplateExtension(namespace = "user")
public class UserExtension {

    private static SecurityIdentity get() {
        return Arc.container().instance(CurrentIdentityAssociation.class).get().getIdentity();
    }

    public static boolean isAnonymous() {
        return get().isAnonymous();
    }

    public static String email() {
        return get().getPrincipal().getName();
    }

    public static boolean extension() {
        return ((SecurityFilter.MyPrincipal) get().getPrincipal()).hasExtension();
    }
}
