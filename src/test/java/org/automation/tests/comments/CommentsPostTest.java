package org.automation.tests.comments;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.automation.base.BaseTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("JSONPlaceholder API")
@Feature("Comments - POST")
class CommentsPostTest extends BaseTest {

    @Test
    @Story("Create a comment")
    @Description("POST /comments returns 201, echoes the submitted fields, and assigns a generated id")
    void createComment_echoesSubmittedFieldsAndAssignsId() {
        Map<String, Object> newComment = Map.of(
                "postId", 1,
                "name", "foo",
                "email", "foo@bar.com",
                "body", "bar"
        );

        given().spec(requestSpec)
                .body(newComment)
                .when().post("/comments")
                .then().spec(responseSpec)
                .statusCode(201)
                .header("Content-Type", equalTo("application/json; charset=utf-8"))
                .body("postId", equalTo(1))
                .body("name", equalTo("foo"))
                .body("email", equalTo("foo@bar.com"))
                .body("body", equalTo("bar"))
                .body("id", notNullValue())
                .body("id", greaterThan(0));
    }
}
