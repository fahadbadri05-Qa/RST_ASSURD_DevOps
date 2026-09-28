package com.apitest.clients;

import com.apitest.config.ConfigManager;
import com.apitest.models.AuthRequest;
import com.apitest.models.AuthResponse;

import static io.restassured.RestAssured.given;

public class AuthClient {

    public static String createToken() {
        AuthRequest authRequest = new AuthRequest(ConfigManager.getUsername(), ConfigManager.getPassword());

        AuthResponse authResponse = given()
                .baseUri(ConfigManager.getBaseUrl())
                .contentType("application/json")
                .body(authRequest)
                .when()
                .post("/auth")
                .then()
                .statusCode(200)
                .extract().as(AuthResponse.class);

        return authResponse.getToken();
    }
}
