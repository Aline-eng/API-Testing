package org.automation.tests.albums;

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
import static org.hamcrest.Matchers.*;

@Epic("JSONPlaceholder API")
@Feature("Albums - POST")
class AlbumsPostTest extends BaseTest {

    private static final JsonNode DATA = TestData.load("albums.json");

    @Test
    @Story("Create an album")
    @Description("POST /albums returns 201, echoes the submitted fields, and assigns a generated id")
    void createAlbum_echoesSubmittedFieldsAndAssignsId() {
        JsonNode create = DATA.get("create");
        Map<String, Object> newAlbum = TestData.asMap(create);

        given().spec(requestSpec)
                .body(newAlbum)
                .when().post("/albums")
                .then().spec(responseSpec)
                .statusCode(201)
                .header("Content-Type", equalTo("application/json; charset=utf-8"))
                .body("userId", equalTo(create.get("userId").asInt()))
                .body("title", equalTo(create.get("title").asText()))
                .body("id", notNullValue())
                .body("id", greaterThan(0));
    }
}
