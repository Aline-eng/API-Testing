package org.automation.tests.albums;

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
@Feature("Albums - POST")
class AlbumsPostTest extends BaseTest {

    @Test
    @Story("Create an album")
    @Description("POST /albums returns 201, echoes the submitted fields, and assigns a generated id")
    void createAlbum_echoesSubmittedFieldsAndAssignsId() {
        Map<String, Object> newAlbum = Map.of(
                "userId", 1,
                "title", "foo"
        );

        given().spec(requestSpec)
                .body(newAlbum)
                .when().post("/albums")
                .then().spec(responseSpec)
                .statusCode(201)
                .header("Content-Type", equalTo("application/json; charset=utf-8"))
                .body("userId", equalTo(1))
                .body("title", equalTo("foo"))
                .body("id", notNullValue())
                .body("id", greaterThan(0));
    }
}
