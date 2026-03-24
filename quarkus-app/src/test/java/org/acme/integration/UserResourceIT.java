package org.acme.integration;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;


@QuarkusTest
public class UserResourceIT {

    static String token;

    @BeforeAll
    public static void getToken() throws Exception {
        // Try a few times in case Keycloak is still starting
        String tokenEndpoint = "http://keycloak:8080/realms/agenda/protocol/openid-connect/token";
        for (int i = 0; i < 12; i++) {
            try {
                Response resp = RestAssured
                    .given()
                    .contentType("application/x-www-form-urlencoded")
                    .formParam("grant_type", "password")
                    .formParam("client_id", "quarkus-app")
                    .formParam("client_secret", "quarkus-app-secret")
                    .formParam("username", "joao")
                    .formParam("password", "password")
                    .post(tokenEndpoint);

                if (resp.statusCode() != 200) {
                    System.err.println("Keycloak token endpoint returned status " + resp.statusCode());
                    System.err.println(resp.asString());
                }

                resp.then().statusCode(200);
                token = resp.path("access_token");
                if (token != null && !token.isEmpty()) return;
            } catch (Exception ex) {
                Thread.sleep(2000);
            }
        }
        throw new IllegalStateException("Could not obtain token from Keycloak");
    }

    @Test
    public void testMeEndpointReturnsUser() {
        given()
                .auth().oauth2(token)
                .when().get("/api/users/me")
                .then()
                .statusCode(200)
                .body("username", equalTo("joao"));
    }
}
