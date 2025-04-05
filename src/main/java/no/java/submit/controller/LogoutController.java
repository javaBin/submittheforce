package no.java.submit.controller;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

@Path("logout")
public class LogoutController {

    @GET
    public Response get() {
        return Response
                .seeOther(UriBuilder.fromUri("/").build())
                .header("Set-Cookie", "q_session=; Max-Age=0")
                .build();
    }
}
