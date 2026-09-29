package com.apitest.tests;

import static org.hamcrest.Matchers.equalTo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.apitest.models.Booking;
import com.apitest.models.BookingDates;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;

@Epic("Booking - Data Driven")
public class DataDrivenBookingTests extends BaseTest {

    @Tag("regression")
    @ParameterizedTest(name = "GET /booking/{0} should return 404")
    @ValueSource(ints = {0, -1, 999999999})
    @Description("Verify a range of invalid/boundary booking IDs all return 404, not just one hardcoded case")
    void getBookingById_withBoundaryInvalidIds_returns404(int invalidId) {
        bookingClient.getBookingById(invalidId)
                .then()
                .statusCode(404);
    }

    @Tag("regression")
    @ParameterizedTest(name = "create booking: {0} {1}, price={2}, deposit={3}")
    @CsvSource({
            "Alice, Wonderland, 100, true",
            "Bob, Builder, 0, false",
            "Carol, Danvers, 99999, true",
            "Dave, Grohl, 1, false"
    })
    @Description("Verify booking creation succeeds across varied price/deposit combinations, including boundary prices")
    void createBooking_withVariedData_succeeds(String firstname, String lastname, int price, boolean depositPaid) {
        Booking booking = new Booking(
                firstname, lastname, price, depositPaid,
                new BookingDates("2026-12-01", "2026-12-05"),
                "Data-driven test"
        );

        bookingClient.createBooking(booking)
                .then()
                .statusCode(200)
                .body("booking.firstname", equalTo(firstname))
                .body("booking.totalprice", equalTo(price));
    }
}