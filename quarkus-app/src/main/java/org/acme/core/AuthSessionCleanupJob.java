package org.acme.core;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.logging.StructuredLogFields;
import org.acme.logging.StructuredLogContext;
import org.jboss.logging.Logger;

import java.util.Map;

@ApplicationScoped
public class AuthSessionCleanupJob {

    private static final Logger LOG = Logger.getLogger(AuthSessionCleanupJob.class);

    @Inject
    AuthSessionService authSessionService;

    @Scheduled(every = "${agenda.auth.cleanup-interval:30m}")
    void cleanupExpiredSessions() {
        long deleted = authSessionService.cleanupExpiredSessions();
        try (var ignored = StructuredLogContext.open(Map.of(
                StructuredLogFields.EVENT, "auth.session.cleanup.executed",
                StructuredLogFields.OUTCOME, "success",
                StructuredLogFields.DELETED_SESSIONS, deleted
        ))) {
            LOG.debug("auth.session.cleanup.executed");
        }
    }
}