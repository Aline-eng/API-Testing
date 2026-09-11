package org.automation.tests.comments;

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
@Feature("Comments - POST")
class CommentsPostTest extends BaseTest {

    private static final JsonNode DATA = TestData.load("comments.json");

    @Test
    @Story("Create a comment")
    @Description("POST /comments returns 201, echoes the submitted fields, and assigns a generated id")
    void createComment_echoesSubmittedFieldsAndAssignsId() {
        JsonNode create = DATA.get("create");
        Map<String, Object> newComment = TestData.asMap(create);

        given().spec(requestSpec)
                .body(newComment)
                .when().post("/comments")
                .then().spec(responseSpec)
                .statusCode(201)
                .header("Content-Type", equalTo("application/json; charset=utf-8"))
                .body("postId", equalTo(create.get("postId").asInt()))
                .body("name", equalTo(create.get("name").asText()))
                .body("email", equalTo(create.get("email").asText()))
                .body("body", equalTo(create.get("body").asText()))
                .body("id", notNullValue())
                .body("id", greaterThan(0));
    }
}
