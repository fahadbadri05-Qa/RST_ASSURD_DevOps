package com.apitest.tests;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@WireMockTest(httpPort = 8089)
public class WireMockBookingTests {

    @Test
    @Tag("smoke")
    public void testMockedAuthEndpoint() {
        stubFor(post(urlEqualTo("/auth"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"token\":\"mock-token-portfolio-123\"}")));

        given()
                .baseUri("http://localhost:8089")
                .contentType("application/json")
                .body("{\"username\":\"admin\",\"password\":\"password123\"}")
            .when()
                .post("/auth")
            .then()
                .statusCode(200)
                .body("token", equalTo("mock-token-portfolio-123"));
    }
}