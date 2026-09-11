package org.automation.tests.posts;

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
@Feature("Posts - POST")
class PostsPostTest extends BaseTest {

    private static final JsonNode DATA = TestData.load("posts.json");

    @Test
    @Story("Create a post")
    @Description("POST /posts returns 201, echoes the submitted fields, and assigns a generated id")
    void createPost_echoesSubmittedFieldsAndAssignsId() {
        Map<String, Object> newPost = TestData.asMap(DATA.get("create"));

        given().spec(requestSpec)
                .body(newPost)
                .when().post("/posts")
                .then().spec(responseSpec)
                .statusCode(201)
                .header("Content-Type", equalTo("application/json; charset=utf-8"))
                .body("title", equalTo("foo"))
                .body("body", equalTo("bar"))
                .body("userId", equalTo(1))
                .body("id", notNullValue())
                .body("id", greaterThan(0));
    }
}
