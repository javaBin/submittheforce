package no.java.submit.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Provider;
import no.java.submit.form.SessionForm;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Work-around until this is fixed:
 * https://github.com/quarkusio/quarkus/issues/39757
 */
@Provider
public class SessionFormHandler implements MessageBodyReader<SessionForm> {

    @Inject
    ObjectMapper mapper;

    @Override
    public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return type == SessionForm.class;
    }

    @Override
    public SessionForm readFrom(Class<SessionForm> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> multivaluedMap, InputStream inputStream) throws IOException, WebApplicationException {
        // Read body
        var body = CharStreams.toString(new InputStreamReader(inputStream));

        // Prepare maps to hold values
        var session = new HashMap<String, Object>();
        var speakers = new HashMap<String, Map<String, String>>();

        // Parse body and split values into different maps
        for (var nvp : URLEncodedUtils.parse(body, StandardCharsets.UTF_8)) {
            if (nvp.getName().startsWith("speakers[")) {
                var nameParts = nvp.getName().split("\\.", 2);

                if (!speakers.containsKey(nameParts[0]))
                    speakers.put(nameParts[0], new HashMap<>());

                speakers.get(nameParts[0]).put(nameParts[1], nvp.getValue());
            } else {
                session.put(nvp.getName(), nvp.getValue());
            }
        }

        // Put speaker information into session map
        session.put("speakers", speakers.keySet().stream()
                .sorted()
                .map(speakers::get)
                .toList());

        // Convert it!
        return mapper.convertValue(session, SessionForm.class);
    }
}
