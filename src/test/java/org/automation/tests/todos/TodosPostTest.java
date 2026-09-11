package org.automation.tests.todos;

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
@Feature("Todos - POST")
class TodosPostTest extends BaseTest {

    @Test
    @Story("Create a todo")
    @Description("POST /todos returns 201, echoes the submitted fields, and assigns a generated id")
    void createTodo_echoesSubmittedFieldsAndAssignsId() {
        Map<String, Object> newTodo = Map.of(
                "userId", 1,
                "title", "foo",
                "completed", false
        );

        given().spec(requestSpec)
                .body(newTodo)
                .when().post("/todos")
                .then().spec(responseSpec)
                .statusCode(201)
                .header("Content-Type", equalTo("application/json; charset=utf-8"))
                .body("userId", equalTo(1))
                .body("title", equalTo("foo"))
                .body("completed", equalTo(false))
                .body("id", notNullValue())
                .body("id", greaterThan(0));
    }
}
