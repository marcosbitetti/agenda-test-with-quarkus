package org.acme.adapters.web;

import jakarta.inject.Inject;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.core.UserService;
import org.acme.core.AuthSessionService;

@Path("/api/users")
public class UserResource {

    @Inject
    AuthSessionService authSessionService;

    @Inject
    UserService userService;

    @GET
    @Path("/me")
    @Produces(MediaType.APPLICATION_JSON)
    public Response me(@CookieParam(AuthSessionService.COOKIE_NAME) String sessionId) {
        try {
            var session = authSessionService.findActiveSession(sessionId);
            if (session.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            var currentUser = session.get().user();

            var user = userService.findOrCreateByExternalId(
                    currentUser.subject(),
                    currentUser.username(),
                    currentUser.email()
            );
            return Response.ok(new UserDto(user)).build();
        } catch (AuthSessionService.SessionUnavailableException e) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
    }
}
