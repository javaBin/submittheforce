package no.java.submit.template;

import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    @Inject
    Template error;

    @Override
    public Response toResponse(NotFoundException exception) {
        return Response
                .status(404)
                .header("Content-Type", "text/html")
                .entity(error
                        .data("title", "Ups")
                        .data("message", "Page not found"))
                .build();
    }
}
