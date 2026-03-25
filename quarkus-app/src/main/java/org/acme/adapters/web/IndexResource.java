package org.acme.adapters.web;

import jakarta.inject.Inject;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.core.AuthSessionService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.URI;
import java.util.stream.Collectors;

@Path("/")
public class IndexResource {

    @Inject
    AuthSessionService authSessionService;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response index(@CookieParam(AuthSessionService.COOKIE_NAME) String sessionId) {
        try {
            if (hasActiveSession(sessionId)) {
                return redirectTo("/home");
            }
            return servePage("login.html");
        } catch (AuthSessionService.SessionUnavailableException e) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("Falha ao validar a sessao atual.")
                    .build();
        }
    }

    @GET
    @Path("/home")
    @Produces(MediaType.TEXT_HTML)
    public Response home(@CookieParam(AuthSessionService.COOKIE_NAME) String sessionId) {
        try {
            if (!hasActiveSession(sessionId)) {
                return redirectTo("/");
            }
            return servePage("home.html");
        } catch (AuthSessionService.SessionUnavailableException e) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("Falha ao validar a sessao atual.")
                    .build();
        }
    }

    private boolean hasActiveSession(String sessionId) {
        return authSessionService.findActiveSession(sessionId).isPresent();
    }

    private Response redirectTo(String path) {
        return Response.seeOther(URI.create(path)).build();
    }

    private Response servePage(String resourceName) {
        // Try classpath resource first
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/resources/" + resourceName)) {
            if (is != null) {
                String html = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                        .lines().collect(Collectors.joining("\n"));
                return Response.ok(html).build();
            }
        } catch (IOException ignored) {
        }

        // Fallback to project src (works when running in mounted dev container)
        java.nio.file.Path p = Paths.get("src/main/resources/META-INF/resources", resourceName);
        if (Files.exists(p)) {
            try {
                String html = Files.readString(p, StandardCharsets.UTF_8);
                return Response.ok(html).build();
            } catch (IOException e) {
                return Response.serverError().entity("Error reading page: " + e.getMessage()).build();
            }
        }

        return Response.status(Response.Status.NOT_FOUND).entity(resourceName + " not found").build();
    }
}
