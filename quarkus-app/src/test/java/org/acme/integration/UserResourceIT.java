package org.acme.integration;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import jakarta.inject.Inject;
import org.acme.adapters.persistence.AuthSessionRepositoryImpl;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;


@QuarkusTest
public class UserResourceIT {

        @Inject
        AuthSessionRepositoryImpl authSessionRepository;

        @Inject
        AuthSessionTestSupport authSessionTestSupport;

    @Test
    public void testLoginReturnsSessionCookieAndUser() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "joao",
                          "password": "password"
                        }
                        """)
                .when().post("/api/auth/login")
                .then()
                .statusCode(200)
                .cookie("AGENDA_SESSION", notNullValue())
                .body("username", equalTo("joao"));
    }

    @Test
    public void testLoginWithInvalidCredentialsReturns401() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "joao",
                          "password": "senha-errada"
                        }
                        """)
                .when().post("/api/auth/login")
                .then()
                .statusCode(401)
                .body("message", equalTo("Login ou senha invalidos."));
    }

    @Test
    public void testLoginWithEmptyFieldsReturns400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "",
                          "password": ""
                        }
                        """)
                .when().post("/api/auth/login")
                .then()
                .statusCode(400)
                .body("message", equalTo("Informe login ou e-mail e senha."));
    }

    @Test
    public void testMeEndpointReturnsUserFromLocalSession() {
        ValidatableResponse loginResponse = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "joao",
                          "password": "password"
                        }
                        """)
                .when().post("/api/auth/login")
                .then()
                .statusCode(200);

        String sessionCookie = loginResponse.extract().cookie("AGENDA_SESSION");

        given()
                .cookie("AGENDA_SESSION", sessionCookie)
                .when().get("/api/users/me")
                .then()
                .statusCode(200)
                .body("username", equalTo("joao"));
    }

    @Test
    public void testMeEndpointRefreshesExpiredAccessToken() {
        ValidatableResponse loginResponse = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "joao",
                          "password": "password"
                        }
                        """)
                .when().post("/api/auth/login")
                .then()
                .statusCode(200);

        String sessionCookie = loginResponse.extract().cookie("AGENDA_SESSION");
        expireAccessToken(sessionCookie, Instant.now().minusSeconds(30));

        given()
                .cookie("AGENDA_SESSION", sessionCookie)
                .when().get("/api/users/me")
                .then()
                .statusCode(200)
                .body("username", equalTo("joao"));
    }

    @Test
    public void testExpiredRefreshTokenInvalidatesSession() {
        ValidatableResponse loginResponse = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "joao",
                          "password": "password"
                        }
                        """)
                .when().post("/api/auth/login")
                .then()
                .statusCode(200);

        String sessionCookie = loginResponse.extract().cookie("AGENDA_SESSION");
        expireAccessToken(sessionCookie, Instant.now().minusSeconds(30));
        expireRefreshToken(sessionCookie, Instant.now().minusSeconds(30));

        given()
                .cookie("AGENDA_SESSION", sessionCookie)
                .when().get("/api/users/me")
                .then()
                .statusCode(401);
    }

    @Test
    public void testLogoutRevokesRemoteSessionAndClearsLocalSession() {
        ValidatableResponse loginResponse = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "joao",
                          "password": "password"
                        }
                        """)
                .when().post("/api/auth/login")
                .then()
                .statusCode(200);

        String sessionCookie = loginResponse.extract().cookie("AGENDA_SESSION");
        String refreshToken = authSessionRepository.findById(sessionCookie)
                .orElseThrow()
                .refreshToken();

        given()
                .cookie("AGENDA_SESSION", sessionCookie)
                .when().delete("/api/auth/session")
                .then()
                .statusCode(204);

        given()
                .cookie("AGENDA_SESSION", sessionCookie)
                .when().get("/api/users/me")
                .then()
                .statusCode(401);

        given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "refresh_token")
                .formParam("client_id", "quarkus-app")
                .formParam("client_secret", "quarkus-app-secret")
                .formParam("refresh_token", refreshToken)
                .when().post("http://keycloak:8080/realms/agenda/protocol/openid-connect/token")
                .then()
                .statusCode(400)
                .body("error", equalTo("invalid_grant"));
    }

    @Test
    public void testCleanupRemovesExpiredSessions() {
        ValidatableResponse loginResponse = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "joao",
                          "password": "password"
                        }
                        """)
                .when().post("/api/auth/login")
                .then()
                .statusCode(200);

        String sessionCookie = loginResponse.extract().cookie("AGENDA_SESSION");
        expireRefreshToken(sessionCookie, Instant.now().minusSeconds(30));

        authSessionTestSupport.cleanupExpiredSessions();

        given()
                .cookie("AGENDA_SESSION", sessionCookie)
                .when().get("/api/users/me")
                .then()
                .statusCode(401);
    }

    void expireAccessToken(String sessionId, Instant expiresAt) {
                authSessionTestSupport.expireAccessToken(sessionId, expiresAt);
    }

    void expireRefreshToken(String sessionId, Instant expiresAt) {
                authSessionTestSupport.expireRefreshToken(sessionId, expiresAt);
    }
}
