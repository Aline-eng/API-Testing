package org.automation.tests.albums;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.automation.base.BaseTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@Epic("JSONPlaceholder API")
@Feature("Albums - PUT/PATCH")
class AlbumsPutPatchTest extends BaseTest {

    private static final int ALBUM_ID = 1;

    @Test
    @Story("Full update of an album")
    @Description("PUT /albums/{id} returns 200 with the full resource replaced by the submitted fields")
    void updateAlbum_replacesFullResource() {
        Map<String, Object> updatedAlbum = Map.of(
                "id", ALBUM_ID,
                "userId", 1,
                "title", "updated title"
        );

        given().spec(requestSpec)
                .body(updatedAlbum)
                .when().put("/albums/{id}", ALBUM_ID)
                .then().spec(responseSpec)
                .statusCode(200)
                .header("Content-Type", equalTo("application/json; charset=utf-8"))
                .body("id", equalTo(ALBUM_ID))
                .body("userId", equalTo(1))
                .body("title", equalTo("updated title"));
    }

    @Test
    @Story("Partial update of an album")
    @Description("PATCH /albums/{id} returns 200, changes only the targeted field, leaves the rest unchanged")
    void patchAlbum_changesOnlyTargetedField() {
        String newTitle = "patched title";

        given().spec(requestSpec)
                .body(Map.of("title", newTitle))
                .when().patch("/albums/{id}", ALBUM_ID)
                .then().spec(responseSpec)
                .statusCode(200)
                .header("Content-Type", equalTo("application/json; charset=utf-8"))
                .body("id", equalTo(ALBUM_ID))
                .body("userId", equalTo(1))
                .body("title", equalTo(newTitle));
    }
}
