package no.java.submit.template;

import io.quarkus.arc.Arc;
import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal;
import io.quarkus.qute.TemplateExtension;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.security.identity.SecurityIdentity;

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
        return ((OidcJwtCallerPrincipal) get().getPrincipal()).getClaim("email");
    }

    public static boolean extension() {
        // return ((SecurityFilter.MyPrincipal) get().getPrincipal()).hasExtension();
        return false;
    }
}
