package org.automation.tests.comments;

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
@Feature("Comments - PUT/PATCH")
class CommentsPutPatchTest extends BaseTest {

    private static final int COMMENT_ID = 1;

    @Test
    @Story("Full update of a comment")
    @Description("PUT /comments/{id} returns 200 with the full resource replaced by the submitted fields")
    void updateComment_replacesFullResource() {
        Map<String, Object> updatedComment = Map.of(
                "id", COMMENT_ID,
                "postId", 1,
                "name", "updated",
                "email", "updated@bar.com",
                "body", "updated body"
        );

        given().spec(requestSpec)
                .body(updatedComment)
                .when().put("/comments/{id}", COMMENT_ID)
                .then().spec(responseSpec)
                .statusCode(200)
                .header("Content-Type", equalTo("application/json; charset=utf-8"))
                .body("id", equalTo(COMMENT_ID))
                .body("postId", equalTo(1))
                .body("name", equalTo("updated"))
                .body("email", equalTo("updated@bar.com"))
                .body("body", equalTo("updated body"));
    }

    @Test
    @Story("Partial update of a comment")
    @Description("PATCH /comments/{id} returns 200, changes only the targeted field, leaves the rest unchanged")
    void patchComment_changesOnlyTargetedField() {
        String newName = "patched name";

        given().spec(requestSpec)
                .body(Map.of("name", newName))
                .when().patch("/comments/{id}", COMMENT_ID)
                .then().spec(responseSpec)
                .statusCode(200)
                .header("Content-Type", equalTo("application/json; charset=utf-8"))
                .body("id", equalTo(COMMENT_ID))
                .body("postId", equalTo(1))
                .body("name", equalTo(newName))
                .body("email", equalTo("Eliseo@gardner.biz"))
                .body("body", equalTo(
                        "laudantium enim quasi est quidem magnam voluptate ipsam eos\ntempora quo necessitatibus\n"
                                + "dolor quam autem quasi\nreiciendis et nam sapiente accusantium"));
    }
}
