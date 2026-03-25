package org.acme.adapters.web;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@Path("/")
public class IndexResource {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response index() {
        // Try classpath resource first
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/resources/index.html")) {
            if (is != null) {
                String html = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                        .lines().collect(Collectors.joining("\n"));
                return Response.ok(html).build();
            }
        } catch (IOException ignored) {
        }

        // Fallback to project src (works when running in mounted dev container)
        java.nio.file.Path p = Paths.get("src/main/resources/META-INF/resources/index.html");
        if (Files.exists(p)) {
            try {
                String html = Files.readString(p, StandardCharsets.UTF_8);
                return Response.ok(html).build();
            } catch (IOException e) {
                return Response.serverError().entity("Error reading index: " + e.getMessage()).build();
            }
        }

        return Response.status(Response.Status.NOT_FOUND).entity("index.html not found").build();
    }
}
