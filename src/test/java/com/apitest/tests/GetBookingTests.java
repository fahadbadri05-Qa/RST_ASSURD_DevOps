package com.apitest.tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.restassured.response.Response;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

@Epic("Booking - Read")
public class GetBookingTests extends BaseTest {

    @Test
    @Tag("smoke")
    @Description("Verify GET /booking returns a non-empty list of booking IDs")
    void getAllBookings_returnsNonEmptyList() {
        Response response = bookingClient.getAllBookingIds();

        response.then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].bookingid", notNullValue());
    }

    @Test
    @Tag("regression")
    @Description("Verify GET /booking/{id} for a valid ID returns full booking details")
    void getBookingById_withValidId_returnsBookingDetails() {
        int existingId = bookingClient.getAllBookingIds()
                .then().extract().jsonPath().getInt("[0].bookingid");

        bookingClient.getBookingById(existingId)
                .then()
                .statusCode(200)
                .body("firstname", notNullValue())
                .body("lastname", notNullValue())
                .body("totalprice", greaterThanOrEqualTo(0));
    }

    @Test
    @Tag("regression")
    @Description("Verify GET /booking/{id} response matches the expected JSON schema")
    void getBookingById_matchesJsonSchema() {
        int existingId = bookingClient.getAllBookingIds()
                .then().extract().jsonPath().getInt("[0].bookingid");

        bookingClient.getBookingById(existingId)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/booking-schema.json"));
    }

    @Test
    @Tag("regression")
    @Description("Verify GET /booking/{id} for a non-existent ID returns 404")
    void getBookingById_withInvalidId_returns404() {
        bookingClient.getBookingById(999999999)
                .then()
                .statusCode(404);
    }
}
