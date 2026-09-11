package org.automation.tests.albums;

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
@Feature("Albums - GET")
class AlbumsGetTest extends BaseTest {

    private static final String ALBUM_SCHEMA = "schemas/album-schema.json";

    @Test
    @Story("List all albums")
    @Description("GET /albums returns 200, an array of 100 albums, each matching the album schema")
    void getAllAlbums_returnsAllAlbumsMatchingSchema() throws Exception {
        Response response = given().spec(requestSpec)
                .when().get("/albums")
                .then().spec(responseSpec)
                .statusCode(200)
                .header("Content-Type", containsString("application/json"))
                .extract().response();

        List<Map<String, Object>> albums = response.jsonPath().getList("$");
        assertThat(albums, hasSize(100));

        ObjectMapper mapper = new ObjectMapper();
        for (Map<String, Object> album : albums) {
            String json = mapper.writeValueAsString(album);
            assertThat(json, matchesJsonSchemaInClasspath(ALBUM_SCHEMA));
        }
    }

    @Test
    @Story("Get single album")
    @Description("GET /albums/{id} returns 200 with matching id/userId and a schema-valid body")
    void getAlbumById_returnsCorrectAlbum() {
        int albumId = 1;

        given().spec(requestSpec)
                .when().get("/albums/{id}", albumId)
                .then().spec(responseSpec)
                .statusCode(200)
                .header("Content-Type", containsString("application/json"))
                .body("id", equalTo(albumId))
                .body("userId", equalTo(1))
                .body(matchesJsonSchemaInClasspath(ALBUM_SCHEMA));
    }

    @Test
    @Story("Get non-existent album")
    @Description("GET /albums/99999 returns 404 for an id that does not exist")
    void getAlbumById_invalidId_returns404() {
        given().spec(requestSpec)
                .when().get("/albums/{id}", 99999)
                .then().spec(responseSpec)
                .statusCode(404)
                .header("Content-Type", containsString("application/json"));
    }

    @Test
    @Story("Filter albums by userId")
    @Description("GET /albums?userId={id} returns 200 with only that user's albums")
    void getAlbumsFilteredByUserId_returnsOnlyMatchingAlbums() {
        int userId = 1;

        given().spec(requestSpec)
                .queryParam("userId", userId)
                .when().get("/albums")
                .then().spec(responseSpec)
                .statusCode(200)
                .header("Content-Type", containsString("application/json"))
                .body("size()", equalTo(10))
                .body("userId", everyItem(equalTo(userId)));
    }
    // tEST
}
