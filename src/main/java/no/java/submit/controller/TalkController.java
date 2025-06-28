package no.java.submit.controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.Validator;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import no.java.submit.form.SessionForm;
import no.java.submit.form.SpeakerForm;
import no.java.submit.model.Kind;
import no.java.submit.service.ConferenceService;
import no.java.submit.service.TalksService;
import no.java.submit.service.TimelineService;
import no.java.submit.util.SessionHelper;
import no.java.submit.util.SessionSecretHelper;
import no.java.submit.util.UserHelper;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Path("talk")
@Blocking
@Produces(MediaType.TEXT_HTML)
public class TalkController {

    @ConfigProperty(name = "talk.initial_status", defaultValue = "SUBMITTED")
    String initialStatus;

    @Inject
    ConferenceService conferenceService;

    @Inject
    TalksService talksService;

    @Inject
    TimelineService timelineService;

    @Inject
    SessionSecretHelper sessionSecrets;

    @Inject
    Template talk;

    @Inject
    Template sessionForm;

    @Inject
    Template sessionFormClosed;

    @Inject
    Template all;

    @Inject
    Template error;

    @Inject
    Validator validator;

    @Inject
    @Named("app.admins")
    List<String> appAdmins;

    @GET
    @Authenticated
    public TemplateInstance all() {
        return all.instance();
    }

    @GET
    @Path("{sessionId}")
    @Authenticated
    public TemplateInstance view(@PathParam("sessionId") String sessionId, @Context SecurityIdentity securityIdentity) {
        var email = UserHelper.getEmail(securityIdentity);

        try {
            var session = talksService.getSession(email, sessionId);

            if (!email.isEmpty() && !session.containsEmail(email) && !appAdmins.contains(email))
                throw new NotAuthorizedException("Not allowed to view this session");

            return talk
                    .data("session", session)
                    .data("secret", null);
        } catch (ClientWebApplicationException e) {
            throw new NotAuthorizedException("Not allowed to view this session", e);
        }
    }

    @GET
    @Path("{sessionId}/{secret}")
    public TemplateInstance view(@PathParam("sessionId") String sessionId, @PathParam("secret") String secret) {
        sessionSecrets.validate(sessionId, secret);

        try {
            var session = talksService.getSession("", sessionId);

            return talk
                    .data("session", session)
                    .data("secret", secret);
        } catch (ClientWebApplicationException e) {
            throw new NotAuthorizedException("Not allowed to view this session", e);
        }
    }

    @GET
    @Path("{sessionId}/secret")
    @Authenticated
    public RestResponse<?> redirectWithSecret(@PathParam("sessionId") String sessionId, @Context SecurityIdentity securityIdentity) {
        var email = UserHelper.getEmail(securityIdentity);

        if (!appAdmins.contains(email))
            throw new NotAuthorizedException("Not admin");

        return RestResponse.seeOther(UriBuilder
                .fromUri(String.format("/talk/%s/%s", sessionId, sessionSecrets.get(sessionId)))
                .build());
    }

    @GET
    @Path("new")
    @Authenticated
    public TemplateInstance newSession(@Context SecurityIdentity securityIdentity) {
        if (timelineService.isClosed(UserHelper.hasExtension(securityIdentity)))
            return error
                    .data("title", "Too late")
                    .data("message", "It is past deadline for submission of new talks.");

        var form = new SessionForm() {{
            this.kind = Kind.PRESENTATION_45;
        }};
        form.speakers = new ArrayList<>();
        form.speakers.add(new SpeakerForm() {{
            this.email = UserHelper.getEmail(securityIdentity);
        }});

        return sessionForm
                .data("form", form)
                .data("val", Collections.emptyMap())
                .data("sessionId", null);
    }

    @POST
    @Path("new")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Authenticated
    public Object newSessionPost(SessionForm form, @Context SecurityIdentity securityIdentity) {
        if (timelineService.isClosed(UserHelper.hasExtension(securityIdentity)))
            return error
                    .data("title", "Too late")
                    .data("message", "It is past deadline for submission of new talks.");

        // Validate form and present form if there are any errors
        var validation = validator.validate(form);
        if (!validation.isEmpty()) {
            return sessionForm
                    .data("form", form)
                    .data("val", validation.stream().collect(Collectors.groupingBy(c -> c.getPropertyPath().toString())))
                    .data("sessionId", null);
        }

        // Prepare form for sending
        var session = form.asSession();
        session.conferenceId = conferenceService.current().id;
        session.postedBy = UserHelper.getEmail(securityIdentity);
        session.status = initialStatus;

        // Send form data
        var newSession = talksService.createSession(session.postedBy, session.conferenceId, session);

        // Redirect to preview page
        return Response
                .seeOther(UriBuilder.fromUri("/talk/{sessionId}").build(newSession.sessionId))
                .build();
    }

