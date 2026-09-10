package org.automation.tests.users;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.automation.base.BaseTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("JSONPlaceholder API")
@Feature("Users - POST")
class UsersPostTest extends BaseTest {

    @Test
    @Story("Create a user")
    @Description("POST /users returns 201, echoes the submitted fields, and assigns a generated id")
    void createUser_echoesSubmittedFieldsAndAssignsId() {
        Map<String, Object> newUser = Map.of(
                "name", "Foo Bar",
                "username", "foobar",
                "email", "foo@bar.com"
        );

        given().spec(requestSpec)
                .body(newUser)
                .when().post("/users")
                .then().spec(responseSpec)
                .statusCode(201)
                .header("Content-Type", equalTo("application/json; charset=utf-8"))
                .body("name", equalTo("Foo Bar"))
                .body("username", equalTo("foobar"))
                .body("email", equalTo("foo@bar.com"))
                .body("id", notNullValue())
                .body("id", greaterThan(0));
    }
}
