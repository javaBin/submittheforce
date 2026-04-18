package no.java.submit.util;

import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal;
import io.quarkus.security.identity.SecurityIdentity;

public interface UserHelper {

    static OidcJwtCallerPrincipal getPrincipal(SecurityIdentity securityIdentity) {
        return (OidcJwtCallerPrincipal) securityIdentity.getPrincipal();
    }

    static String getEmail(SecurityIdentity securityIdentity) {
        return getPrincipal(securityIdentity).getClaim("email");
    }
}
