package no.java.submit.template;

import io.quarkus.arc.Arc;
import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal;
import io.quarkus.qute.TemplateExtension;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.security.identity.SecurityIdentity;
import no.java.submit.util.ExtensionService;
import org.eclipse.microprofile.config.ConfigProvider;

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
        return Arc.container().instance(ExtensionService.class).get().has(get());
    }

    public static boolean isAdmin() {
        var identity = get();
        if (identity.isAnonymous())
            return false;

        var admins = ConfigProvider.getConfig().getOptionalValue("app.admins", String.class).orElse("");
        var currentEmail = email();
        for (var admin : admins.split(","))
            if (admin.trim().equals(currentEmail))
                return true;
        return false;
    }
}
