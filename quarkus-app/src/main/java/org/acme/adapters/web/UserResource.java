package org.acme.adapters.web;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.core.UserService;
import org.eclipse.microprofile.jwt.JsonWebToken;

import jakarta.annotation.security.RolesAllowed;

@Path("/api/users")
public class UserResource {

    @Inject
    JsonWebToken jwt;

    @Inject
    UserService userService;

    @GET
    @Path("/me")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user"})
    public Response me() {
        String sub = jwt.getClaim("sub");
        if (sub == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String preferred = jwt.getClaim("preferred_username");
        String email = jwt.getClaim("email");

        var user = userService.findOrCreateByExternalId(sub, preferred, email);
        return Response.ok(new UserDto(user)).build();
    }
}
