package org.automation.tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.automation.base.BaseTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * The lab spec calls for "malformed POST body -> appropriate 4xx handling", but
 * JSONPlaceholder does not actually behave that way: it performs no server-side
 * payload validation for well-formed JSON (any fields/types are accepted with 201),
 * and syntactically broken JSON crashes its body-parser with an unhandled
 * exception, returning 500 rather than 400. These tests assert the exact,
 * observed behavior rather than the spec-ideal behavior - see docs/test-summary.md
 * for this documented as a known API deviation from REST convention.
 */
@Epic("JSONPlaceholder API")
@Feature("Posts - Edge cases")
class PostsEdgeCasesTest extends BaseTest {

    @Test
    @Story("Malformed request body")
    @Description("POST /posts with syntactically invalid JSON returns 500, not 400, " +
            "because JSONPlaceholder's body-parser throws an unhandled JSON.parse error")
    void postWithMalformedJsonSyntax_serverReturns500NotBadRequest() {
        String malformedJson = "{\"title\": \"foo\", \"body\": \"bar\", ";

        Response response = given().spec(requestSpec)
                .body(malformedJson)
                .when().post("/posts")
                .then().extract().response();

        System.out.println("Raw response status: " + response.getStatusCode());
        System.out.println("Raw response body: " + response.getBody().asString());

        assertThat(response.getStatusCode(), equalTo(500));
    }

    @Test
    @Story("Empty request body")
    @Description("POST /posts with an empty JSON object still returns 201 - JSONPlaceholder " +
            "performs no server-side validation of required fields")
    void postWithEmptyBody_serverAcceptsWithNoValidation() {
        Response response = given().spec(requestSpec)
                .body("{}")
                .when().post("/posts")
                .then().extract().response();

        System.out.println("Raw response status: " + response.getStatusCode());
        System.out.println("Raw response body: " + response.getBody().asString());

        assertThat(response.getStatusCode(), equalTo(201));
    }

    @Test
    @Story("Wrong field types")
    @Description("POST /posts with userId sent as a string still returns 201 - JSONPlaceholder " +
            "performs no server-side type validation")
    void postWithWrongFieldTypes_serverAcceptsWithNoValidation() {
        Map<String, Object> malformedTypes = new HashMap<>();
        malformedTypes.put("title", "foo");
        malformedTypes.put("body", "bar");
        malformedTypes.put("userId", "not-a-number");

        Response response = given().spec(requestSpec)
                .body(malformedTypes)
                .when().post("/posts")
                .then().extract().response();

        System.out.println("Raw response status: " + response.getStatusCode());
        System.out.println("Raw response body: " + response.getBody().asString());

        assertThat(response.getStatusCode(), equalTo(201));
    }
}
