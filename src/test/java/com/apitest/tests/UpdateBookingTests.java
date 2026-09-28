package com.apitest.tests;

import com.apitest.clients.AuthClient;
import com.apitest.models.Booking;
import com.apitest.models.BookingDates;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;

@Epic("Booking - Update")
public class UpdateBookingTests extends BaseTest {

    private static String token;
    private static int bookingId;

    @BeforeAll
    static void createTokenAndBooking() {
        token = AuthClient.createToken();
        Booking booking = new Booking(
                "Jane", "Smith", 200, false,
                new BookingDates("2026-09-01", "2026-09-05"),
                "None"
        );
        bookingId = bookingClient.createBooking(booking)
                .then().extract().jsonPath().getInt("bookingid");
    }

    @Test
    @Tag("smoke")
    @Description("Verify a booking can be updated with a valid auth token")
    void updateBooking_withValidToken_updatesSuccessfully() {
        Booking updated = new Booking(
                "Janet", "Smith", 250, true,
                new BookingDates("2026-09-01", "2026-09-06"),
                "Extra towels"
        );

        bookingClient.updateBooking(bookingId, updated, token)
                .then()
                .statusCode(200)
                .body("firstname", equalTo("Janet"))
                .body("totalprice", equalTo(250));
    }

    @Test
    @Tag("regression")
    @Description("Verify updating a booking without a token is rejected with 403 Forbidden")
    void updateBooking_withoutToken_returnsForbidden() {
        Booking updated = new Booking(
                "Nope", "Denied", 100, true,
                new BookingDates("2026-09-01", "2026-09-02"),
                "None"
        );

        bookingClient.updateBooking(bookingId, updated, "")
                .then()
                .statusCode(403);
    }
}
