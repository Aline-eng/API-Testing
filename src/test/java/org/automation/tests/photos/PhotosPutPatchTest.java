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
        JsonNode update = DATA.get("update");
        Map<String, Object> updatedPhoto = TestData.asMap(update);

        given().spec(requestSpec)
                .body(updatedPhoto)
                .when().put("/photos/{id}", PHOTO_ID)
                .then().spec(responseSpec)
                .statusCode(200)
                .header("Content-Type", equalTo("application/json; charset=utf-8"))
                .body("id", equalTo(PHOTO_ID))
                .body("albumId", equalTo(update.get("albumId").asInt()))
                .body("title", equalTo(update.get("title").asText()))
                .body("url", equalTo(update.get("url").asText()))
                .body("thumbnailUrl", equalTo(update.get("thumbnailUrl").asText()));
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
                .body("albumId", equalTo(DATA.get("update").get("albumId").asInt()))
                .body("title", equalTo(newTitle))
                .body("url", equalTo(DATA.get("originalUrl").asText()))
                .body("thumbnailUrl", equalTo(DATA.get("originalThumbnailUrl").asText()));
    }
}
