package api.test;

import api.model.login.LoginInput;
import api.model.login.LoginResponse;
import api.model.user.Address;
import api.model.user.CreateUserResponse;
import api.model.user.User;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.text.IsBlankString.blankString;

public class CreateUserApiTests {
    private static final String LOGIN_PATH = "/api/login";
    private static final String CREATE_USER_PATH = "/api/user";
    private static final String DELETE_USER_PATH = "/api/user/{id}";
    private static final String GET_USER_PATH = "/api/user/{id}";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static List<String> createdUserIds = new ArrayList<>();
    private static String TOKEN;

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 3000;

        //Get token
        LoginInput loginInput = new LoginInput("staff", "1234567890");
        Response actualResponse = RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .body(loginInput)
                .post(LOGIN_PATH);
        assertThat(actualResponse.statusCode(), equalTo(200));
        LoginResponse loginResponse = actualResponse.as(LoginResponse.class);
        assertThat(loginResponse.getToken(), not(blankString()));
        TOKEN = "Bearer ".concat(loginResponse.getToken());
    }

    @Test
    void verifyStaffCreateUserSuccessfully() {
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
        String randomEmail = String.format("auto_api_%s@abc.com", System.currentTimeMillis());
        user.setEmail(randomEmail);
        user.setPhone("0123456789");
        user.setAddresses(List.of(address));

        Response createUserResponse = RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .header(AUTHORIZATION_HEADER, TOKEN)
                .body(user)
                .post(CREATE_USER_PATH);
        System.out.printf("Create user response: %s%n", createUserResponse.asString());
        assertThat(createUserResponse.statusCode(), equalTo(200));
        CreateUserResponse actual = createUserResponse.as(CreateUserResponse.class);
        createdUserIds.add(actual.getId());
        assertThat(actual.getId(), not(blankString()));
        assertThat(actual.getMessage(), equalTo("Customer created"));

        Response getCreatedUserResponse = RestAssured.given().log().all()
                .header(AUTHORIZATION_HEADER, TOKEN)
                .pathParam("id", actual.getId())
                .get(GET_USER_PATH);
        System.out.printf("Create user response: %s%n", getCreatedUserResponse.asString());
        assertThat(getCreatedUserResponse.statusCode(), equalTo(200));
        //TO-DO: verify schema

        String expectedTemplate = """
                {
                    "id": "%s",
                    "firstName": "John",
                    "lastName": "Doe",
                    "middleName": "Smith",
                    "birthday": "01-23-2000",
                    "phone": "0123456789",
                    "email": "%s",
                    "createdAt": "",
                    "updatedAt": "",
                    "addresses": [
                        {
                            "id": "",
                            "customerId": "%s",
                            "streetNumber": "123",
                            "street": "Main St",
                            "ward": "Ward 1",
                            "district": "District 1",
                            "city": "Thu Duc",
                            "state": "Ho Chi Minh",
                            "zip": "70000",
                            "country": "VN",
                            "createdAt": "",
                            "updatedAt": ""
                        }
                    ]
                }
                """;
        String expected = String.format(expectedTemplate, actual.getId(), randomEmail, actual.getId());
        String actualGetCreated = getCreatedUserResponse.asString();
        assertThat(actualGetCreated, jsonEquals(expected).whenIgnoringPaths("createdAt", "updatedAt",
                "addresses[*].id", "addresses[*].createdAt", "addresses[*].updatedAt"));
    }

    @AfterAll
    static void tearDown() {
        //Clean data
        createdUserIds.forEach(id -> {
            RestAssured.given().log().all()
                    .header(AUTHORIZATION_HEADER, TOKEN)
                    .pathParam("id", id)
                    .delete(DELETE_USER_PATH);
        });
    }
}
