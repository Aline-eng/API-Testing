package org.automation.tests.todos;

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
import static org.hamcrest.Matchers.equalTo;

@Epic("JSONPlaceholder API")
@Feature("Todos - PUT/PATCH")
class TodosPutPatchTest extends BaseTest {

    private static final int TODO_ID = 1;
    private static final JsonNode DATA = TestData.load("todos.json");

    @Test
    @Story("Full update of a todo")
    @Description("PUT /todos/{id} returns 200 with the full resource replaced by the submitted fields")
    void updateTodo_replacesFullResource() {
        JsonNode update = DATA.get("update");
        Map<String, Object> updatedTodo = TestData.asMap(update);

        given().spec(requestSpec)
                .body(updatedTodo)
                .when().put("/todos/{id}", TODO_ID)
                .then().spec(responseSpec)
                .statusCode(200)
                .header("Content-Type", equalTo("application/json; charset=utf-8"))
                .body("id", equalTo(TODO_ID))
                .body("userId", equalTo(update.get("userId").asInt()))
                .body("title", equalTo(update.get("title").asText()))
                .body("completed", equalTo(update.get("completed").asBoolean()));
    }

    @Test
    @Story("Partial update of a todo")
    @Description("PATCH /todos/{id} returns 200, changes only the targeted field, leaves the rest unchanged")
    void patchTodo_changesOnlyTargetedField() {
        boolean newCompleted = true;

        given().spec(requestSpec)
                .body(Map.of("completed", newCompleted))
                .when().patch("/todos/{id}", TODO_ID)
                .then().spec(responseSpec)
                .statusCode(200)
                .header("Content-Type", equalTo("application/json; charset=utf-8"))
                .body("id", equalTo(TODO_ID))
                .body("userId", equalTo(DATA.get("update").get("userId").asInt()))
                .body("title", equalTo(DATA.get("originalTitle").asText()))
                .body("completed", equalTo(newCompleted));
    }
}
