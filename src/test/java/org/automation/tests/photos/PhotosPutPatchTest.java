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
import static org.hamcrest.Matchers.equalTo;

@Epic("JSONPlaceholder API")
@Feature("Photos - PUT/PATCH")
class PhotosPutPatchTest extends BaseTest {

    private static final int PHOTO_ID = 1;
    private static final JsonNode DATA = TestData.load("photos.json");

    @Test
    @Story("Full update of a photo")
    @Description("PUT /photos/{id} returns 200 with the full resource replaced by the submitted fields")
    void updatePhoto_replacesFullResource() {
        Map<String, Object> updatedPhoto = TestData.asMap(DATA.get("update"));

        given().spec(requestSpec)
                .body(updatedPhoto)
                .when().put("/photos/{id}", PHOTO_ID)
                .then().spec(responseSpec)
                .statusCode(200)
                .header("Content-Type", equalTo("application/json; charset=utf-8"))
                .body("id", equalTo(PHOTO_ID))
                .body("albumId", equalTo(1))
                .body("title", equalTo("updated title"))
                .body("url", equalTo("https://via.placeholder.com/600/111111"))
                .body("thumbnailUrl", equalTo("https://via.placeholder.com/150/111111"));
    }

    @Test
    @Story("Partial update of a photo")
    @Description("PATCH /photos/{id} returns 200, changes only the targeted field, leaves the rest unchanged")
    void patchPhoto_changesOnlyTargetedField() {
        String newTitle = DATA.get("patchTitle").asText();

        given().spec(requestSpec)
                .body(Map.of("title", newTitle))
                .when().patch("/photos/{id}", PHOTO_ID)
                .then().spec(responseSpec)
                .statusCode(200)
                .header("Content-Type", equalTo("application/json; charset=utf-8"))
                .body("id", equalTo(PHOTO_ID))
                .body("albumId", equalTo(1))
                .body("title", equalTo(newTitle))
                .body("url", equalTo(DATA.get("originalUrl").asText()))
                .body("thumbnailUrl", equalTo(DATA.get("originalThumbnailUrl").asText()));
    }
}
