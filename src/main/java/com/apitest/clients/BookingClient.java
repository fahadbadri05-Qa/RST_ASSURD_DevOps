package com.apitest.clients;

import com.apitest.config.ConfigManager;
import com.apitest.models.Booking;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class BookingClient {

    public Response getAllBookingIds() {
        return given()
                .baseUri(ConfigManager.getBaseUrl())
                .when()
                .get("/booking");
    }

    public Response getBookingById(int id) {
        return given()
                .baseUri(ConfigManager.getBaseUrl())
                .when()
                .get("/booking/" + id);
    }

    public Response createBooking(Booking booking) {
        return given()
                .baseUri(ConfigManager.getBaseUrl())
                .contentType("application/json")
                .body(booking)
                .when()
                .post("/booking");
    }

    public Response updateBooking(int id, Booking booking, String token) {
        return given()
                .baseUri(ConfigManager.getBaseUrl())
                .contentType("application/json")
                .cookie("token", token)
                .body(booking)
                .when()
                .put("/booking/" + id);
    }

    public Response deleteBooking(int id, String token) {
        return given()
                .baseUri(ConfigManager.getBaseUrl())
                .cookie("token", token)
                .when()
                .delete("/booking/" + id);
    }
}
