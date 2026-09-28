package com.apitest.tests;

import com.apitest.clients.AuthClient;
import com.apitest.models.Booking;
import com.apitest.models.BookingDates;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Epic("Booking - Delete")
public class DeleteBookingTests extends BaseTest {

    private static String token;

    @BeforeAll
    static void getToken() {
        token = AuthClient.createToken();
    }

    @Test
    @Tag("regression")
    @Description("Verify a booking can be deleted with a valid token, and is gone afterward")
    void deleteBooking_withValidToken_removesBooking() {
        Booking booking = new Booking(
                "Delete", "Me", 50, false,
                new BookingDates("2026-10-01", "2026-10-02"),
                "None"
        );
        int bookingId = bookingClient.createBooking(booking)
                .then().extract().jsonPath().getInt("bookingid");

        bookingClient.deleteBooking(bookingId, token)
                .then()
                .statusCode(201);

        bookingClient.getBookingById(bookingId)
                .then()
                .statusCode(404);
    }
}
