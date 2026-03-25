package org.acme.adapters.keycloak;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.i18n.AgendaMessages;
import org.acme.i18n.MessageKey;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@ApplicationScoped
public class KeycloakPasswordAuthenticator {

    @ConfigProperty(name = "agenda.keycloak.base-url")
    String keycloakBaseUrl;

    @ConfigProperty(name = "agenda.keycloak.realm")
    String realm;

    @ConfigProperty(name = "agenda.keycloak.client-id")
    String clientId;

    @ConfigProperty(name = "agenda.keycloak.client-secret")
    String clientSecret;

    @Inject
    ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public AuthenticatedUser authenticate(String username, String password) {
        return requestToken(formBody(username, password), TokenRequestType.PASSWORD);
    }

    public AuthenticatedUser refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new KeycloakAuthenticationException(FailureType.REFRESH_REJECTED, AgendaMessages.get(MessageKey.KEYCLOAK_REFRESH_TOKEN_MISSING));
        }
        return requestToken(refreshBody(refreshToken), TokenRequestType.REFRESH);
    }

    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        try {
            HttpRequest request = HttpRequest.newBuilder(logoutEndpoint())
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(logoutBody(refreshToken)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 204 || response.statusCode() == 200) {
                return;
            }

            JsonNode body = safeReadJson(response.body());
            String error = text(body, "error");
            if (response.statusCode() == 400 && "invalid_grant".equals(error)) {
                return;
            }

            throw new KeycloakAuthenticationException(FailureType.UNAVAILABLE,
                    AgendaMessages.get(MessageKey.KEYCLOAK_LOGOUT_FAILED));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new KeycloakAuthenticationException(FailureType.UNAVAILABLE, AgendaMessages.get(MessageKey.KEYCLOAK_LOGOUT_INTERRUPTED), e);
        } catch (IOException e) {
            throw new KeycloakAuthenticationException(FailureType.UNAVAILABLE, AgendaMessages.get(MessageKey.KEYCLOAK_LOGOUT_COMMUNICATION_FAILED), e);
        }
    }

    private AuthenticatedUser requestToken(String formBody, TokenRequestType requestType) {
        try {
            HttpRequest request = HttpRequest.newBuilder(tokenEndpoint())
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(formBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return mapResponse(response, requestType);
        } catch (ConnectException e) {
            throw new KeycloakAuthenticationException(FailureType.UNAVAILABLE, AgendaMessages.get(MessageKey.KEYCLOAK_UNAVAILABLE), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new KeycloakAuthenticationException(FailureType.UNAVAILABLE, AgendaMessages.get(MessageKey.KEYCLOAK_AUTH_INTERRUPTED), e);
        } catch (IOException e) {
            throw new KeycloakAuthenticationException(FailureType.UNAVAILABLE, AgendaMessages.get(MessageKey.KEYCLOAK_COMMUNICATION_FAILED), e);
        }
    }

    private AuthenticatedUser mapResponse(HttpResponse<String> response, TokenRequestType requestType) throws IOException {
        JsonNode body = safeReadJson(response.body());

        if (response.statusCode() == 200) {
            String accessToken = text(body, "access_token");
            if (accessToken == null || accessToken.isBlank()) {
                throw new IOException(AgendaMessages.get(MessageKey.KEYCLOAK_RESPONSE_MISSING_ACCESS_TOKEN));
            }
            long expiresIn = body.path("expires_in").asLong(300);
            String refreshToken = text(body, "refresh_token");
            if (refreshToken == null || refreshToken.isBlank()) {
                throw new IOException(AgendaMessages.get(MessageKey.KEYCLOAK_RESPONSE_MISSING_REFRESH_TOKEN));
            }
            long refreshExpiresIn = body.path("refresh_expires_in").asLong(expiresIn);
            JsonNode claims = decodeClaims(accessToken);
            String subject = text(claims, "sub");
            if (subject == null || subject.isBlank()) {
                throw new IOException(AgendaMessages.get(MessageKey.KEYCLOAK_TOKEN_MISSING_SUBJECT));
            }
            return new AuthenticatedUser(
                    subject,
                    text(claims, "preferred_username"),
                    text(claims, "email"),
                    accessToken,
                    Instant.now().plusSeconds(expiresIn),
                    refreshToken,
                    Instant.now().plusSeconds(refreshExpiresIn)
            );
        }

        String error = text(body, "error");
        if (response.statusCode() == 400 && "invalid_grant".equals(error)) {
            if (requestType == TokenRequestType.REFRESH) {
                throw new KeycloakAuthenticationException(FailureType.REFRESH_REJECTED, AgendaMessages.get(MessageKey.KEYCLOAK_REFRESH_TOKEN_REJECTED));
            }
            throw new KeycloakAuthenticationException(FailureType.INVALID_CREDENTIALS, AgendaMessages.get(MessageKey.KEYCLOAK_INVALID_CREDENTIALS));
        }

        if (response.statusCode() == 401) {
            if ("invalid_grant".equals(error)) {
                if (requestType == TokenRequestType.REFRESH) {
                    throw new KeycloakAuthenticationException(FailureType.REFRESH_REJECTED, AgendaMessages.get(MessageKey.KEYCLOAK_REFRESH_TOKEN_REJECTED));
                }
                throw new KeycloakAuthenticationException(FailureType.INVALID_CREDENTIALS, AgendaMessages.get(MessageKey.KEYCLOAK_INVALID_CREDENTIALS));
            }
            throw new KeycloakAuthenticationException(FailureType.UNAVAILABLE, AgendaMessages.get(MessageKey.KEYCLOAK_CLIENT_REJECTED));
        }

        throw new KeycloakAuthenticationException(FailureType.UNAVAILABLE,
                AgendaMessages.format(MessageKey.KEYCLOAK_STATUS_RESPONSE, response.statusCode()));
    }

    private JsonNode decodeClaims(String accessToken) throws IOException {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IOException(AgendaMessages.get(MessageKey.KEYCLOAK_JWT_MISSING));
        }
        String[] segments = accessToken.split("\\.");
        if (segments.length < 2) {
            throw new IOException(AgendaMessages.get(MessageKey.KEYCLOAK_JWT_INVALID));
        }

        byte[] decoded = Base64.getUrlDecoder().decode(segments[1]);
        return objectMapper.readTree(decoded);
    }

    private JsonNode safeReadJson(String payload) throws IOException {
        if (payload == null || payload.isBlank()) {
            return objectMapper.createObjectNode();
        }
        return objectMapper.readTree(payload);
    }

    private URI tokenEndpoint() {
        String normalizedBase = keycloakBaseUrl.endsWith("/")
                ? keycloakBaseUrl.substring(0, keycloakBaseUrl.length() - 1)
                : keycloakBaseUrl;
        return URI.create(normalizedBase + "/realms/" + realm + "/protocol/openid-connect/token");
    }

    private URI logoutEndpoint() {
        String normalizedBase = keycloakBaseUrl.endsWith("/")
                ? keycloakBaseUrl.substring(0, keycloakBaseUrl.length() - 1)
                : keycloakBaseUrl;
        return URI.create(normalizedBase + "/realms/" + realm + "/protocol/openid-connect/logout");
    }

    private String formBody(String username, String password) {
        return "grant_type=password"
                + "&client_id=" + encode(clientId)
                + "&client_secret=" + encode(clientSecret)
                + "&username=" + encode(username)
                + "&password=" + encode(password);
    }

    private String refreshBody(String refreshToken) {
        return "grant_type=refresh_token"
                + "&client_id=" + encode(clientId)
                + "&client_secret=" + encode(clientSecret)
                + "&refresh_token=" + encode(refreshToken);
    }

    private String logoutBody(String refreshToken) {
        return "client_id=" + encode(clientId)
                + "&client_secret=" + encode(clientSecret)
                + "&refresh_token=" + encode(refreshToken);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String text(JsonNode json, String fieldName) {
        JsonNode value = json.get(fieldName);
        return value == null || value.isNull() ? null : value.asText();
    }

    public record AuthenticatedUser(String subject,
                                    String username,
                                    String email,
                                    String accessToken,
                                    Instant accessTokenExpiresAt,
                                    String refreshToken,
                                    Instant refreshTokenExpiresAt) {
    }

    public enum FailureType {
        INVALID_CREDENTIALS,
        REFRESH_REJECTED,
        UNAVAILABLE
    }

    private enum TokenRequestType {
        PASSWORD,
        REFRESH
    }

    public static class KeycloakAuthenticationException extends RuntimeException {
        private final FailureType failureType;

        public KeycloakAuthenticationException(FailureType failureType, String message) {
            super(message);
            this.failureType = failureType;
        }

        public KeycloakAuthenticationException(FailureType failureType, String message, Throwable cause) {
            super(message, cause);
            this.failureType = failureType;
        }

        public FailureType failureType() {
            return failureType;
        }
    }
}