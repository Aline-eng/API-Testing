package org.automation.tests.posts;

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
@Feature("Posts - PUT/PATCH")
class PostsPutPatchTest extends BaseTest {

    private static final int POST_ID = 1;

    @Test
    @Story("Full update of a post")
    @Description("PUT /posts/{id} returns 200 with the full resource replaced by the submitted fields")
    void updatePost_replacesFullResource() {
        Map<String, Object> updatedPost = Map.of(
                "id", POST_ID,
                "title", "updated title",
                "body", "updated body",
                "userId", 1
        );

        given().spec(requestSpec)
                .body(updatedPost)
                .when().put("/posts/{id}", POST_ID)
                .then().spec(responseSpec)
                .statusCode(200)
                .header("Content-Type", equalTo("application/json; charset=utf-8"))
                .body("id", equalTo(POST_ID))
                .body("title", equalTo("updated title"))
                .body("body", equalTo("updated body"))
                .body("userId", equalTo(1));
    }

    @Test
    @Story("Partial update of a post")
    @Description("PATCH /posts/{id} returns 200, changes only the targeted field, leaves the rest unchanged")
    void patchPost_changesOnlyTargetedField() {
        String newTitle = "patched title";

        given().spec(requestSpec)
                .body(Map.of("title", newTitle))
                .when().patch("/posts/{id}", POST_ID)
                .then().spec(responseSpec)
                .statusCode(200)
                .header("Content-Type", equalTo("application/json; charset=utf-8"))
                .body("id", equalTo(POST_ID))
                .body("userId", equalTo(1))
                .body("title", equalTo(newTitle))
                .body("body", equalTo(
                        "quia et suscipit\nsuscipit recusandae consequuntur expedita et cum\n"
                                + "reprehenderit molestiae ut ut quas totam\nnostrum rerum est autem sunt rem eveniet architecto"));
    }
}
