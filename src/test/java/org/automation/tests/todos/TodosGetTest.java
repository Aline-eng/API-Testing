package org.automation.tests.todos;

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
@Feature("Todos - GET")
class TodosGetTest extends BaseTest {

    private static final String TODO_SCHEMA = "schemas/todo-schema.json";

    @Test
    @Story("List all todos")
    @Description("GET /todos returns 200, an array of 200 todos, each matching the todo schema")
    void getAllTodos_returnsAllTodosMatchingSchema() throws Exception {
        Response response = given().spec(requestSpec)
                .when().get("/todos")
                .then().spec(responseSpec)
                .statusCode(200)
                .header("Content-Type", containsString("application/json"))
                .extract().response();

        List<Map<String, Object>> todos = response.jsonPath().getList("$");
        assertThat(todos, hasSize(200));

        ObjectMapper mapper = new ObjectMapper();
        for (Map<String, Object> todo : todos) {
            String json = mapper.writeValueAsString(todo);
            assertThat(json, matchesJsonSchemaInClasspath(TODO_SCHEMA));
        }
    }

    @Test
    @Story("Get single todo")
    @Description("GET /todos/{id} returns 200 with matching id/userId/completed and a schema-valid body")
    void getTodoById_returnsCorrectTodo() {
        int todoId = 1;

        given().spec(requestSpec)
                .when().get("/todos/{id}", todoId)
                .then().spec(responseSpec)
                .statusCode(200)
                .header("Content-Type", containsString("application/json"))
                .body("id", equalTo(todoId))
                .body("userId", equalTo(1))
                .body("completed", equalTo(false))
                .body(matchesJsonSchemaInClasspath(TODO_SCHEMA));
    }

    @Test
    @Story("Get non-existent todo")
    @Description("GET /todos/99999 returns 404 for an id that does not exist")
    void getTodoById_invalidId_returns404() {
        given().spec(requestSpec)
                .when().get("/todos/{id}", 99999)
                .then().spec(responseSpec)
                .statusCode(404)
                .header("Content-Type", containsString("application/json"));
    }

    @Test
    @Story("Filter todos by userId")
    @Description("GET /todos?userId={id} returns 200 with only that user's todos")
    void getTodosFilteredByUserId_returnsOnlyMatchingTodos() {
        int userId = 1;

        given().spec(requestSpec)
                .queryParam("userId", userId)
                .when().get("/todos")
                .then().spec(responseSpec)
                .statusCode(200)
                .header("Content-Type", containsString("application/json"))
                .body("size()", equalTo(20))
                .body("userId", everyItem(equalTo(userId)));
    }

    @Test
    @Story("Filter todos by completed status")
    @Description("GET /todos?completed=true returns 200 with only completed todos")
    void getTodosFilteredByCompleted_returnsOnlyCompletedTodos() {
        given().spec(requestSpec)
                .queryParam("completed", true)
                .when().get("/todos")
                .then().spec(responseSpec)
                .statusCode(200)
                .header("Content-Type", containsString("application/json"))
                .body("completed", everyItem(equalTo(true)));
    }
}
