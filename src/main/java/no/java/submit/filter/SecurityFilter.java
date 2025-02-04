package no.java.submit.filter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.ext.Provider;
import no.java.submit.util.TokenHelper;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;

/**
 * https://quarkus.io/guides/security-customization#jaxrs-security-context
 */
@Provider
@PreMatching
public class SecurityFilter implements ContainerRequestFilter {

    @Inject
    @Named("cookie")
    TokenHelper cookieTokens;

    @Override
    public void filter(ContainerRequestContext context) {
        // Look for the expected session key
        if (!context.getCookies().containsKey("auth-session"))
            return;

        // Fetch token
        var cookie = context.getCookies().get("auth-session").getValue();

        // Decode token
        DecodedJWT decodedJWT;
        try {
            decodedJWT = cookieTokens.verify(cookie);
        } catch (JWTVerificationException exception) {
            // Remove cookie and return to homepage when invalid
            context.abortWith(Response
                    .seeOther(UriBuilder.fromUri("/").build())
                    .header("Set-Cookie", "auth-session=; Max-Age=0")
                    .build());
            return;
        }

        var email = decodedJWT.getSubject();
        var access = decodedJWT.getClaim("access").isMissing() ? Collections.emptyList() : Arrays.asList(decodedJWT.getClaim("access").asString().split(" "));
        var principal = new MyPrincipal(email, access.contains("extension"));

        // Configure user
        context.setSecurityContext(new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return principal;
            }

            @Override
            public boolean isUserInRole(String s) {
                return access.contains(s);
            }

            @Override
            public boolean isSecure() {
                return false;
            }

            @Override
            public String getAuthenticationScheme() {
                return "basic";
            }
        });
    }

    public class MyPrincipal implements Principal {

        private String name;

        private boolean extension;

        public MyPrincipal(String name, boolean extension) {
            this.name = name;
            this.extension = extension;
        }

        @Override
        public String getName() {
            return name;
        }

        public boolean hasExtension() {
            return extension;
        }
    }
}
