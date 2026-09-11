package org.automation.tests.todos;

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
@Feature("Todos - PUT/PATCH")
class TodosPutPatchTest extends BaseTest {

    private static final int TODO_ID = 1;

    @Test
    @Story("Full update of a todo")
    @Description("PUT /todos/{id} returns 200 with the full resource replaced by the submitted fields")
    void updateTodo_replacesFullResource() {
        Map<String, Object> updatedTodo = Map.of(
                "id", TODO_ID,
                "userId", 1,
                "title", "updated title",
                "completed", true
        );

        given().spec(requestSpec)
                .body(updatedTodo)
                .when().put("/todos/{id}", TODO_ID)
                .then().spec(responseSpec)
                .statusCode(200)
                .header("Content-Type", equalTo("application/json; charset=utf-8"))
                .body("id", equalTo(TODO_ID))
                .body("userId", equalTo(1))
                .body("title", equalTo("updated title"))
                .body("completed", equalTo(true));
    }

    @Test
    @Story("Partial update of a todo")
    @Description("PATCH /todos/{id} returns 200, changes only the targeted field, leaves the rest unchanged")
    void patchTodo_changesOnlyTargetedField() {
        given().spec(requestSpec)
                .body(Map.of("completed", true))
                .when().patch("/todos/{id}", TODO_ID)
                .then().spec(responseSpec)
                .statusCode(200)
                .header("Content-Type", equalTo("application/json; charset=utf-8"))
                .body("id", equalTo(TODO_ID))
                .body("userId", equalTo(1))
                .body("title", equalTo("delectus aut autem"))
                .body("completed", equalTo(true));
    }
}