    @GET
    @Path("{sessionId}/edit")
    @Authenticated
    public TemplateInstance editSession(@PathParam("sessionId") String sessionId, @Context SecurityIdentity securityIdentity) {
        var email = UserHelper.getEmail(securityIdentity);

        return editSession(sessionId, email, null);
    }

    @GET
    @Path("{sessionId}/{secret}/edit")
    public TemplateInstance editSession(@PathParam("sessionId") String sessionId, @PathParam("secret") String secret) {
        sessionSecrets.validate(sessionId, secret);

        return editSession(sessionId, "", secret);
    }

    public TemplateInstance editSession(String sessionId, String email, String secret) {
        try {
            var session = talksService.getSession(email, sessionId);

            if (!email.isEmpty() && !session.containsEmail(email) && !appAdmins.contains(email))
                throw new NotAuthorizedException("Not allowed to edit this session");

            if (!conferenceService.current().id.equals(session.conferenceId))
                throw new NotFoundException();

            return (timelineService.isClosed(appAdmins.contains(email)) ? sessionFormClosed : sessionForm)
                    .data("form", SessionForm.parse(session))
                    .data("val", Collections.emptyMap())
                    .data("sessionId", sessionId)
                    .data("secret", secret);
        } catch (ClientWebApplicationException e) {
            throw new NotAuthorizedException("Not allowed to view this session", e);
        }
    }

    @POST
    @Path("{sessionId}/edit")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Authenticated
    public Object editSessionPost(@PathParam("sessionId") String sessionId, SessionForm form, @Context SecurityIdentity securityIdentity) {
        var email = UserHelper.getEmail(securityIdentity);

        return editSessionPost(sessionId, form, email, null);
    }

    @POST
    @Path("{sessionId}/{secret}/edit")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Object editSessionPost(@PathParam("sessionId") String sessionId, @PathParam("secret") String secret, SessionForm form) {
        sessionSecrets.validate(sessionId, secret);

        return editSessionPost(sessionId, form, "", secret);
    }

    public Object editSessionPost(String sessionId, SessionForm form, String email, String secret) {
        // Validate form and present form if there are any errors
        var validation = validator.validate(form);
        if (!validation.isEmpty()) {
            return (timelineService.isClosed(appAdmins.contains(email)) ? sessionFormClosed : sessionForm)
                    .data("form", form)
                    .data("val", validation.stream().collect(Collectors.groupingBy(c -> c.getPropertyPath().toString())))
                    .data("sessionId", sessionId)
                    .data("secret", secret);
        }

        // Fetch current session
        var session = talksService.getSession(email, sessionId);

        if (!email.isEmpty() && !session.containsEmail(email) && !appAdmins.contains(email))
            throw new NotAuthorizedException("Not allowed to edit this session");

        // Prepare form for sending
        var newSession = form.asSession();

        if (timelineService.isClosed(appAdmins.contains(email))) {
            SessionHelper.partialUpdate(session, newSession);
        } else {
            // Update session with new data
            session.data = newSession.data;
            session.speakers = newSession.speakers;
        }

        // Send form data
        talksService.updateSession(session.postedBy, sessionId, session);

        if (secret != null) {
            // Redirect to preview page with secret
            return Response
                    .seeOther(UriBuilder.fromUri("/talk/{sessionId}/{secret}").build(sessionId, secret))
                    .build();
        }

        // Redirect to preview page
        return Response
                .seeOther(UriBuilder.fromUri("/talk/{sessionId}").build(sessionId))
                .build();
    }

    @GET
    @Path("new/_speaker")
    public TemplateInstance addSpeaker() {
        return sessionForm.instance().getFragment("speaker")
                .data("speaker", new SpeakerForm())
                .data("speaker_index", 0)
                .data("speaker_count", 1)
                .data("val", Collections.emptyMap());
    }
}
