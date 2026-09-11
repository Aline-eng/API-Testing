package org.automation.tests.users;

import com.fasterxml.jackson.databind.JsonNode;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.automation.base.BaseTest;
import org.automation.base.TestData;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@Epic("JSONPlaceholder API")
@Feature("Users - PUT/PATCH")
class UsersPutPatchTest extends BaseTest {

    private static final int USER_ID = 1;
    private static final JsonNode DATA = TestData.load("users.json");

    @Test
    @Story("Full update of a user")
    @Description("PUT /users/{id} returns 200 with the full resource replaced by the submitted fields")
    void updateUser_replacesFullResource() {
        Map<String, Object> updatedUser = TestData.asMap(DATA.get("update"));

        given().spec(requestSpec)
                .body(updatedUser)
                .when().put("/users/{id}", USER_ID)
                .then().spec(responseSpec)
                .statusCode(200)
                .header("Content-Type", equalTo("application/json; charset=utf-8"))
                .body("id", equalTo(USER_ID))
                .body("name", equalTo("Updated Name"))
                .body("username", equalTo("updatedname"))
                .body("email", equalTo("updated@bar.com"));
    }

    @Test
    @Story("Partial update of a user")
    @Description("PATCH /users/{id} returns 200, changes only the targeted field, leaves the rest (including nested address/company) unchanged")
    void patchUser_changesOnlyTargetedField() {
        String newName = DATA.get("patchName").asText();

        given().spec(requestSpec)
                .body(Map.of("name", newName))
                .when().patch("/users/{id}", USER_ID)
                .then().spec(responseSpec)
                .statusCode(200)
                .header("Content-Type", equalTo("application/json; charset=utf-8"))
                .body("id", equalTo(USER_ID))
                .body("name", equalTo(newName))
                .body("username", equalTo(DATA.get("originalUsername").asText()))
                .body("email", equalTo(DATA.get("originalEmail").asText()))
                .body("address.city", equalTo(DATA.get("originalCity").asText()))
                .body("company.name", equalTo(DATA.get("originalCompanyName").asText()));
    }
}
