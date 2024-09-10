package api.test;

import api.model.login.LoginInput;
import api.model.login.LoginResponse;
import api.model.user.Address;
import api.model.user.CreateUserResponse;
import api.model.user.User;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.text.IsBlankString.blankString;

public class CreateUserApiTests {
    private static final String LOGIN_PATH = "/api/login";
    private static final String CREATE_USER_PATH = "/api/user";

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 3000;
    }

    @Test
    void verifyStaffCreateUserSuccessfully() {
        LoginInput loginInput = new LoginInput("staff", "1234567890");
        Response actualResponse = RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .body(loginInput)
                .post(LOGIN_PATH);
        assertThat(actualResponse.statusCode(), equalTo(200));
        LoginResponse loginResponse = actualResponse.as(LoginResponse.class);
        assertThat(loginResponse.getToken(), not(blankString()));
        Address address = new Address();
        address.setStreetNumber("123");
        address.setStreet("Main St");
        address.setWard("Ward 1");
        address.setDistrict("District 1");
        address.setCity("Thu Duc");
        address.setState("Ho Chi Minh");
        address.setZip("70000");
        address.setCountry("VN");
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setMiddleName("Smith");
        user.setBirthday("01-23-2000");
        user.setEmail("abc1@xyz.com");
        user.setPhone("0123456789");
        user.setAddresses(List.of(address));

        Response createUserResponse = RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer ".concat(loginResponse.getToken()))
                .body(user)
                .post(CREATE_USER_PATH);
        System.out.printf("Create user response: %s%n", createUserResponse.asString());
        assertThat(createUserResponse.statusCode(), equalTo(200));
        CreateUserResponse actual = createUserResponse.as(CreateUserResponse.class);
        assertThat(actual.getId(), not(blankString()));
        assertThat(actual.getMessage(), equalTo("Customer created"));
    }
}
