package com.apitest.tests;

import com.apitest.models.Booking;
import com.apitest.models.BookingDates;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Epic("Booking - Create")
public class CreateBookingTests extends BaseTest {

    @Test
    @Tag("smoke")
    @Description("Verify a booking can be created with valid data")
    void createBooking_withValidData_returnsCreatedBooking() {
        Booking booking = new Booking(
                "John", "Doe", 150, true,
                new BookingDates("2026-08-10", "2026-08-15"),
                "Late checkout"
        );

        bookingClient.createBooking(booking)
                .then()
                .statusCode(200)
                .body("bookingid", notNullValue())
                .body("booking.firstname", equalTo("John"))
                .body("booking.lastname", equalTo("Doe"))
                .body("booking.totalprice", equalTo(150));
    }

    @Test
    @Tag("regression")
    @Description("Verify a booking can be created without the optional additionalneeds field")
    void createBooking_withoutOptionalField_stillSucceeds() {
        Booking booking = new Booking(
                "Minimal", "Fields", 75, false,
                new BookingDates("2026-11-01", "2026-11-03"),
                null
        );

        bookingClient.createBooking(booking)
                .then()
                .statusCode(200)
                .body("booking.firstname", equalTo("Minimal"));
    }
}
