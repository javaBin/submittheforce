package no.java.submit.controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import no.java.submit.util.CodeHelper;
import no.java.submit.util.ExtensionService;
import no.java.submit.util.UserHelper;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Path("code")
@Blocking
@Produces(MediaType.TEXT_HTML)
@Authenticated
public class CodeController {

    @ConfigProperty(name = "app.cookie.secure", defaultValue = "true")
    boolean cookieSecure;

    @Inject
    Template code;

    @Inject
    CodeHelper codeHelper;

    @Inject
    ExtensionService extensionService;

    @Inject
    @Named("app.admins")
    List<String> appAdmins;

    @GET
    public TemplateInstance view(@Context SecurityIdentity identity) {
        return page(identity, null, null, null);
    }

    @POST
    @Path("use")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response use(@FormParam("code") String submitted, @Context SecurityIdentity identity) {
        var email = UserHelper.getEmail(identity);
        var valid = codeHelper.validate(submitted, email)
                .filter(date -> !date.isBefore(LocalDate.now()));

        if (valid.isEmpty()) {
            return Response.ok(page(identity, "The code is not valid for your account.", null, null).render()).build();
        }

        var cookie = new NewCookie.Builder(ExtensionService.COOKIE_NAME)
                .value(submitted)
                .path("/")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(NewCookie.SameSite.STRICT)
                .build();

        return Response.seeOther(UriBuilder.fromUri("/").build())
                .cookie(cookie)
                .build();
    }

    @POST
    @Path("generate")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public TemplateInstance generate(@FormParam("date") String dateStr, @FormParam("email") String rawEmail, @Context SecurityIdentity identity) {
        if (!appAdmins.contains(UserHelper.getEmail(identity)))
            throw new NotAuthorizedException("Not admin");

        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (DateTimeParseException | NullPointerException e) {
            return page(identity, "Invalid date (use YYYY-MM-DD).", null, null);
        }

        var email = rawEmail == null ? "" : rawEmail.trim();
        if (email.isBlank()) {
            return page(identity, "Email is required.", null, null);
        }

        var generated = new Generated(codeHelper.generate(date, email), date, email);
        return page(identity, null, null, generated);
    }

    private TemplateInstance page(SecurityIdentity identity, String error, String success, Generated generated) {
        return code
                .data("valid", extensionService.validFor(identity).orElse(null))
                .data("error", error)
                .data("success", success)
                .data("generated", generated)
                .data("isAdmin", appAdmins.contains(UserHelper.getEmail(identity)));
    }

    public record Generated(String code, LocalDate date, String email) {
    }
}
