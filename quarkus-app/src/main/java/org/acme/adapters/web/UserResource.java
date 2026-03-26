package org.acme.adapters.web;

import jakarta.inject.Inject;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.ServiceUnavailableException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.core.AuthSessionService;
import org.acme.core.UserService;
import org.acme.i18n.AgendaMessages;
import org.acme.i18n.MessageKey;
import org.jboss.logging.Logger;

@Path("/api/users")
public class UserResource {

    private static final Logger LOG = Logger.getLogger(UserResource.class);

    @Inject
    AuthSessionService authSessionService;

    @Inject
    UserService userService;

    @GET
    @Path("/me")
    @Produces(MediaType.APPLICATION_JSON)
    public Response me(@CookieParam(AuthSessionService.COOKIE_NAME) String sessionId) {
        LOG.debug("users.me.received");
        try {
            var session = authSessionService.findActiveSession(sessionId).orElseThrow(
                    () -> new NotAuthorizedException(AgendaMessages.get(MessageKey.AUTH_SESSION_INVALID_OR_EXPIRED)));

            var currentUser = session.user();

            var user = userService.findOrCreateByExternalId(currentUser.subject(), currentUser.username(),
                    currentUser.email());
            LOG.debugf("users.me.completed subject=%s username=%s", currentUser.subject(), currentUser.username());
            return Response.ok(new UserDto(user)).build();
        } catch (AuthSessionService.SessionUnavailableException e) {
            throw new ServiceUnavailableException(AgendaMessages.get(MessageKey.AUTH_SERVICE_UNAVAILABLE));
        }
    }
}
