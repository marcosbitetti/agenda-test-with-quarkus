package org.acme.integration;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.acme.adapters.persistence.AuthSessionRepositoryImpl;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;


@QuarkusTest
public class UserResourceIT {

        @Inject
        AuthSessionRepositoryImpl authSessionRepository;

        @Inject
        AuthSessionTestSupport authSessionTestSupport;

        @BeforeEach
        public void setUp() {
                authSessionTestSupport.clearContacts();
        }

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
        public void testRootWithoutSessionReturnsLoginPage() {
                given()
                                .when().get("/")
                                .then()
                                .statusCode(200)
                                .body(containsString("Informe seu login ou e-mail e senha."));
        }

        @Test
        public void testRootWithSessionRedirectsToHome() {
                String sessionCookie = loginAndExtractSessionCookie();

                given()
                                .redirects().follow(false)
                                .cookie("AGENDA_SESSION", sessionCookie)
                                .when().get("/")
                                .then()
                                .statusCode(303)
                                .header("Location", endsWith("/home"));
        }

        @Test
        public void testHomeWithoutSessionRedirectsToLogin() {
                given()
                                .redirects().follow(false)
                                .when().get("/home")
                                .then()
                                .statusCode(303)
                                .header("Location", endsWith("/"));
        }

        @Test
        public void testHomeWithSessionReturnsPage() {
                String sessionCookie = loginAndExtractSessionCookie();

                given()
                                .cookie("AGENDA_SESSION", sessionCookie)
                                .when().get("/home")
                                .then()
                                .statusCode(200)
                                .body(containsString("Minha agenda"))
                                                                                                                                .body(containsString("hx-get=\"/api/contacts/panel\""))
                                                                                                                                .body(containsString("/js/vendor/htmx.min.js"));
        }

        @Test
        public void testContactsPanelRequiresSession() {
                given()
                                .when().get("/api/contacts/panel")
                                .then()
                                .statusCode(401);
        }

        @Test
        public void testContactsPanelShowsEmptyState() {
                String sessionCookie = loginAndExtractSessionCookie();

                given()
                                .cookie("AGENDA_SESSION", sessionCookie)
                                .when().get("/api/contacts/panel")
                                .then()
                                .statusCode(200)
                                .contentType(containsString("text/html"))
                                .body(containsString("Nenhum contato ativo cadastrado."))
                                .body(containsString(">0<"));
        }

    @Test
    public void testContactsEndpointRequiresSession() {
        given()
                .when().get("/api/contacts")
                .then()
                .statusCode(401);
    }

