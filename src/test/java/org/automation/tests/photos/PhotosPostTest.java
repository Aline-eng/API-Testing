package org.automation.tests.photos;

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
@Feature("Photos - POST")
class PhotosPostTest extends BaseTest {

    private static final JsonNode DATA = TestData.load("photos.json");

    @Test
    @Story("Create a photo")
    @Description("POST /photos returns 201, echoes the submitted fields, and assigns a generated id")
    void createPhoto_echoesSubmittedFieldsAndAssignsId() {
        Map<String, Object> newPhoto = TestData.asMap(DATA.get("create"));

        given().spec(requestSpec)
                .body(newPhoto)
                .when().post("/photos")
                .then().spec(responseSpec)
                .statusCode(201)
                .header("Content-Type", equalTo("application/json; charset=utf-8"))
                .body("albumId", equalTo(1))
                .body("title", equalTo("foo"))
                .body("url", equalTo("https://via.placeholder.com/600/000000"))
                .body("thumbnailUrl", equalTo("https://via.placeholder.com/150/000000"))
                .body("id", notNullValue())
                .body("id", greaterThan(0));
    }
}
