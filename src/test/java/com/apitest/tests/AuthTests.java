package com.apitest.tests;

import com.apitest.clients.AuthClient;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Epic("Authentication")
public class AuthTests extends BaseTest {

    @Test
    @Tag("smoke")
    @Description("Verify that valid credentials return a non-empty auth token")
    void createToken_withValidCredentials_returnsToken() {
        String token = AuthClient.createToken();

        assertNotNull(token, "Token should not be null");
        assertFalse(token.isEmpty(), "Token should not be empty");
    }
}
