package no.java.submit.template;

import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class NotAuthorizedExceptionMapper implements ExceptionMapper<NotAuthorizedException> {

    @Inject
    Template noAccess;

    @Override
    public Response toResponse(NotAuthorizedException e) {
        return Response
                .status(401)
                .header("Content-Type", "text/html")
                .entity(noAccess
                        .data("title", "Sorry, no access")
                        .data("message", e.getMessage()))
                .build();
    }
}
