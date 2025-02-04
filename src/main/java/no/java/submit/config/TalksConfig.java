package no.java.submit.config;

import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Named;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import no.java.submit.service.TalksService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

import java.net.URI;
import java.util.Base64;

@Dependent
public class TalksConfig implements ClientHeadersFactory {

    @ConfigProperty(name = "talks.location", defaultValue = "http://localhost:8082")
    URI location;

    @ConfigProperty(name = "talks.username", defaultValue = "anon")
    String username;

    @ConfigProperty(name = "talks.password", defaultValue = "anon")
    String password;

    @Produces
    @Named("talksService")
    public TalksService loadTalksService() {
        return QuarkusRestClientBuilder.newBuilder()
                .baseUri(location)
                .build(TalksService.class);
    }

    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> multivaluedMap, MultivaluedMap<String, String> multivaluedMap1) {
        return new MultivaluedHashMap<>() {{
            this.add("Authorization", "Basic  " + Base64.getEncoder().encodeToString(String.format("%s:%s", username, password).getBytes()));
        }};
    }
}