    @Test
    public void testCreateAndListContactsForAuthenticatedUser() {
        String sessionCookie = loginAndExtractSessionCookie();

        given()
                .cookie("AGENDA_SESSION", sessionCookie)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "firstName": "Maria",
                          "lastName": "Silva",
                          "birthDate": "1992-07-10",
                          "phoneNumbers": ["11999990000", "1133334444"],
                          "relationshipDegree": "Prima"
                        }
                        """)
                .when().post("/api/contacts")
                .then()
                .statusCode(201)
                .body("firstName", equalTo("Maria"))
                .body("lastName", equalTo("Silva"))
                .body("fullName", equalTo("Maria Silva"))
                .body("phoneNumbers", hasSize(2));

        given()
                .cookie("AGENDA_SESSION", sessionCookie)
                .when().get("/api/contacts")
                .then()
                .statusCode(200)
                .body("", hasSize(1))
                .body("[0].relationshipDegree", equalTo("Prima"));
    }

        @Test
        public void testCreateContactFromPanelRendersUpdatedHtml() {
                String sessionCookie = loginAndExtractSessionCookie();

                given()
                                .cookie("AGENDA_SESSION", sessionCookie)
                                .contentType("application/x-www-form-urlencoded")
                                .formParam("firstName", "Maria")
                                .formParam("lastName", "Silva")
                                .formParam("birthDate", "1992-07-10")
                                .formParam("phoneNumbers", "11999990000\n1133334444")
                                .formParam("relationshipDegree", "Prima")
                                .when().post("/api/contacts/panel")
                                .then()
                                .statusCode(200)
                                .contentType(containsString("text/html"))
                                .body(containsString("Contato salvo com sucesso."))
                                .body(containsString("Maria Silva"))
                                .body(containsString("Prima"))
                                .body(containsString(">1<"));
        }

    @Test
    public void testCreateContactWithInvalidPayloadReturns400() {
        String sessionCookie = loginAndExtractSessionCookie();

        given()
                .cookie("AGENDA_SESSION", sessionCookie)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "firstName": "  ",
                          "lastName": "Silva",
                          "birthDate": "1992-07-10",
                          "phoneNumbers": ["11999990000"]
                        }
                        """)
                .when().post("/api/contacts")
                .then()
                .statusCode(400)
                .body("message", equalTo("Nome obrigatorio."));
    }

        @Test
        public void testCreateContactFromPanelWithInvalidPayloadReturnsFragmentError() {
                String sessionCookie = loginAndExtractSessionCookie();

                given()
                                .cookie("AGENDA_SESSION", sessionCookie)
                                .contentType("application/x-www-form-urlencoded")
                                .formParam("firstName", " ")
                                .formParam("lastName", "Silva")
                                .formParam("birthDate", "1992-07-10")
                                .formParam("phoneNumbers", "11999990000")
                                .when().post("/api/contacts/panel")
                                .then()
                                .statusCode(200)
                                .contentType(containsString("text/html"))
                                .body(containsString("Nome obrigatorio."))
                                .body(not(containsString("Contato salvo com sucesso.")));
        }

    @Test
    public void testDeleteContactSoftDeletesIt() {
        String sessionCookie = loginAndExtractSessionCookie();

        Number contactId = given()
                .cookie("AGENDA_SESSION", sessionCookie)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "firstName": "Carlos",
                          "lastName": "Souza",
                          "birthDate": "1988-03-21",
                          "phoneNumbers": ["21988887777"]
                        }
                        """)
                .when().post("/api/contacts")
                .then()
                .statusCode(201)
                .extract().path("id");

        given()
                .cookie("AGENDA_SESSION", sessionCookie)
                .when().delete("/api/contacts/" + contactId.longValue())
                .then()
                .statusCode(204);

        given()
                .cookie("AGENDA_SESSION", sessionCookie)
                .when().get("/api/contacts")
                .then()
                .statusCode(200)
                .body("", hasSize(0));
    }

    @Test
    public void testDeleteContactFromPanelUpdatesFragment() {
        String sessionCookie = loginAndExtractSessionCookie();

        Number contactId = given()
                .cookie("AGENDA_SESSION", sessionCookie)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "firstName": "Carlos",
                          "lastName": "Souza",
                          "birthDate": "1988-03-21",
                          "phoneNumbers": ["21988887777"]
                        }
                        """)
                .when().post("/api/contacts")
                .then()
                .statusCode(201)
                .extract().path("id");

        given()
                .cookie("AGENDA_SESSION", sessionCookie)
                .when().delete("/api/contacts/panel/" + contactId.longValue())
                .then()
                .statusCode(200)
                .contentType(containsString("text/html"))
                .body(containsString("Contato excluido com sucesso."))
                .body(containsString("Nenhum contato ativo cadastrado."));
    }

    @Test
    public void testDeleteMissingContactFromPanelReturnsControlledFragment() {
        String sessionCookie = loginAndExtractSessionCookie();

        given()
                .cookie("AGENDA_SESSION", sessionCookie)
                .when().delete("/api/contacts/panel/99999")
                .then()
                .statusCode(200)
                .contentType(containsString("text/html"))
                .body(containsString("Contato nao encontrado."));
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

                String loginAndExtractSessionCookie() {
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

                                return loginResponse.extract().cookie("AGENDA_SESSION");
                }
}
