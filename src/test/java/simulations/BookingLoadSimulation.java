package simulations;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class BookingLoadSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8081")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    ScenarioBuilder scn = scenario("Booking API Load Test")
            .exec(
                    http("Auth - Get Token")
                            .post("/auth")
                            .body(StringBody("{\"username\": \"admin\", \"password\": \"password123\"}"))
                            .check(status().is(200))
                            .check(jsonPath("$.token").saveAs("authToken"))
            )
            .exec(
                    http("Create Booking")
                            .post("/booking")
                            .body(StringBody("{\"firstname\": \"Load\", \"lastname\": \"Test\", \"totalprice\": 150, \"depositpaid\": true, \"bookingdates\": {\"checkin\": \"2026-06-01\", \"checkout\": \"2026-06-05\"}, \"additionalneeds\": \"Breakfast\"}"))
                            .check(status().is(200))
                            .check(jsonPath("$.bookingid").saveAs("bookingId"))
            )
            .exec(
                    http("Get Booking By Id")
                            .get("/booking/#{bookingId}")
                            .check(status().is(200))
            )
            .exec(
                    http("Get All Bookings")
                            .get("/booking")
                            .check(status().is(200))
            );

    {
        setUp(
                scn.injectOpen(rampUsers(20).during(10))
        ).protocols(httpProtocol)
         .assertions(
                global().failedRequests().count().is(0L),
                global().responseTime().max().lt(1000)
         );
    }
}