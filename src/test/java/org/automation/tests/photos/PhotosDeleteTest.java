package org.automation.tests.photos;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.automation.base.BaseTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Epic("JSONPlaceholder API")
@Feature("Photos - DELETE")
class PhotosDeleteTest extends BaseTest {

    @Test
    @Story("Delete a photo")
    @Description("DELETE /photos/{id} returns 200 with an empty JSON object body")
    void deletePhoto_returnsEmptyBody() {
        Map<String, Object> body = given().spec(requestSpec)
                .when().delete("/photos/{id}", 1)
                .then().spec(responseSpec)
                .statusCode(200)
                .header("Content-Type", equalTo("application/json; charset=utf-8"))
                .extract().jsonPath().getMap("$");

        assertThat(body, anEmptyMap());
    }
}
