package org.automation.tests.comments;

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
@Feature("Comments - GET (relational, read-only)")
class CommentsGetTest extends BaseTest {

    private static final String COMMENT_SCHEMA = "schemas/comment-schema.json";

    @Test
    @Story("List comments for a post")
    @Description("GET /posts/{id}/comments returns 200, a non-empty array where every comment.postId matches, schema valid")
    void getCommentsForPost_allBelongToRequestedPost() throws Exception {
        int postId = 1;

        Response response = given().spec(requestSpec)
                .when().get("/posts/{id}/comments", postId)
                .then().spec(responseSpec)
                .statusCode(200)
                .header("Content-Type", containsString("application/json"))
                .body("postId", everyItem(equalTo(postId)))
                .extract().response();

        List<Map<String, Object>> comments = response.jsonPath().getList("$");
        assertThat(comments, is(not(empty())));

        ObjectMapper mapper = new ObjectMapper();
        for (Map<String, Object> comment : comments) {
            String json = mapper.writeValueAsString(comment);
            assertThat(json, matchesJsonSchemaInClasspath(COMMENT_SCHEMA));
        }
    }
}
