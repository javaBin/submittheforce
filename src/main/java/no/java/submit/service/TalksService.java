package no.java.submit.service;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheKey;
import io.quarkus.cache.CacheResult;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import no.java.submit.config.TalksConfig;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// Initiated by TalksConfig
@SuppressWarnings("unused")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RegisterClientHeaders(TalksConfig.class)
public interface TalksService {

    class ConferencesResponse {
        public List<Conference> conferences;
        public Map<String, Conference> byId;

        public ConferencesResponse(List<Conference> conferences) {
            this.conferences = conferences;
            this.byId = conferences.stream()
                    .collect(Collectors.toMap(c -> c.id, Function.identity()));
        }

        public Conference id(String id) {
            return byId.get(id);
        }
    }

    class Conference {
        public String id, slug, name;

        public String getName() {
            return name.replace("Javazone", "JavaZone");
        }
    }

    class SessionsResponse {
        public List<Session> sessions;
        public Map<String, List<Session>> sessionsByConference;
        public Map<String, Session> sessionsById;


        public SessionsResponse(List<Session> sessions) {
            this.sessions = sessions;
            this.sessionsByConference = sessions.stream()
                    .collect(Collectors.groupingBy(s -> s.conferenceId));
            this.sessionsById = sessions.stream()
                    .collect(Collectors.toMap(s -> s.sessionId, Function.identity()));
        }

        public List<Session> sessionsFor(Conference conference) {
            if (!sessionsByConference.containsKey(conference.id))
                return Collections.emptyList();

            return sessionsByConference.get(conference.id);
        }
    }

    class Session {
        public String sessionId, conferenceId, postedBy, status, lastUpdated;
        public Map<String, DataField<Object>> data;
        public List<Speaker> speakers;

        public boolean containsEmail(String email) {
            if (email.equals(postedBy))
                return true;

            for (Speaker speaker : speakers)
                if (email.equals(speaker.email))
                    return true;

            return false;
        }

        public Speaker getSpeaker(String name) {
            for (Speaker speaker : speakers) {
                if (speaker.name.equals(name)) {
                    return speaker;
                }
            }
            return null;
        }
    }

    class Speaker {
        public String id, name, email;
        public Map<String, DataField<Object>> data;
    }

    class DataField<T> {
        public boolean privateData;
        public T value;

        public DataField(boolean privateData, T value) {
            this.privateData = privateData;
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    @GET
    @Path("public/allSessions")
    ConferencesResponse conferences();

    @GET
    @Path("data/submitter/{email}/session")
    @CacheResult(cacheName = "sessions")
    SessionsResponse byEmail(@CacheKey @PathParam("email") String email);

    @GET
    @Path("/data/session/{sessionId}")
    @CacheResult(cacheName = "sessions")
    Session getSession(@CacheKey @QueryParam("email") String email, @PathParam("sessionId") String sessionId);

    @POST
    @Path("data/conference/{conferenceId}/session")
    @CacheInvalidate(cacheName = "sessions")
    Session createSession(@CacheKey @QueryParam("email") String email, String conferenceId, Session session);

    @PUT
    @Path("/data/session/{sessionId}")
    @CacheInvalidate(cacheName = "sessions")
    Session updateSession(@CacheKey @QueryParam("email") String email, String sessionId, Session session);
}
