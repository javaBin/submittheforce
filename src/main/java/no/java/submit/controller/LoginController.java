package no.java.submit.controller;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.quarkus.qute.Template;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.Validator;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import no.java.submit.form.LoginForm;
import no.java.submit.lang.SubmitException;
import no.java.submit.service.EmailService;
import no.java.submit.util.ExtensionCodeHelper;
import no.java.submit.util.HmacHelper;
import no.java.submit.util.TokenHelper;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;

@Path("login")
public class LoginController {

    @Inject
    @Named("app.url")
    String applicationUrl;

    @Inject
    @Named("cookie")
    TokenHelper cookieTokens;

    @Inject
    @Named("login")
    TokenHelper loginTokens;

    @Inject
    ExtensionCodeHelper extensionCodes;

    @Inject
    HmacHelper hmacs;

    @Inject
    EmailService emailService;

    @Inject
    Template home;

    @Inject
    Validator validator;

    @POST
    public Response post(@FormParam("email") String email) throws SubmitException {
        // Create the form
        var form = new LoginForm();
        form.email = email;

        // Validate the form
        var result = validator.validate(form);
        if (!result.isEmpty()) {
            // TODO Validation result
            return Response.ok()
                    .entity(home
                            .getFragment("form")
                            .data("form", form))
                    .build();
        }

        // Create secret
        var secret = hmacs.create(String.format("%s:%s", form.email, Instant.now()));
        var link = String.format("%s/login?token=%s", applicationUrl, secret);

        // Create magic link
        var cookie = loginTokens.create(form.email, new HashMap<>() {{
            put("secret", hmacs.create(secret));
        }});

        // Send email
        emailService.sendLogin(form.email, link);

        // Confirm email sending
        return Response.ok()
                .header("Set-Cookie", String.format("login-session=%s; Path=/", cookie))
                .entity(home.getFragment("sent").instance())
                .build();
    }

    @GET
    public Response get(@QueryParam("token") String token, @CookieParam("login-session") String cookie) {
        // Validate token
        DecodedJWT decodedJWT;
        try {
            decodedJWT = loginTokens.verify(cookie);

            // Verify secret
            var claim = decodedJWT.getClaim("secret");
            if (claim.isMissing() || !claim.asString().equals(hmacs.create(token)))
                throw new JWTVerificationException("Force");
        } catch (JWTVerificationException exception) {
            // Send user to frontpage if token is invalid
            return Response
                    .seeOther(UriBuilder.fromUri("/").build())
                    .header("Set-Cookie", "login-session=; Max-Age=0")
                    .build();
        }

        // Extract email
        var email = decodedJWT.getSubject();

        // Create cookie
        var authCookie = cookieTokens.create(email, Collections.emptyMap());

        // Return user to frontpage
        return Response
                .seeOther(UriBuilder.fromUri("/").build())
                .header("Set-Cookie", "login-session=; Max-Age=0")
                .header("Set-Cookie", String.format("auth-session=%s; Path=/", authCookie))
                .build();
    }

    @POST
    @Authenticated
    @Blocking
    @Path("extend")
    public Response extend(@FormParam("code") String code, @Context SecurityIdentity securityIdentity) {
        if (!extensionCodes.verify(securityIdentity.getPrincipal().getName(), code)) {
            return Response
                    .seeOther(UriBuilder.fromUri("/").build())
                    .build();
        }

        var cookie = cookieTokens.create(securityIdentity.getPrincipal().getName(), new HashMap<>() {{
            put("access", "extension");
        }});

        return Response
                .seeOther(UriBuilder.fromUri("/").build())
                .header("Set-Cookie", String.format("auth-session=%s; SameSite=strict; Path=/", cookie))
                .build();
    }
}
