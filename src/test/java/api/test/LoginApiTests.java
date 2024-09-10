package api.test;

import api.model.login.LoginInput;
import api.model.login.LoginResponse;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.text.IsBlankString.blankString;

public class LoginApiTests {
    private static final String LOGIN_PATH = "/api/login";

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 3000;
    }

    @Test
    void verifyStaffLoginSuccessful() {
        LoginInput loginInput = new LoginInput("staff", "1234567890");
        Response actualResponse = RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .body(loginInput)
                .post(LOGIN_PATH);
        assertThat(actualResponse.statusCode(), equalTo(200));
        //Need to verify schema
        LoginResponse loginResponse = actualResponse.as(LoginResponse.class);
        assertThat(loginResponse.getToken(), not(blankString()));
        assertThat(loginResponse.getTimeout(), equalTo(120000));
    }
}
