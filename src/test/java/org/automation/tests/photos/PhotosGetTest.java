package org.automation.tests.photos;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.automation.base.BaseTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Epic("JSONPlaceholder API")
@Feature("Photos - GET")
class PhotosGetTest extends BaseTest {

    private static final String PHOTO_SCHEMA = "schemas/photo-schema.json";
    private static final int SCHEMA_VALIDATION_SAMPLE_SIZE = 25;

    @Test
    @Story("List all photos")
    @Description("GET /photos returns 200, an array of 5000 photos; a sample is validated against the photo schema " +
            "(the full list is too large to schema-validate item-by-item in a reasonable test time)")
    void getAllPhotos_returnsAllPhotosSampleMatchingSchema() throws Exception {
        Response response = given().spec(requestSpec)
                .when().get("/photos")
                .then().spec(responseSpec)
                .statusCode(200)
                .header("Content-Type", containsString("application/json"))
                .extract().response();

        List<Map<String, Object>> photos = response.jsonPath().getList("$");
        assertThat(photos, hasSize(5000));

        ObjectMapper mapper = new ObjectMapper();
        for (Map<String, Object> photo : photos.subList(0, SCHEMA_VALIDATION_SAMPLE_SIZE)) {
            String json = mapper.writeValueAsString(photo);
            assertThat(json, matchesJsonSchemaInClasspath(PHOTO_SCHEMA));
        }
    }

    @Test
    @Story("Get single photo")
    @Description("GET /photos/{id} returns 200 with matching id/albumId and a schema-valid body")
    void getPhotoById_returnsCorrectPhoto() {
        int photoId = 1;

        given().spec(requestSpec)
                .when().get("/photos/{id}", photoId)
                .then().spec(responseSpec)
                .statusCode(200)
                .header("Content-Type", containsString("application/json"))
                .body("id", equalTo(photoId))
                .body("albumId", equalTo(1))
                .body(matchesJsonSchemaInClasspath(PHOTO_SCHEMA));
    }

    @Test
    @Story("Get non-existent photo")
    @Description("GET /photos/99999999 returns 404 for an id that does not exist")
    void getPhotoById_invalidId_returns404() {
        given().spec(requestSpec)
                .when().get("/photos/{id}", 99999999)
                .then().spec(responseSpec)
                .statusCode(404)
                .header("Content-Type", containsString("application/json"));
    }

    @Test
    @Story("Filter photos by albumId")
    @Description("GET /photos?albumId={id} returns 200 with only that album's photos")
    void getPhotosFilteredByAlbumId_returnsOnlyMatchingPhotos() {
        int albumId = 1;

        given().spec(requestSpec)
                .queryParam("albumId", albumId)
                .when().get("/photos")
                .then().spec(responseSpec)
                .statusCode(200)
                .header("Content-Type", containsString("application/json"))
                .body("size()", equalTo(50))
                .body("albumId", everyItem(equalTo(albumId)));
    }
}
