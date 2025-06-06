package no.java.submit.controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
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
import no.java.submit.util.UserHelper;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

@Path("talk")
@Authenticated
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
    Template talk;

    @Inject
    Template sessionForm;

    @Inject
    Template all;

    @Inject
    Template error;

    @Inject
    Validator validator;

    @GET
    public TemplateInstance all() {
        return all.instance();
    }

    @GET
    @Path("{sessionId}")
    public TemplateInstance view(@PathParam("sessionId") String sessionId, @Context SecurityIdentity securityIdentity) {
        var email = UserHelper.getEmail(securityIdentity);

        try {
            var session = talksService.getSession(email, sessionId);

            if (!session.containsEmail(email))
                throw new NotAuthorizedException("Not allowed to view this session");

            return talk.data("session", session);
        } catch (ClientWebApplicationException e) {
            throw new NotAuthorizedException("Not allowed to view this session", e);
        }
    }

    @GET
    @Path("new")
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
    public TemplateInstance editSession(@PathParam("sessionId") String sessionId, @Context SecurityIdentity securityIdentity) {
        var email = UserHelper.getEmail(securityIdentity);

        try {
            var session = talksService.getSession(email, sessionId);

            if (!session.containsEmail(email))
                throw new NotAuthorizedException("Not allowed to view this session");

            if (!conferenceService.current().id.equals(session.conferenceId))
                throw new NotFoundException();

            return sessionForm
                    .data("form", SessionForm.parse(session))
                    .data("val", Collections.emptyMap())
                    .data("sessionId", sessionId);
        } catch (ClientWebApplicationException e) {
            throw new NotAuthorizedException("Not allowed to view this session", e);
        }
    }

    @POST
    @Path("{sessionId}/edit")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Object editSessionPost(@PathParam("sessionId") String sessionId, SessionForm form, @Context SecurityIdentity securityIdentity) {
        // Validate form and present form if there are any errors
        var validation = validator.validate(form);
        if (!validation.isEmpty()) {
            return sessionForm
                    .data("form", form)
                    .data("val", validation.stream().collect(Collectors.groupingBy(c -> c.getPropertyPath().toString())))
                    .data("sessionId", sessionId);
        }

        // Prepare form for sending
        var session = form.asSession();
        session.postedBy = UserHelper.getEmail(securityIdentity);
        session.sessionId = sessionId;

        // Send form data
        talksService.updateSession(session.postedBy, sessionId, session);

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
