package org.automation.tests.users;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.automation.base.BaseTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

@Epic("JSONPlaceholder API")
@Feature("Users - GET")
class UsersGetTest extends BaseTest {

    private static final String USER_SCHEMA = "schemas/user-schema.json";

    @Test
    @Story("Get single user")
    @Description("GET /users/{id} returns 200 with nested address/company objects correctly typed, schema valid")
    void getUserById_returnsNestedAddressAndCompany() {
        int userId = 1;

        given().spec(requestSpec)
                .when().get("/users/{id}", userId)
                .then().spec(responseSpec)
                .statusCode(200)
                .header("Content-Type", containsString("application/json"))
                .body("id", equalTo(userId))
                .body("address", notNullValue())
                .body("address.city", instanceOf(String.class))
                .body("address.geo.lat", instanceOf(String.class))
                .body("address.geo.lng", instanceOf(String.class))
                .body("company", notNullValue())
                .body("company.name", instanceOf(String.class))
                .body("company.catchPhrase", instanceOf(String.class))
                .body(matchesJsonSchemaInClasspath(USER_SCHEMA));
    }

    @Test
    @Story("Get a non-existent user")
    @Description("GET /users/99999 returns 404 for an id that does not exist")
    void getUserById_invalidId_returns404() {
        given().spec(requestSpec)
                .when().get("/users/{id}", 99999)
                .then().spec(responseSpec)
                .statusCode(404)
                .header("Content-Type", containsString("application/json"));
    }

    @Test
    @Story("Filter users by username")
    @Description("GET /users?username={name} returns 200 with only the matching user")
    void getUsersFilteredByUsername_returnsOnlyMatchingUser() {
        given().spec(requestSpec)
                .queryParam("username", "Bret")
                .when().get("/users")
                .then().spec(responseSpec)
                .statusCode(200)
                .header("Content-Type", containsString("application/json"))
                .body("size()", equalTo(1))
                .body("[0].username", equalTo("Bret"))
                .body("[0].id", equalTo(1));
    }
}
