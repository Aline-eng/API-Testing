package org.automation.tests.posts;

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
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Epic("JSONPlaceholder API")
@Feature("Posts - GET")
class PostsGetTest extends BaseTest {

    private static final String POST_SCHEMA = "schemas/post-schema.json";

    @Test
    @Story("List all posts")
    @Description("GET /posts returns 200, an array of 100 posts, each matching the post schema, within 2s")
    void getAllPosts_returnsAllPostsMatchingSchema() throws Exception {
        Response response = given().spec(requestSpec)
                .when().get("/posts")
                .then().spec(responseSpec)
                .statusCode(200)
                .header("Content-Type", containsString("application/json"))
                .time(lessThan(2000L), TimeUnit.MILLISECONDS)
                .extract().response();

        List<Map<String, Object>> posts = response.jsonPath().getList("$");
        assertThat(posts, hasSize(100));

        ObjectMapper mapper = new ObjectMapper();
        for (Map<String, Object> post : posts) {
            String json = mapper.writeValueAsString(post);
            assertThat(json, matchesJsonSchemaInClasspath(POST_SCHEMA));
        }
    }

    @Test
    @Story("Get single post")
    @Description("GET /posts/{id} returns 200 with matching id/userId and a schema-valid body")
    void getPostById_returnsCorrectPost() {
        int postId = 1;

        given().spec(requestSpec)
                .when().get("/posts/{id}", postId)
                .then().spec(responseSpec)
                .statusCode(200)
                .header("Content-Type", containsString("application/json"))
                .body("id", equalTo(postId))
                .body("userId", equalTo(1))
                .body(matchesJsonSchemaInClasspath(POST_SCHEMA));
    }

    @Test
    @Story("Get non-existent post")
    @Description("GET /posts/99999 returns 404 for an id that does not exist")
    void getPostById_invalidId_returns404() {
        given().spec(requestSpec)
                .when().get("/posts/{id}", 99999)
                .then().spec(responseSpec)
                .statusCode(404)
                .header("Content-Type", containsString("application/json"));
    }
}
