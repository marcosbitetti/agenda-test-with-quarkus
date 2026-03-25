package org.acme.health;

import jakarta.inject.Inject;
import javax.sql.DataSource;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.Connection;

@Path("/api/health")
public class HealthResource {

    @Inject
    DataSource dataSource;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response health() {
        boolean dbUp;
        try (Connection c = dataSource.getConnection()) {
            dbUp = true;
        } catch (Exception e) {
            dbUp = false;
        }

        HealthStatus.State overallStatus = dbUp ? HealthStatus.State.UP : HealthStatus.State.DOWN;
        HealthStatus status = new HealthStatus(HealthStatus.State.UP, overallStatus);
        return Response.ok(status).build();
    }
}
