import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;

public class BookingBooks {

    private static final String BASE_URL = "http://restful-booker.herokuapp.com";
    private int bookingId;

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = BASE_URL;
    }

    @Test
    public void createBooking() {
        JSONObject bookingDates = new JSONObject();
        bookingDates.put("checkin", "2024-09-01");
        bookingDates.put("checkout", "2024-09-10");

        JSONObject requestBody = new JSONObject();
        requestBody.put("firstname", "John");
        requestBody.put("lastname", "Doe");
        requestBody.put("totalprice", 250);
        requestBody.put("depositpaid", false);
        requestBody.put("bookingdates", bookingDates);
        requestBody.put("additionalneeds", "WiFi");

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Accept", "application/json")
                .body(requestBody.toString())
                .when()
                .post("/booking");

        response.then().statusCode(200)
                .body("booking.firstname", equalTo("John"))
                .body("booking.lastname", equalTo("Doe"));

        bookingId = response.jsonPath().getInt("bookingid");
        System.out.println("Created booking ID: " + bookingId);
    }

    @Test
    public void getAllBookingIds() {
        Response response = RestAssured.given()
                .header("Accept", "application/json")
                .when()
                .get("/booking");

        response.then().statusCode(200);
        List<Integer> bookingIds = response.jsonPath().getList("bookingid");
        System.out.println("Booking IDs: " + bookingIds);

        if (!bookingIds.isEmpty()) {
            bookingId = bookingIds.get(0);
        }
    }

    @Test
    public void updateBookingPrice() {
        if (bookingId == 0) {
            createBooking();
        }

        JSONObject requestBody = new JSONObject();
        requestBody.put("totalprice", 300);

        Response response = RestAssured.given()
                .auth().preemptive().basic("admin", "password123")
                .contentType(ContentType.JSON)
                .header("Accept", "application/json")
                .body(requestBody.toString())
                .when()
                .patch("/booking/" + bookingId);

        response.then().statusCode(200)
                .body("totalprice", equalTo(300));
    }

    @Test
    public void updateBookingDetails() {
        if (bookingId == 0) {
            createBooking();
        }

        JSONObject bookingDates = new JSONObject();
        bookingDates.put("checkin", "2024-10-01");
        bookingDates.put("checkout", "2024-10-15");

        JSONObject requestBody = new JSONObject();
        requestBody.put("firstname", "Alice");
        requestBody.put("lastname", "Smith");
        requestBody.put("totalprice", 350);
        requestBody.put("depositpaid", true);
        requestBody.put("bookingdates", bookingDates);
        requestBody.put("additionalneeds", "Dinner");

        Response response = RestAssured.given()
                .auth().preemptive().basic("admin", "password123")
                .contentType(ContentType.JSON)
                .header("Accept", "application/json")
                .body(requestBody.toString())
                .when()
                .put("/booking/" + bookingId);

        response.then().statusCode(200)
                .body("firstname", equalTo("Alice"))
                .body("additionalneeds", equalTo("Dinner"));
    }

    @Test
    public void deleteBooking() {
        if (bookingId == 0) {
            createBooking();
        }

        Response response = RestAssured.given()
                .auth().preemptive().basic("admin", "password123")
                .header("Accept", "application/json")
                .when()
                .delete("/booking/" + bookingId);

        response.then().statusCode(201);
    }
}
